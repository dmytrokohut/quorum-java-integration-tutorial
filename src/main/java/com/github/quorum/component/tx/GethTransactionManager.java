package com.github.quorum.component.tx;

import static com.github.quorum.utils.Constants.NONCE_TOO_LOW_ERROR_MESSAGE;

import com.github.quorum.interfacee.TransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.FastRawTransactionManager;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.io.IOException;
import java.math.BigInteger;

@Slf4j
public class GethTransactionManager extends FastRawTransactionManager implements TransactionManager {

    private static final byte ATTEMPTS = 20;
    private static final int SLEEP_DURATION = 100;

    private final TransactionReceiptProcessor txReceiptProcessor;

    public GethTransactionManager(Web3j web3j, Credentials credentials) {
        this(web3j, credentials, new PollingTransactionReceiptProcessor(web3j, SLEEP_DURATION, ATTEMPTS));
    }

    public GethTransactionManager(Web3j web3j, Credentials credentials, TransactionReceiptProcessor txReceiptProcessor) {
        super(web3j, credentials, txReceiptProcessor);
        this.txReceiptProcessor = txReceiptProcessor;
    }

    @Override
    public TransactionReceipt executeTransaction(
            final BigInteger gasPrice, final BigInteger gasLimit, final String to, final String data) {

        while (true) {
            try {
                final EthSendTransaction ethSendTx = sendTransaction(gasPrice, gasLimit, to, data, BigInteger.ZERO);

                if (ethSendTx != null && ethSendTx.hasError() && NONCE_TOO_LOW_ERROR_MESSAGE.equals(ethSendTx.getError().getMessage())) {
                    log.warn("[BLOCKCHAIN] try to re-send transaction cause error: {}", ethSendTx.getError().getMessage());
                    continue;
                }

                return this.txReceiptProcessor.waitForTransactionReceipt(ethSendTx.getTransactionHash());

            } catch (TransactionException ex) {
                log.error("[BLOCKCHAIN] exception while receiving TransactionReceipt from Quorum node", ex);
                throw new RuntimeException(ex);

            } catch (IOException ex) {
                log.error("[BLOCKCHAIN] exception while sending transaction to Quorum node", ex);
                throw new RuntimeException(ex);
            }
        }
    }
}
