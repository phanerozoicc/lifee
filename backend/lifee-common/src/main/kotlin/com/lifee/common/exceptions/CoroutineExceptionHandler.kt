package com.lifee.common.exceptions

import kotlinx.coroutines.CoroutineExceptionHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.coroutines.CoroutineContext

/**
 * 协程异常处理器
 * 统一处理协程中未捕获的异常
 */
@Component
class GlobalCoroutineExceptionHandler : CoroutineExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalCoroutineExceptionHandler::class.java)
    
    override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler
    
    override fun handleException(context: CoroutineContext, exception: Throwable) {
        logger.error("Uncaught exception in coroutine: ${context}", exception)
        
        // 根据异常类型进行不同的处理
        when (exception) {
            is DomainException -> {
                logger.warn("Domain exception in coroutine: ${exception.message}", exception)
            }
            is ApplicationException -> {
                logger.error("Application exception in coroutine: ${exception.message}", exception)
            }
            is InfrastructureException -> {
                logger.error("Infrastructure exception in coroutine: ${exception.message}", exception)
            }
            else -> {
                logger.error("Unknown exception in coroutine: ${exception.message}", exception)
            }
        }
    }
}