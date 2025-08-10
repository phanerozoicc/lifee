package com.lifee.config.app.adapters.web.exceptions

import com.lifee.config.domain.exceptions.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

/**
 * 配置模块全局异常处理器
 * 处理配置相关的业务异常，确保返回适当的HTTP状态码和错误信息
 */
@RestControllerAdvice
class ConfigExceptionHandler {
    
    companion object {
        private val logger = LoggerFactory.getLogger(ConfigExceptionHandler::class.java)
    }
    
    /**
     * 处理配置未找到异常
     */
    @ExceptionHandler(ConfigurationNotFoundException::class)
    fun handleConfigurationNotFound(
        ex: ConfigurationNotFoundException
    ): ResponseEntity<ErrorResponse> {
        logger.warn("配置未找到: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            error = "CONFIGURATION_NOT_FOUND",
            message = ex.message ?: "配置未找到",
            details = mapOf(
                "configurationId" to (ex.configurationId?.toString() ?: "unknown")
            ),
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
    
    /**
     * 处理配置项未找到异常
     */
    @ExceptionHandler(ConfigItemNotFoundException::class)
    fun handleConfigItemNotFound(
        ex: ConfigItemNotFoundException
    ): ResponseEntity<ErrorResponse> {
        logger.warn("配置项未找到: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            error = "CONFIG_ITEM_NOT_FOUND",
            message = ex.message ?: "配置项未找到",
            details = mapOf(
                "key" to (ex.key?.value ?: "unknown"),
                "environment" to (ex.environment?.toString() ?: "unknown")
            ),
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
    
    /**
     * 处理无效配置异常
     */
    @ExceptionHandler(InvalidConfigurationException::class)
    fun handleInvalidConfiguration(
        ex: InvalidConfigurationException
    ): ResponseEntity<ErrorResponse> {
        logger.warn("无效配置: errorType={}, message={}", ex.errorType, ex.message)
        
        val errorResponse = ErrorResponse(
            error = "INVALID_CONFIGURATION",
            message = ex.message ?: "配置验证失败",
            details = ex.getErrorDetails(),
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    /**
     * 处理配置已存在异常
     */
    @ExceptionHandler(ConfigurationAlreadyExistsException::class)
    fun handleConfigurationAlreadyExists(
        ex: ConfigurationAlreadyExistsException
    ): ResponseEntity<ErrorResponse> {
        logger.warn("配置已存在: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            error = "CONFIGURATION_ALREADY_EXISTS",
            message = ex.message ?: "配置已存在",
            details = mapOf(
                "namespace" to (ex.namespace ?: "unknown"),
                "environment" to (ex.environment?.toString() ?: "unknown")
            ),
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }
    
    /**
     * 处理配置项已存在异常
     */
    @ExceptionHandler(ConfigItemAlreadyExistsException::class)
    fun handleConfigItemAlreadyExists(
        ex: ConfigItemAlreadyExistsException
    ): ResponseEntity<ErrorResponse> {
        logger.warn("配置项已存在: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            error = "CONFIG_ITEM_ALREADY_EXISTS",
            message = ex.message ?: "配置项已存在",
            details = mapOf(
                "key" to (ex.key?.value ?: "unknown"),
                "environment" to (ex.environment?.toString() ?: "unknown")
            ),
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }
    
    /**
     * 处理命名空间配置未找到异常
     */
    @ExceptionHandler(NamespaceConfigurationNotFoundException::class)
    fun handleNamespaceConfigurationNotFound(
        ex: NamespaceConfigurationNotFoundException
    ): ResponseEntity<ErrorResponse> {
        logger.warn("命名空间配置未找到: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            error = "NAMESPACE_CONFIGURATION_NOT_FOUND",
            message = ex.message ?: "命名空间配置未找到",
            details = mapOf(
                "namespace" to (ex.namespace ?: "unknown"),
                "environment" to (ex.environment?.toString() ?: "unknown")
            ),
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(
        ex: IllegalArgumentException
    ): ResponseEntity<ErrorResponse> {
        logger.warn("非法参数: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            error = "ILLEGAL_ARGUMENT",
            message = ex.message ?: "参数不合法",
            details = mapOf(
                "cause" to (ex.cause?.message ?: "unknown")
            ),
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        ex: RuntimeException
    ): ResponseEntity<ErrorResponse> {
        logger.error("配置模块运行时异常", ex)
        
        val errorResponse = ErrorResponse(
            error = "INTERNAL_SERVER_ERROR",
            message = "服务器内部错误",
            details = mapOf(
                "type" to ex.javaClass.simpleName,
                "cause" to (ex.cause?.message ?: "unknown")
            ),
            timestamp = LocalDateTime.now()
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
    
    /**
     * 错误响应数据类
     */
    data class ErrorResponse(
        val error: String,
        val message: String,
        val details: Map<String, Any>,
        val timestamp: LocalDateTime
    )
}

/**
 * 配置已存在异常扩展
 */
class ConfigurationAlreadyExistsException(
    val namespace: String?,
    val environment: com.lifee.config.domain.valueobjects.Environment?
) : RuntimeException("配置已存在: 命名空间 '$namespace'，环境 '$environment'")

/**
 * 配置项已存在异常
 */
class ConfigItemAlreadyExistsException(
    val key: com.lifee.config.domain.valueobjects.ConfigKey?,
    val environment: com.lifee.config.domain.valueobjects.Environment?
) : RuntimeException("配置项已存在: 键 '${key?.value}'，环境 '$environment'")

/**
 * 配置未找到异常
 */
class ConfigurationNotFoundException(
    val configurationId: com.lifee.config.domain.valueobjects.ConfigId?
) : RuntimeException("配置未找到: ID '$configurationId'")

/**
 * 命名空间配置未找到异常
 */
class NamespaceConfigurationNotFoundException(
    val namespace: String?,
    val environment: com.lifee.config.domain.valueobjects.Environment?
) : RuntimeException("命名空间配置未找到: 命名空间 '$namespace'，环境 '$environment'")