package com.lifee.user.app.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

/**
 * 数据库配置类
 */
@Configuration
@EntityScan(
    basePackages = ["com.lifee.user.infrastructure.entities"]
)
@EnableJpaAuditing
@EnableTransactionManagement
class DatabaseConfig {
    
    @Bean
    fun entityManagerFactory(dataSource: DataSource): LocalContainerEntityManagerFactoryBean {
        val em = LocalContainerEntityManagerFactoryBean()
        em.dataSource = dataSource
        em.setPackagesToScan("com.lifee.user.infrastructure.entities")
        em.jpaVendorAdapter = HibernateJpaVendorAdapter()
        return em
    }
}