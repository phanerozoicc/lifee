package com.lifee.knowledge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * 知识库服务应用启动类
 */
@SpringBootApplication(
    scanBasePackages = [
        "com.lifee.common.cqrs",
        "com.lifee.common.domain",
        "com.lifee.common.exceptions",
        "com.lifee.common.orchestration",
        "com.lifee.common.saga",
        "com.lifee.knowledge"
    ]
)

class KnowledgeApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<KnowledgeApplication>(*args)
        }
    }
}