package com.amyojiakor.TransactionMicroService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@ComponentScan(basePackages = "com.amyojiakor.TransactionMicroService")
public class TransactionMicroServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TransactionMicroServiceApplication.class, args);
	}

}
