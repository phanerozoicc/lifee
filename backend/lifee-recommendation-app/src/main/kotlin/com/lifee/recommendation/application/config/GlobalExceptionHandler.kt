package com.lifee.recommendation.application.config

import com.lifee.common.exceptions.ApplicationException
import com.lifee.common.exceptions.DomainException
import com.lifee.common.exceptions.InfrastructureException
import kotlinx.coroutines.CoroutineExceptionHandler
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange
import kotlin.coroutines.CoroutineContext

/**
 * 全局异步异常处理器
 * 处理协程中的异常和Spring WebFlux的异常
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    /**
     * 协程异常处理器
     */
    val coroutineExceptionHandler = CoroutineExceptionHandler { context: CoroutineContext, exception: Throwable ->
        logger.error("Uncaught exception in coroutine: ${context[CoroutineContext.Key]}", exception)
        
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
    
    @ExceptionHandler(DomainException::class)
    suspend fun handleDomainException(
        exception: DomainException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        logger.warn("Domain exception: ${exception.message}", exception)
        
        val errorResponse = ErrorResponse(
            code = "DOMAIN_ERROR",
            message = exception.message ?: "Domain validation failed",
            timestamp = System.currentTimeMillis(),
            path = exchange.request.path.value()
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    @ExceptionHandler(ApplicationException::class)
    suspend fun handleApplicationException(
        exception: ApplicationException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        logger.error("Application exception: ${exception.message}", exception)
        
        val errorResponse = ErrorResponse(
            code = "APPLICATION_ERROR",
            message = exception.message ?: "Application error occurred",
            timestamp = System.currentTimeMillis(),
            path = exchange.request.path.value()
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
    
    @ExceptionHandler(InfrastructureException::class)
    suspend fun handleInfrastructureException(
        exception: InfrastructureException,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        logger.error("Infrastructure exception: ${exception.message}", exception)
        
        val errorResponse = ErrorResponse(
            code = "INFRASTRUCTURE_ERROR",
            message = "Service temporarily unavailable",
            timestamp = System.currentTimeMillis(),
            path = exchange.request.path.value()
        )
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse)
    }
    
    @ExceptionHandler(Exception::class)
    suspend fun handleGenericException(
        exception: Exception,
        exchange: ServerWebExchange
    ): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected exception: ${exception.message}", exception)
        
        val errorResponse = ErrorResponse(
            code = "INTERNAL_ERROR",
            message = "An unexpected error occurred",
            timestamp = System.currentTimeMillis(),
            path = exchange.request.path.value()
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}

/**
 * 错误响应数据类
 */
data class ErrorResponse(
    val code: String,
    val message: String,
    val timestamp: Long,
    val path: String
)