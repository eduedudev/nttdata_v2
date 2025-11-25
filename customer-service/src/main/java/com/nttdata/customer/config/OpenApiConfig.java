package com.nttdata.customer.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Customer Service API")
                        .description("API for customer management and personal data")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NTT Data")
                                .email("support@nttdata.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8081")
                                .description("Development server")
                ));
    }
}
