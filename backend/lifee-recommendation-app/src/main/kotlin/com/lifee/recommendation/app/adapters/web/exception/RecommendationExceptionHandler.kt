package com.lifee.recommendation.app.adapters.web.exception

import com.lifee.common.exceptions.ErrorResponse
import com.lifee.recommendation.domain.exceptions.InvalidRecommendationException
import com.lifee.recommendation.domain.exceptions.RecommendationNotFoundException
import com.lifee.recommendation.domain.exceptions.RecommendationItemNotFoundException
import com.lifee.recommendation.domain.exceptions.DuplicateRecommendationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import java.time.LocalDateTime

/**
 * 推荐模块全局异常处理器
 */
@RestControllerAdvice
class RecommendationExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(RecommendationExceptionHandler::class.java)
    
    /**
     * 处理推荐未找到异常
     */
    @ExceptionHandler(RecommendationNotFoundException::class)
    fun handleRecommendationNotFoundException(
        ex: RecommendationNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("推荐未找到: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            code = "RECOMMENDATION_NOT_FOUND",
            message = ex.message ?: "推荐不存在",
            timestamp = LocalDateTime.now(),
            path = request.getDescription(false).removePrefix("uri="),
            status = HttpStatus.NOT_FOUND.value(),
            details = mapOf(
                "error_type" to "RecommendationNotFoundException",
                "module" to "recommendation"
            )
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
    
    /**
     * 处理推荐项未找到异常
     */
    @ExceptionHandler(RecommendationItemNotFoundException::class)
    fun handleRecommendationItemNotFoundException(
        ex: RecommendationItemNotFoundException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("推荐项未找到: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            code = "RECOMMENDATION_ITEM_NOT_FOUND",
            message = ex.message ?: "推荐项不存在",
            timestamp = LocalDateTime.now(),
            path = request.getDescription(false).removePrefix("uri="),
            status = HttpStatus.NOT_FOUND.value(),
            details = mapOf(
                "error_type" to "RecommendationItemNotFoundException",
                "module" to "recommendation"
            )
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
    
    /**
     * 处理无效推荐异常
     */
    @ExceptionHandler(InvalidRecommendationException::class)
    fun handleInvalidRecommendationException(
        ex: InvalidRecommendationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("无效推荐操作: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            code = "INVALID_RECOMMENDATION",
            message = ex.message ?: "推荐操作无效",
            timestamp = LocalDateTime.now(),
            path = request.getDescription(false).removePrefix("uri="),
            status = HttpStatus.BAD_REQUEST.value(),
            details = mapOf(
                "error_type" to "InvalidRecommendationException",
                "module" to "recommendation"
            )
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    /**
     * 处理重复推荐异常
     */
    @ExceptionHandler(DuplicateRecommendationException::class)
    fun handleDuplicateRecommendationException(
        ex: DuplicateRecommendationException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("重复推荐: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            code = "DUPLICATE_RECOMMENDATION",
            message = ex.message ?: "推荐已存在",
            timestamp = LocalDateTime.now(),
            path = request.getDescription(false).removePrefix("uri="),
            status = HttpStatus.CONFLICT.value(),
            details = mapOf(
                "error_type" to "DuplicateRecommendationException",
                "module" to "recommendation"
            )
        )
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }
    
    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.warn("推荐模块参数错误: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            code = "INVALID_ARGUMENT",
            message = ex.message ?: "参数无效",
            timestamp = LocalDateTime.now(),
            path = request.getDescription(false).removePrefix("uri="),
            status = HttpStatus.BAD_REQUEST.value(),
            details = mapOf(
                "error_type" to "IllegalArgumentException",
                "module" to "recommendation"
            )
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(
        ex: RuntimeException,
        request: WebRequest
    ): ResponseEntity<ErrorResponse> {
        logger.error("推荐模块运行时异常: {}", ex.message, ex)
        
        val errorResponse = ErrorResponse(
            code = "INTERNAL_ERROR",
            message = "推荐服务内部错误",
            timestamp = LocalDateTime.now(),
            path = request.getDescription(false).removePrefix("uri="),
            status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            details = mapOf(
                "error_type" to "RuntimeException",
                "module" to "recommendation",
                "original_message" to (ex.message ?: "未知错误")
            )
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}