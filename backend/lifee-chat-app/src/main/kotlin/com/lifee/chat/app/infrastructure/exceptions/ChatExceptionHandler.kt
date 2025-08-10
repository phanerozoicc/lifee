package com.lifee.chat.app.infrastructure.exceptions

import com.lifee.chat.domain.exceptions.ConversationNotFoundException
import com.lifee.chat.domain.exceptions.InvalidMessageContentException
import com.lifee.common.exceptions.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 聊天模块异常处理器
 */
@RestControllerAdvice
class ChatExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(ChatExceptionHandler::class.java)
    
    /**
     * 处理对话未找到异常
     */
    @ExceptionHandler(ConversationNotFoundException::class)
    fun handleConversationNotFound(ex: ConversationNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("对话未找到: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            code = "CONVERSATION_NOT_FOUND",
            message = ex.message ?: "对话不存在",
            timestamp = System.currentTimeMillis()
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
    
    /**
     * 处理无效消息内容异常
     */
    @ExceptionHandler(InvalidMessageContentException::class)
    fun handleInvalidMessageContent(ex: InvalidMessageContentException): ResponseEntity<ErrorResponse> {
        logger.warn("无效消息内容: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            code = "INVALID_MESSAGE_CONTENT",
            message = ex.message ?: "消息内容不符合规范",
            timestamp = System.currentTimeMillis()
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    /**
     * 处理参数验证异常
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        logger.warn("参数验证失败: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            code = "INVALID_ARGUMENT",
            message = ex.message ?: "参数不合法",
            timestamp = System.currentTimeMillis()
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    /**
     * 处理通用运行时异常
     */
    @ExceptionHandler(RuntimeException::class)
    fun handleRuntimeException(ex: RuntimeException): ResponseEntity<ErrorResponse> {
        logger.error("聊天模块运行时异常", ex)
        
        val errorResponse = ErrorResponse(
            code = "CHAT_RUNTIME_ERROR",
            message = "聊天服务暂时不可用，请稍后重试",
            timestamp = System.currentTimeMillis()
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}