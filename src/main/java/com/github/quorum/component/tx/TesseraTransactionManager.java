package com.github.quorum.component.tx;

import static com.github.quorum.utils.Constants.NONCE_TOO_LOW_ERROR_MESSAGE;

import com.github.quorum.interfacee.TransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.enclave.Tessera;
import org.web3j.quorum.tx.QuorumTransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

@Slf4j
public class TesseraTransactionManager implements TransactionManager {

    private static final byte ATTEMPTS = 20;
    private static final int SLEEP_DURATION = 100;

    private final Quorum quorum;
    private final String fromAddress;
    private final QuorumTransactionManager quorumTxManager;
    private final TransactionReceiptProcessor txReceiptProcessor;

    public TesseraTransactionManager(
            Quorum quorum,
            Credentials credentials,
            String publicKey,
            List<String> privateFor,
            Tessera tessera
    ) {
        this.quorum = quorum;
        this.fromAddress = credentials.getAddress();
        this.quorumTxManager = new QuorumTransactionManager(quorum, credentials, publicKey, privateFor, tessera);
        this.txReceiptProcessor = new PollingTransactionReceiptProcessor(quorum, SLEEP_DURATION, ATTEMPTS);
    }

    @Override
    public TransactionReceipt executeTransaction(
            final BigInteger gasPrice, final BigInteger gasLimit, final String to, final String data) {

        while (true) {
            try {
                final EthSendTransaction ethSendTx = sendTransaction(gasPrice, gasLimit, to, data);

                if (ethSendTx.hasError() && NONCE_TOO_LOW_ERROR_MESSAGE.equals(ethSendTx.getError().getMessage())) {
                    log.warn("[BLOCKCHAIN] try to re-send transaction cause error {}", ethSendTx.getError().getMessage());
                    continue;
                }
                return processResponse(ethSendTx);

            } catch (TransactionException ex) {
                log.error("[BLOCKCHAIN] exception while receiving TransactionReceipt from Quorum node", ex);
                throw new RuntimeException(ex);
            } catch (Exception ex) {
                log.error("[BLOCKCHAIN] exception while sending transaction to Quorum node", ex);
                throw new RuntimeException(ex);
            }
        }
    }

    private EthSendTransaction sendTransaction(
            final BigInteger gasPrice, final BigInteger gasLimit, final String to, final String data) throws IOException {

        final BigInteger nonce = getNonce();
        final RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, data);

        return this.quorumTxManager.signAndSend(rawTransaction);
    }

    private TransactionReceipt processResponse(final EthSendTransaction transactionResponse)
            throws IOException, TransactionException {
        if (transactionResponse.hasError()) {
            throw new RuntimeException(
                    "[BLOCKCHAIN] error processing transaction request: "
                    + transactionResponse.getError().getMessage()
            );
        }

        final String transactionHash = transactionResponse.getTransactionHash();

        return this.txReceiptProcessor.waitForTransactionReceipt(transactionHash);
    }

    private BigInteger getNonce() throws IOException {
        final EthGetTransactionCount ethGetTxCount = this.quorum.ethGetTransactionCount(
                this.fromAddress, DefaultBlockParameterName.PENDING).send();
        return ethGetTxCount.getTransactionCount();
    }
}
