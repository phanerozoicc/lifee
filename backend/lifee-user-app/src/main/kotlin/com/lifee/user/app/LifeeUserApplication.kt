package com.lifee.user.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * Lifee用户服务应用主类
 */
@SpringBootApplication
@ComponentScan(basePackages = ["com.lifee"])
@EnableJpaRepositories(basePackages = ["com.lifee.user.infrastructure.repositories"])
@EnableKafka
@EnableTransactionManagement
class LifeeUserApplication

fun main(args: Array<String>) {
    runApplication<LifeeUserApplication>(*args)
}