package com.lifee.knowledge.application.infrastructure.exceptions

import com.lifee.common.exceptions.ConcurrencyException
import com.lifee.common.exceptions.ErrorResponse
import com.lifee.knowledge.domain.exceptions.*
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 知识库模块异常处理器
 */
@RestControllerAdvice
class KnowledgeExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(KnowledgeExceptionHandler::class.java)
    
    /**
     * 处理知识库未找到异常
     */
    @ExceptionHandler(KnowledgeBaseNotFoundException::class)
    fun handleKnowledgeBaseNotFound(ex: KnowledgeBaseNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("知识库未找到: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            code = "KNOWLEDGE_BASE_NOT_FOUND",
            message = ex.message ?: "知识库不存在",
            timestamp = System.currentTimeMillis()
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
    
    /**
     * 处理文档未找到异常
     */
    @ExceptionHandler(DocumentNotFoundException::class)
    fun handleDocumentNotFound(ex: DocumentNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("文档未找到: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            code = "DOCUMENT_NOT_FOUND",
            message = ex.message ?: "文档不存在",
            timestamp = System.currentTimeMillis()
        )
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }
    
    /**
     * 处理无效文档异常
     */
    @ExceptionHandler(InvalidDocumentException::class)
    fun handleInvalidDocument(ex: InvalidDocumentException): ResponseEntity<ErrorResponse> {
        logger.warn("无效文档: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            code = "INVALID_DOCUMENT",
            message = ex.message ?: "文档内容不符合规范",
            timestamp = System.currentTimeMillis()
        )
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }
    
    /**
     * 处理文档已存在异常
     */
    @ExceptionHandler(DocumentAlreadyExistsException::class)
    fun handleDocumentAlreadyExists(ex: DocumentAlreadyExistsException): ResponseEntity<ErrorResponse> {
        logger.warn("文档已存在: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            code = "DOCUMENT_ALREADY_EXISTS",
            message = ex.message ?: "文档已存在",
            timestamp = System.currentTimeMillis()
        )
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }
    
    /**
     * 处理重复知识库名称异常
     */
    @ExceptionHandler(DuplicateKnowledgeBaseNameException::class)
    fun handleDuplicateKnowledgeBaseName(ex: DuplicateKnowledgeBaseNameException): ResponseEntity<ErrorResponse> {
        logger.warn("知识库名称重复: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            code = "DUPLICATE_KNOWLEDGE_BASE_NAME",
            message = ex.message ?: "知识库名称已存在",
            timestamp = System.currentTimeMillis()
        )
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
    }
    
    /**
     * 处理未授权访问异常
     */
    @ExceptionHandler(UnauthorizedAccessException::class)
    fun handleUnauthorizedAccess(ex: UnauthorizedAccessException): ResponseEntity<ErrorResponse> {
        logger.warn("未授权访问: {}", ex.message)
        
        val errorResponse = ErrorResponse(
            code = "UNAUTHORIZED_ACCESS",
            message = ex.message ?: "无权限访问该资源",
            timestamp = System.currentTimeMillis()
        )
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }
    
    /**
     * 处理并发冲突异常
     */
    @ExceptionHandler(ConcurrencyException::class)
    fun handleConcurrencyException(ex: ConcurrencyException): ResponseEntity<ErrorResponse> {
        logger.warn("知识库并发冲突异常: 聚合根ID={}, 期望版本={}, 实际版本={}, 消息={}", 
            ex.aggregateId, ex.expectedVersion, ex.actualVersion, ex.message)
        
        val errorResponse = ErrorResponse(
            code = "CONCURRENCY_CONFLICT",
            message = "知识库数据已被其他用户修改，请刷新后重试",
            timestamp = System.currentTimeMillis()
        )
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse)
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
        logger.error("知识库模块运行时异常", ex)
        
        val errorResponse = ErrorResponse(
            code = "KNOWLEDGE_RUNTIME_ERROR",
            message = "知识库服务暂时不可用，请稍后重试",
            timestamp = System.currentTimeMillis()
        )
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}