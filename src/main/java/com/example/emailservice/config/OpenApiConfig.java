package com.example.emailservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String API_KEY_SCHEME ="apiKeyAuth";

    @Bean
    public OpenAPI emailserviceOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("Email service API")
                        .version("1.0.0")
                        .description("REST API for sending emails and tracking their status."
                        +"All /api/** endpoints require an X-API-KEY header")
                        .contact(new Contact()
                                .name("Email Service Team")
                                .email("support@emaple.com")))
                .addSecurityItem(new SecurityRequirement().addList(API_KEY_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(API_KEY_SCHEME, new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name(ApiKeyFilter.HEADER)
                                .description("Client API key, sent as the X-API-KEY header")));
    }

}
