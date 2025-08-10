package com.lifee.common.coroutines

import com.lifee.common.exceptions.GlobalCoroutineExceptionHandler
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import jakarta.annotation.PreDestroy

/**
 * 协程作用域管理器
 * 统一管理应用中的协程作用域
 */
@Component
class CoroutineScopeManager(
    private val exceptionHandler: GlobalCoroutineExceptionHandler
) {
    
    /**
     * 应用级协程作用域
     */
    val applicationScope = CoroutineScope(
        SupervisorJob() + 
        Dispatchers.Default + 
        exceptionHandler
    )
    
    /**
     * IO操作协程作用域
     */
    val ioScope = CoroutineScope(
        SupervisorJob() + 
        Dispatchers.IO + 
        exceptionHandler
    )
    
    /**
     * 计算密集型操作协程作用域
     */
    val computationScope = CoroutineScope(
        SupervisorJob() + 
        Dispatchers.Default + 
        exceptionHandler
    )
    
    /**
     * 主线程协程作用域（用于UI更新等）
     */
    val mainScope = CoroutineScope(
        SupervisorJob() + 
        Dispatchers.Main + 
        exceptionHandler
    )
    
    /**
     * 创建带超时的协程作用域
     */
    fun createTimeoutScope(timeoutMs: Long): CoroutineScope {
        return CoroutineScope(
            SupervisorJob() + 
            Dispatchers.IO + 
            exceptionHandler
        )
    }
    
    /**
     * 在IO作用域中启动协程
     */
    fun launchIO(block: suspend CoroutineScope.() -> Unit): Job {
        return ioScope.launch(block = block)
    }
    
    /**
     * 在计算作用域中启动协程
     */
    fun launchComputation(block: suspend CoroutineScope.() -> Unit): Job {
        return computationScope.launch(block = block)
    }
    
    /**
     * 在IO作用域中执行异步操作
     */
    suspend fun <T> asyncIO(block: suspend CoroutineScope.() -> T): Deferred<T> {
        return ioScope.async(block = block)
    }
    
    /**
     * 在计算作用域中执行异步操作
     */
    suspend fun <T> asyncComputation(block: suspend CoroutineScope.() -> T): Deferred<T> {
        return computationScope.async(block = block)
    }
    
    /**
     * 带超时的执行
     */
    suspend fun <T> withTimeout(timeoutMs: Long, block: suspend CoroutineScope.() -> T): T {
        return withTimeout(timeoutMs, block)
    }
    
    /**
     * 应用关闭时清理资源
     */
    @PreDestroy
    fun cleanup() {
        applicationScope.cancel("Application is shutting down")
        ioScope.cancel("Application is shutting down")
        computationScope.cancel("Application is shutting down")
        mainScope.cancel("Application is shutting down")
    }
}