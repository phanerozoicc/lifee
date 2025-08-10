package com.lifee.common.transaction

import io.seata.spring.annotation.GlobalTransactionScanner
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Seata分布式事务配置
 */
@Configuration
class SeataConfiguration {

    @Value("\${seata.application-id:lifee-backend}")
    private lateinit var applicationId: String

    @Value("\${seata.tx-service-group:lifee-tx-group}")
    private lateinit var txServiceGroup: String

    @Bean
    fun globalTransactionScanner(): GlobalTransactionScanner {
        return GlobalTransactionScanner(applicationId, txServiceGroup)
    }
}