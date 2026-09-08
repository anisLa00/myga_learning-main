package com.myga.learning.backend.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI description served at /v3/api-docs, with Swagger UI at
 * /swagger-ui.html. The bearer scheme lets you paste a token from
 * {@code POST /api/auth/login} into "Authorize" and try the secured endpoints.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI mygaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MYGA Learning API")
                        .version("v1")
                        .description("School management platform. Roles: ADMIN, TEACHER, PARENT — "
                                + "students are academic entities and never authenticate. "
                                + "Authenticate with POST /api/auth/login and send the returned "
                                + "token as 'Authorization: Bearer <token>'."))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
