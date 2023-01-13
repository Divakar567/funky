package com.diva.funky.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.In;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;

@Configuration
public class DocumentationConfiguration {

    @Value("${document.contact-email:funky.grievance@demo4126.onmicrosoft.com}")
    private String contactEmail;

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(apiInfo())
                .components(new Components()
                        .addSecuritySchemes("OAuth 2.0 Token Authorization",
                                new SecurityScheme()
                                        .name("OAuth 2.0 Token Authorization")
                                        .type(Type.HTTP)
                                        .scheme("Bearer")
                                        .bearerFormat("OPAQUE")
                                        .in(In.HEADER)))
                .addSecurityItem(new SecurityRequirement().addList("OAuth 2.0 Token Authorization"));
    }

    private Info apiInfo() {
        return new Info()
                .title("Funky Web Application")
                .description("A simple web platform for games and sports events handling")
                .version("1.0")
                .contact(new Contact().name("IT").email(contactEmail));
    }
}
