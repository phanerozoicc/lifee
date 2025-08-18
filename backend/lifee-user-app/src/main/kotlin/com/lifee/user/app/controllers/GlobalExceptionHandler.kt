package com.lifee.user.app.controllers

import com.lifee.common.exceptions.BusinessRuleException
import com.lifee.common.eventsourcing.ConcurrencyException
import com.lifee.common.exceptions.DomainException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.Instant
import jakarta.validation.ConstraintViolationException

/**
 * 全局异常处理器
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    /**
     * 处理业务规则异常
     */
    @ExceptionHandler(BusinessRuleException::class)
    fun handleBusinessRuleException(
        ex: BusinessRuleException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("业务规则异常: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Business Rule Violation",
            message = ex.message ?: "业务规则违反",
            path = request.getDescription(false).removePrefix("uri=")
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    /**
     * 处理领域异常
     */
    @ExceptionHandler(DomainException::class)
    fun handleDomainException(
        ex: DomainException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("领域异常: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Domain Exception",
            message = ex.message ?: "领域异常",
            path = request.getDescription(false).removePrefix("uri=")
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    /**
     * 处理并发冲突异常
     */
    @ExceptionHandler(ConcurrencyException::class)
    fun handleConcurrencyException(
        ex: ConcurrencyException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("并发冲突异常: 聚合根ID={}, 期望版本={}, 实际版本={}, 消息={}", 
            ex.aggregateId, ex.expectedVersion, ex.actualVersion, ex.message)
        
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.CONFLICT.value(),
            error = "Concurrency Conflict",
            message = "数据已被其他用户修改，请刷新后重试",
            path = request.getDescription(false).removePrefix("uri=")
        )
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException,
        request: WebRequest
    ): ResponseEntity<ValidationErrorResponse> {
        logger.warn("参数验证异常: {}", ex.message)
        
        val fieldErrors = ex.bindingResult.allErrors.map { error ->
            when (error) {
                is FieldError -> FieldErrorDetail(
                    field = error.field,
                    rejectedValue = error.rejectedValue?.toString(),
                    message = error.defaultMessage ?: "验证失败"
                )
                else -> FieldErrorDetail(
                    field = "unknown",
                    rejectedValue = null,
                    message = error.defaultMessage ?: "验证失败"
                )
            }
        }
        
        val errorResponse = ValidationErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Validation Failed",
            message = "请求参数验证失败",
            path = request.getDescription(false).removePrefix("uri="),
            fieldErrors = fieldErrors
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    /**
     * 处理约束违反异常
     */
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolationException(
        ex: ConstraintViolationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("约束违反异常: {}", ex.message)
        
        val message = ex.constraintViolations.joinToString("; ") { violation ->
            "${violation.propertyPath}: ${violation.message}"
        }
        
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Constraint Violation",
            message = message,
            path = request.getDescription(false).removePrefix("uri=")
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("非法参数异常: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.BAD_REQUEST.value(),
            error = "Illegal Argument",
            message = ex.message ?: "非法参数",
            path = request.getDescription(false).removePrefix("uri=")
        )
        
        return ResponseEntity.badRequest().body(errorResponse)
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("未处理的异常: ", ex)
        
        val errorResponse = ErrorResponse(
            timestamp = Instant.now(),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            error = "Internal Server Error",
            message = "服务器内部错误",
            path = request.getDescription(false).removePrefix("uri=")
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}

/**
 * 错误响应
 */
data class ErrorResponse(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)

/**
 * 验证错误响应
 */
data class ValidationErrorResponse(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String,
    val path: String,
    val fieldErrors: List<FieldErrorDetail>
)

/**
 * 字段错误详情
 */
data class FieldErrorDetail(
    val field: String,
    val rejectedValue: String?,
    val message: String
)