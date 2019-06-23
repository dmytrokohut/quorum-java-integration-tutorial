package com.github.quorum.web;

import static com.github.quorum.utils.Constants.DEPLOY_GAS_LIMIT;
import static com.github.quorum.utils.Constants.GAS_PRICE;

import com.github.quorum.component.dto.APIResponse;
import com.github.quorum.component.dto.ReadUserRequest;
import com.github.quorum.component.wrappers.QuorumDemo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.http.HttpService;
import org.web3j.quorum.Quorum;
import org.web3j.tx.gas.StaticGasProvider;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Component
public class SmartContractReader {

    private final Credentials credentials;

    /**
     * Read user from smart-contract.
     *
     * @param serverRequest
     *          - {@link ServerRequest} object with request information
     * @return {@link ServerResponse} object with response data
     */
    public Mono<ServerResponse> readUser(final ServerRequest serverRequest) {
        return serverRequest
                .bodyToMono(ReadUserRequest.class)
                .map(this::readUserFromSmartContract)
                .flatMap(this::generateResponse);
    }

    private APIResponse readUserFromSmartContract(final ReadUserRequest readUserRequest) {
        final Quorum quorum = Quorum.build(new HttpService(readUserRequest.getEndpoint()));

        log.info("[READER] reading user from smart-contract with '{}' address", readUserRequest.getContractAddress());
        final QuorumDemo quorumDemo = QuorumDemo.load(readUserRequest.getContractAddress(), quorum, this.credentials, new StaticGasProvider(GAS_PRICE, DEPLOY_GAS_LIMIT));
        try {
            final String user = quorumDemo.user().send().getValue();
            log.info("[READER] user: {}", user);
            return APIResponse.newInstance(user);
        } catch (Exception ex) {
            log.info("[READER] exception happens while reading user from smart-contract");
            return APIResponse.newInstance(500, "Something went wrong");
        }
    }

    private Mono<ServerResponse> generateResponse(final APIResponse apiResponse) {
        return ServerResponse
                .ok()
                .body(Mono.just(apiResponse), APIResponse.class);
    }
}
