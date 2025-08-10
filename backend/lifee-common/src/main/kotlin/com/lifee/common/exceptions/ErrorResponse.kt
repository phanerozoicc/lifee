package com.lifee.common.exceptions

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

/**
 * 统一错误响应格式
 */
data class ErrorResponse(
    /**
     * 错误代码
     */
    val code: String,
    
    /**
     * 错误消息
     */
    val message: String,
    
    /**
     * 时间戳（毫秒）
     */
    val timestamp: Long,
    
    /**
     * 请求路径（可选）
     */
    val path: String? = null,
    
    /**
     * HTTP状态码（可选）
     */
    val status: Int? = null,
    
    /**
     * 详细错误信息（可选，用于调试）
     */
    val details: Map<String, Any>? = null
) {
    /**
     * 便捷构造函数，使用LocalDateTime
     */
    constructor(
        code: String,
        message: String,
        timestamp: LocalDateTime,
        path: String? = null,
        status: Int? = null,
        details: Map<String, Any>? = null
    ) : this(
        code = code,
        message = message,
        timestamp = timestamp.toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        path = path,
        status = status,
        details = details
    )
}