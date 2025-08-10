package com.lifee.knowledge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * 知识库服务应用启动类
 */
@SpringBootApplication(
    scanBasePackages = [
        "com.lifee.common",
        "com.lifee.knowledge"
    ]
)
@EnableJpaRepositories(basePackages = ["com.lifee.knowledge.infrastructure.persistence.repositories"])
@EnableTransactionManagement
class KnowledgeApplication

fun main(args: Array<String>) {
    runApplication<KnowledgeApplication>(*args)
}