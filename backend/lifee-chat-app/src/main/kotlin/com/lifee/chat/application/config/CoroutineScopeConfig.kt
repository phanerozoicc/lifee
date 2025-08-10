package com.lifee.chat.application.config

import com.lifee.common.coroutines.CoroutineScopeManager
import com.lifee.common.exceptions.GlobalCoroutineExceptionHandler
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PreDestroy

/**
 * 协程作用域配置
 * 管理应用程序中的协程生命周期
 */
@Configuration
class CoroutineScopeConfig {
    
    private lateinit var scopeManager: CoroutineScopeManager
    
    @Bean
    fun coroutineScopeManager(
        coroutineDispatcher: CoroutineDispatcher,
        globalCoroutineExceptionHandler: GlobalCoroutineExceptionHandler
    ): CoroutineScopeManager {
        scopeManager = CoroutineScopeManager(globalCoroutineExceptionHandler)
        return scopeManager
    }
    
    @Bean
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO
    
    @Bean
    fun defaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
    
    @Bean
    fun unconfinedDispatcher(): CoroutineDispatcher = Dispatchers.Unconfined
    
    @PreDestroy
    fun cleanup() {
        if (::scopeManager.isInitialized) {
            scopeManager.cleanup()
        }
    }
}