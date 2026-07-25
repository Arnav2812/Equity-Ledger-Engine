package com.equityledger.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI equityLedgerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Equity Ledger Engine API")
                        .description("Enterprise-grade double-entry bookkeeping engine with pessimistic locking, corporate action strategy processing, and trade guardrails.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Arnav Sharma")
                                .email("arnavcollege28@gmail.com")));
    }
}