package com.github.phanerozoicc.user

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.context.annotation.ComponentScan
import org.springframework.boot.autoconfigure.domain.EntityScan

/**
 * 用户模块Spring Boot应用程序启动类
 */
@SpringBootApplication
@EnableJpaRepositories(
    basePackages = ["com.github.phanerozoicc.user.infrastructure.persistence.repository"]
)
@EntityScan(
    basePackages = ["com.github.phanerozoicc.user.infrastructure.persistence.entity"]
)
@EnableTransactionManagement
@ComponentScan(
    basePackages = [
        "com.github.phanerozoicc.user.application",
        "com.github.phanerozoicc.user.domain",
        "com.github.phanerozoicc.user.infrastructure",
        "com.github.phanerozoicc.user.presentation"
    ]
)
class UserApplication

fun main(args: Array<String>) {
    runApplication<UserApplication>(*args)
}