package com.github.phanerozoicc.knowledge

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.transaction.annotation.EnableTransactionManagement


@EnableJpaAuditing
@EnableTransactionManagement
@EnableCaching
@EnableAsync
@SpringBootApplication
class KnowledgeAppApplication


fun main(args: Array<String>) {
    runApplication<KnowledgeAppApplication>(*args)
}
