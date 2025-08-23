package com.lifee.chat.app.config

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.core.task.SimpleAsyncTaskExecutor

/**
 * 协程配置类
 * 配置Spring Boot支持Kotlin协程
 */
@Configuration
@EnableAsync
class CoroutineConfig : WebMvcConfigurer {
    
    /**
     * 配置异步支持
     */
    override fun configureAsyncSupport(configurer: AsyncSupportConfigurer) {
        configurer.setDefaultTimeout(30000) // 30秒超时
        configurer.setTaskExecutor(asyncExecutor())
    }
    
    /**
     * 异步执行器
     */
    @Bean("asyncExecutor")
    fun asyncExecutor(): AsyncTaskExecutor {
        return SimpleAsyncTaskExecutor("async-")
    }
    
    /**
     * 协程调度器
     */
    @Bean
    fun coroutineDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }
}