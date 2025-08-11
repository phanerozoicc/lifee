package com.github.phanerozoicc.base.response

import java.time.LocalDateTime

/**
 * 统一API响应格式
 */
data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val data: T? = null,
    val error: String? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun <T> success(data: T, message: String = "操作成功"): ApiResponse<T> {
            return ApiResponse(
                success = true,
                message = message,
                data = data
            )
        }
        
        fun success(message: String = "操作成功"): ApiResponse<Unit> {
            return ApiResponse(
                success = true,
                message = message,
//                data = Unit
            )
        }
        
        fun <T> error(error: String, details: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                message = details,
                error = error
            )
        }
    }
}