package com.amyojiakor.TransactionMicroService.apiConfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import reactor.core.publisher.Mono;

@Configuration
public class ApiConfig {
    @Value("${myapp.api.base-url.accounts-service}")
    private String accountServiceBaseUrl;

    @Value("${myapp.api.base-url.user-service}")
    private String userServiceBaseUrl;

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }


    public String getAccountServiceBaseUrl() {
        return accountServiceBaseUrl;
    }

    public String getUserServiceBaseUrl() {
        return userServiceBaseUrl;
    }
}
