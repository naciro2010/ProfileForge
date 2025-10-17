package com.acme.linkedinoptimizer.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("LinkedIn Optimizer API")
                .version("1.0.0")
                .description("API pour scorer et générer des suggestions pour les profils LinkedIn.")
        )
        .components(
            Components().addSecuritySchemes(
                "ApiKeyAuth",
                SecurityScheme()
                    .type(SecurityScheme.Type.APIKEY)
                    .`in`(SecurityScheme.In.HEADER)
                    .name("X-API-Key")
            )
        )
        .addSecurityItem(SecurityRequirement().addList("ApiKeyAuth"))
}
