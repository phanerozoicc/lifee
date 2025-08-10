package com.lifee.config.app.configuration

import com.lifee.config.domain.repositories.ConfigurationRepository
import com.lifee.config.domain.services.ConfigurationDomainService
import com.lifee.config.domain.services.ConfigValidationService
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * 配置模块Spring配置
 */
@Configuration
@ComponentScan(
    basePackages = [
        "com.lifee.config.app.application",
        "com.lifee.config.app.adapters"
    ]
)
@EntityScan(basePackages = ["com.lifee.config.app.adapters.persistence"])
@EnableJpaRepositories(basePackages = ["com.lifee.config.app.adapters.persistence"])
@EnableTransactionManagement
class ConfigurationModuleConfiguration {
    
    /**
     * 配置领域服务Bean
     */
    @Bean
    fun configurationDomainService(): ConfigurationDomainService {
        return ConfigurationDomainService()
    }
    
    /**
     * 配置验证服务Bean
     */
    @Bean
    fun configValidationService(): ConfigValidationService {
        return ConfigValidationService()
    }
}