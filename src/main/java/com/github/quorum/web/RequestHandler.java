package com.github.quorum.web;

import static com.github.quorum.utils.Constants.*;

import com.github.quorum.component.dto.APIRequest;
import com.github.quorum.component.dto.APIResponse;
import com.github.quorum.component.tx.GethTransactionManager;
import com.github.quorum.component.tx.TesseraTransactionManager;
import com.github.quorum.component.wrappers.QuorumDemo;
import com.github.quorum.configuration.BlockchainConfig;
import com.github.quorum.interfacee.TransactionManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.enclave.Tessera;
import org.web3j.tx.gas.StaticGasProvider;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class RequestHandler {

    private final Web3j web3j;

    private final Quorum quorum;

    private final Tessera tessera;

    private final Credentials credentials;

    private final BlockchainConfig blockchainConfig;

    private String deployedContract;

    @Autowired
    public RequestHandler(
            @Qualifier("initWeb3j") Web3j web3j,
            Quorum quorum,
            Tessera tessera,
            Credentials credentials,
            BlockchainConfig blockchainConfig
    ) {
        this.web3j = web3j;
        this.quorum = quorum;
        this.tessera = tessera;
        this.credentials = credentials;
        this.blockchainConfig = blockchainConfig;
    }

    /**
     * Deploy new smart-contract.
     *
     * @param serverRequest
     *          - {@link ServerRequest} object with request information
     * @return {@link ServerResponse} object with response data
     */
    public Mono<ServerResponse> deployContract(final ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(APIRequest.class)
                .map(this::getTransactionManager)
                .map(this::deployContract)
                .flatMap(this::generateResponse);
    }

    private TransactionManager getTransactionManager(final APIRequest apiRequest) {
        log.info("[HANDLER] privateFor = {}", apiRequest.getPrivateFor());
        TransactionManager txManager;
        if (isPrivate(apiRequest.getPrivateFor())) {
            if (apiRequest.getPrivateFor().size() == 0) {
                apiRequest.getPrivateFor().add(this.blockchainConfig.getTesseraPublicKey());
            }
            txManager = new TesseraTransactionManager(this.quorum, this.credentials, this.blockchainConfig.getTesseraPublicKey(), apiRequest.getPrivateFor(), this.tessera);
        } else {
            txManager = new GethTransactionManager(this.web3j, this.credentials);
        }

        return txManager;
    }

    private boolean isPrivate(final List<String> limitedTo) {
        return limitedTo == null || limitedTo.size() == 0 || !limitedTo.get(0).equals("public");
    }

    private APIResponse deployContract(final TransactionManager txManager) {
        log.info("[HANDLER] deploying new smart-contract");
        final String data = QuorumDemo.getBinary();
        final TransactionReceipt txReceipt = txManager.executeTransaction(GAS_PRICE, DEPLOY_GAS_LIMIT, null, data);
        final APIResponse apiResponse = APIResponse.newInstance(txReceipt);
        this.deployedContract = txReceipt.getContractAddress();
        log.info("[HANDLER] contract has been successfully deployed. Result: {}", apiResponse.getData());

        return apiResponse;
    }

    private Mono<ServerResponse> generateResponse(final APIResponse apiResponse) {
        return ServerResponse
                .ok()
                .body(Mono.just(apiResponse.getData()), Map.class);
    }

    /**
     * Send transaction on update user in smart-contract.
     *
     * @param serverRequest
     *          - {@link ServerRequest} object with request information
     * @return {@link ServerResponse} object with response data
     */
    public Mono<ServerResponse> updateUser(final ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(APIRequest.class)
                .map(this::sendTransaction)
                .flatMap(this::generateResponse);
    }

    private APIResponse sendTransaction(final APIRequest apiRequest) {
        final TransactionManager txManager = getTransactionManager(apiRequest);
        log.info("[HANDLER] sending new transaction");
        final String data = QuorumDemo.getDataOnWriteUser(apiRequest.getUser());
        final TransactionReceipt txReceipt = txManager.executeTransaction(GAS_PRICE, TX_GAS_LIMIT, this.deployedContract, data);
        final APIResponse apiResponse = APIResponse.newInstance(txReceipt);
        log.info("[HANDLER] transaction has been successfully executed. Result: {}", apiResponse.getData());

        return apiResponse;
    }

    /**
     * Read user from smart-contract.
     *
     * @param serverRequest
     *          - {@link ServerRequest} object with request information
     * @return {@link ServerResponse} object with response data
     */
    public Mono<ServerResponse> getUser(final ServerRequest serverRequest) {
        final APIResponse apiResponse = getUser();
        return generateResponse(apiResponse);
    }

    private APIResponse getUser() {
        log.info("[HANDLER] reading user from smart-contract");
        final QuorumDemo quorumDemo = QuorumDemo.load(this.deployedContract, this.web3j, this.credentials, new StaticGasProvider(GAS_PRICE, DEPLOY_GAS_LIMIT));
        final String user = readUserFromSmartContract(quorumDemo);
        final APIResponse apiResponse = APIResponse.newInstance(user);
        log.info("[HANDLER] user: '{}'", user);

        return apiResponse;
    }

    private String readUserFromSmartContract(final QuorumDemo quorumDemo) {
        try {
            return quorumDemo.user().send().getValue();
        } catch (Exception ex) {
            log.info("[HANDLER] exception while reading user from smart-contract: {}", ex);
            return null;
        }
    }
}
