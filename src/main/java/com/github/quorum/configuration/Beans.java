package com.github.quorum.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.quorum.Quorum;
import org.web3j.quorum.enclave.Tessera;
import org.web3j.quorum.enclave.protocol.EnclaveService;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class Beans {

    private final BlockchainConfig blockchainConfig;

    @Bean
    public Credentials initCredentials() {
        return Credentials.create(this.blockchainConfig.getWalletPrivateKey());
    }

    @Bean
    public Web3j initWeb3j() {
        return Web3j.build(new HttpService(this.blockchainConfig.getGethNodeEndpoint()));
    }

    @Bean
    public Quorum initQuorum() {
        return Quorum.build(new HttpService(this.blockchainConfig.getGethNodeEndpoint()));
    }

    @Bean
    public Tessera initEnclaveService() {
        final EnclaveService enclaveService = new EnclaveService(
                this.blockchainConfig.getTesseraNodeHost(),
                this.blockchainConfig.getTesseraNodePort(),
                new OkHttpClient()
        );
        return new Tessera(enclaveService, initQuorum());
    }
}
