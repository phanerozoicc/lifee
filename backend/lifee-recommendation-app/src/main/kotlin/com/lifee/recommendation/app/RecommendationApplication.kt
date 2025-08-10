package com.lifee.recommendation.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * 推荐系统应用启动类
 */
@SpringBootApplication(
    scanBasePackages = [
        "com.lifee.common",
        "com.lifee.recommendation"
    ]
)
@EnableTransactionManagement
class RecommendationApplication

fun main(args: Array<String>) {
    runApplication<RecommendationApplication>(*args)
}