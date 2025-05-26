package com.om.integration.deal.om_api_integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class OmApiIntegrationApplication {

	public static void main(String[] args) {
		SpringApplication.run(OmApiIntegrationApplication.class, args);
	}

}
