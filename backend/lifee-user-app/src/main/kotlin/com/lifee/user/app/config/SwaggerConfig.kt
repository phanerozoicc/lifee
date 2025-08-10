package com.lifee.user.app.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Swagger配置类
 */
@Configuration
class SwaggerConfig {
    
    @Value("\${server.port:8081}")
    private var serverPort: Int = 8081
    
    @Value("\${server.servlet.context-path:/api/v1}")
    private lateinit var contextPath: String
    
    /**
     * OpenAPI配置
     */
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Lifee用户服务API")
                    .description("Lifee知识库管理系统用户服务的RESTful API文档")
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Lifee开发团队")
                            .email("dev@lifee.com")
                            .url("https://lifee.com")
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:$serverPort$contextPath")
                        .description("本地开发环境"),
                    Server()
                        .url("https://api.lifee.com/user")
                        .description("生产环境")
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
                            .description("JWT认证令牌")
                    )
            )
    }
}