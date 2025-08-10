package com.lifee.config.application.config

import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * R2DBC配置类
 * 配置异步数据库连接和事务管理
 */
@Configuration
@EnableR2dbcRepositories(basePackages = ["com.lifee.config.infrastructure.persistence.repositories"])
@EnableTransactionManagement
class R2dbcConfig : AbstractR2dbcConfiguration() {
    
    @Value("\${spring.r2dbc.url:r2dbc:postgresql://localhost:5432/lifee_config}")
    private lateinit var url: String
    
    @Value("\${spring.r2dbc.username:lifee}")
    private lateinit var username: String
    
    @Value("\${spring.r2dbc.password:password}")
    private lateinit var password: String
    
    @Value("\${spring.r2dbc.pool.initial-size:10}")
    private var initialSize: Int = 10
    
    @Value("\${spring.r2dbc.pool.max-size:20}")
    private var maxSize: Int = 20
    
    @Bean
    override fun connectionFactory(): ConnectionFactory {
        return PostgresqlConnectionFactory(
            PostgresqlConnectionConfiguration.builder()
                .host("localhost")
                .port(5432)
                .database("lifee_config")
                .username(username)
                .password(password)
                .build()
        )
    }
    
    @Bean
    fun reactiveTransactionManager(connectionFactory: ConnectionFactory): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }
}