package com.github.quorum.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "blockchain")
public class BlockchainConfig {

    private String gethNodeEndpoint;

    private String walletPrivateKey;

    private String tesseraPublicKey;

    private String tesseraNodeHost;

    private Integer tesseraNodePort;
}
