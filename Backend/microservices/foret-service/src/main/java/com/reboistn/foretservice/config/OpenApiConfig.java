package com.reboistn.foretservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI foretServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Foret Service API - ReboisTN")
                        .description("Microservice de gestion des zones forestières tunisiennes")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ReboisTN Team")
                                .email("contact@reboistn.tn"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
