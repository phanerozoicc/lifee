package com.lifee.chat.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@ComponentScan(
    basePackages = [
        "com.lifee.chat.app",
        "com.lifee.common"
    ]
)
@EnableJpaRepositories(basePackages = ["com.lifee.chat.infrastructure"])
@EnableTransactionManagement
class ChatApplication

fun main(args: Array<String>) {
    runApplication<ChatApplication>(*args)
}