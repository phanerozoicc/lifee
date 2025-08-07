package com.github.phanerozoicc.user

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

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
class UserApplication

fun main(args: Array<String>) {
    runApplication<UserApplication>(*args)
}