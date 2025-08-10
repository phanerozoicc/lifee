package com.lifee.config.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * 配置应用启动类
 */
@SpringBootApplication(
    scanBasePackages = [
        "com.lifee.common",
        "com.lifee.config"
    ]
)
class ConfigurationApplication

fun main(args: Array<String>) {
    runApplication<ConfigurationApplication>(*args)
}