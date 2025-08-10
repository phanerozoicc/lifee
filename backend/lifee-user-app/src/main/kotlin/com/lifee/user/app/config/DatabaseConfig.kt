package com.lifee.user.app.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * 数据库配置类
 */
@Configuration
@EnableJpaRepositories(
    basePackages = ["com.lifee.user.infrastructure.repositories"]
)
@EntityScan(
    basePackages = ["com.lifee.user.infrastructure.entities"]
)
@EnableJpaAuditing
@EnableTransactionManagement
class DatabaseConfig