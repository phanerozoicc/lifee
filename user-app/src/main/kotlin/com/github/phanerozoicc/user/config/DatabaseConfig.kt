package com.github.phanerozoicc.infrastructure.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

/**
 * 数据库配置
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = ["com.github.phanerozoicc.infrastructure.persistence"],
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
class DatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    fun hikariConfig(dataSourceProperties: DataSourceProperties): HikariConfig {
        val config = HikariConfig()
        config.driverClassName = dataSourceProperties.driverClassName
        config.jdbcUrl = dataSourceProperties.url
        config.username = dataSourceProperties.username
        config.password = dataSourceProperties.password
        
        // Connection pool settings
        config.maximumPoolSize = 20
        config.minimumIdle = 5
        config.connectionTimeout = 30000
        config.idleTimeout = 600000
        config.maxLifetime = 1800000
        config.leakDetectionThreshold = 60000
        
        // PostgreSQL specific settings
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        config.addDataSourceProperty("useServerPrepStmts", "true")
        config.addDataSourceProperty("reWriteBatchedInserts", "true")
        
        return config
    }

    @Bean
    @Primary
    fun dataSource(): DataSource {
        return HikariDataSource(hikariConfig())
    }

    @Bean
    @Primary
    fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val factory = LocalContainerEntityManagerFactoryBean()
        factory.dataSource = dataSource()
        factory.setPackagesToScan(
            "com.github.phanerozoicc.**.entity"
        )
        
        val vendorAdapter = HibernateJpaVendorAdapter()
        vendorAdapter.setGenerateDdl(false)
        vendorAdapter.setShowSql(false)
        factory.jpaVendorAdapter = vendorAdapter
        
        val properties = mutableMapOf<String, Any>()
        properties["hibernate.dialect"] = "org.hibernate.dialect.PostgreSQLDialect"
        properties["hibernate.hbm2ddl.auto"] = "validate"
        properties["hibernate.show_sql"] = "false"
        properties["hibernate.format_sql"] = "true"
        properties["hibernate.use_sql_comments"] = "true"
        properties["hibernate.jdbc.batch_size"] = "25"
        properties["hibernate.order_inserts"] = "true"
        properties["hibernate.order_updates"] = "true"
        properties["hibernate.jdbc.batch_versioned_data"] = "true"
        properties["hibernate.connection.provider_disables_autocommit"] = "true"
        
        factory.setJpaPropertyMap(properties)
        return factory
    }

    @Bean
    @Primary
    fun transactionManager(): PlatformTransactionManager {
        val transactionManager = JpaTransactionManager()
        transactionManager.entityManagerFactory = entityManagerFactory().`object`
        return transactionManager
    }
}