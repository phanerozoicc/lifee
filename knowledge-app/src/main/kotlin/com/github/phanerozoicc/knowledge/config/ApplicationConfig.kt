package com.github.phanerozoicc.knowledge.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 应用程序配置类
 * 配置OpenAPI文档、CORS等
 */
@Configuration
class ApplicationConfig : WebMvcConfigurer {

    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Lifee API")
                    .description("AI驱动的个人知识管理系统 API 文档")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Lifee Team")
                            .email("support@lifee.com")
                            .url("https://github.com/phanerozoicc/lifee")
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .addSecurityItem(
                SecurityRequirement().addList("Bearer Authentication")
            )
            .components(
                io.swagger.v3.oas.models.Components()
                    .addSecuritySchemes(
                        "Bearer Authentication",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("JWT token for API authentication")
                    )
            )
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOriginPatterns = listOf("*")
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        configuration.maxAge = 3600L

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}