package org.starcoin.bifrost.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class Web3jConfig {

    @Value("${ethereum.http-service-url}")
    private String ethereumHttpServiceUrl;// = "HTTP://127.0.0.1:7545";

    @Bean
    public Web3j web3j() {
        return Web3j.build(new HttpService(ethereumHttpServiceUrl));
    }

}
