package com.github.phanerozoicc.user.interfaces.rest

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.*
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 通用API响应
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null,
    val details: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun <T> success(data: T, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = true,
                data = data,
                message = message
            )
        }
        
        fun <T> error(error: String, details: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = error,
                details = details
            )
        }
    }
}

/**
 * 分页响应
 */
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean = page == 0,
    val last: Boolean = page == totalPages - 1,
    val numberOfElements: Int = content.size
)

/**
 * 用户注册请求
 */
data class RegisterUserRequest(
    @field:Email(message = "邮箱格式不正确")
    @field:NotBlank(message = "邮箱不能为空")
    val email: String,
    
    @field:NotBlank(message = "密码不能为空")
    @field:Size(min = 8, max = 128, message = "密码长度必须在8-128位之间")
    val password: String,
    
    @field:NotBlank(message = "昵称不能为空")
    @field:Size(min = 2, max = 50, message = "昵称长度必须在2-50位之间")
    val nickname: String,
    
    @field:Size(max = 50, message = "名字长度不能超过50位")
    val firstName: String? = null,
    
    @field:Size(max = 50, message = "姓氏长度不能超过50位")
    val lastName: String? = null
)

data class RegisterUserResponse(
    val userId: String,
    val username: String,
    val email: String,
)

/**
 * 用户登录请求
 */
data class LoginUserRequest(
    @field:Email(message = "邮箱格式不正确")
    @field:NotBlank(message = "邮箱不能为空")
    val email: String,
    
    @field:NotBlank(message = "密码不能为空")
    val password: String,
    
    val ipAddress: String? = null,
    val userAgent: String? = null
)

/**
 * 登录响应
 */
data class LoginResponse(
    val user: UserProfileDTO,
    val token: String,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val expiresAt: LocalDateTime
)

/**
 * 更新用户资料请求
 */
data class UpdateUserProfileRequest(
    @field:Size(min = 2, max = 50, message = "昵称长度必须在2-50位之间")
    val nickname: String? = null,
    
    @field:Size(max = 50, message = "名字长度不能超过50位")
    val firstName: String? = null,
    
    @field:Size(max = 50, message = "姓氏长度不能超过50位")
    val lastName: String? = null,
    
    @field:Size(max = 500, message = "头像URL长度不能超过500位")
    val avatar: String? = null,
    
    @field:Size(max = 500, message = "个人简介长度不能超过500位")
    val bio: String? = null,
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate? = null,
    
    val gender: String? = null,
    
    @field:Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "手机号格式不正确")
    val phoneNumber: String? = null,
    
    @field:Size(max = 200, message = "地址长度不能超过200位")
    val address: String? = null,
    
    @field:Size(max = 200, message = "网站URL长度不能超过200位")
    val website: String? = null
)

/**
 * 修改密码请求
 */
data class ChangePasswordRequest(
    @field:NotBlank(message = "当前密码不能为空")
    val currentPassword: String,
    
    @field:NotBlank(message = "新密码不能为空")
    @field:Size(min = 8, max = 128, message = "新密码长度必须在8-128位之间")
    val newPassword: String
)

/**
 * 验证邮箱请求
 */
data class VerifyEmailRequest(
    @field:NotBlank(message = "验证令牌不能为空")
    val token: String
)

/**
 * 用户资料DTO
 */
data class UserProfileDTO(
    val id: String,
    val email: String,
    val nickname: String,
    val firstName: String?,
    val lastName: String?,
    val avatar: String?,
    val bio: String?,
    @JsonFormat(pattern = "yyyy-MM-dd")
    val birthDate: LocalDate?,
    val gender: String?,
    val phoneNumber: String?,
    val address: String?,
    val website: String?,
    val status: String,
    val emailVerified: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val updatedAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val lastLoginAt: LocalDateTime?
)

/**
 * 用户摘要DTO（用于列表显示）
 */
data class UserSummaryDTO(
    val id: String,
    val email: String,
    val nickname: String,
    val firstName: String?,
    val lastName: String?,
    val avatar: String?,
    val status: String,
    val emailVerified: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val lastLoginAt: LocalDateTime?
)

/**
 * 用户搜索结果DTO
 */
data class UserSearchResultDTO(
    val id: String,
    val email: String,
    val nickname: String,
    val firstName: String?,
    val lastName: String?,
    val avatar: String?,
    val status: String,
    val emailVerified: Boolean,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val createdAt: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val lastLoginAt: LocalDateTime?,
    val matchScore: Double? = null // 搜索匹配度
)

/**
 * 用户统计DTO
 */
data class UserStatisticsDTO(
    val totalUsers: Long,
    val activeUsers: Long,
    val inactiveUsers: Long,
    val lockedUsers: Long,
    val verifiedUsers: Long,
    val unverifiedUsers: Long,
    val newUsersToday: Long,
    val newUsersThisWeek: Long,
    val newUsersThisMonth: Long,
    val activeUsersToday: Long,
    val activeUsersThisWeek: Long,
    val activeUsersThisMonth: Long
)

/**
 * 用户状态类型枚举
 */
enum class UserStatusType {
    ACTIVE,
    INACTIVE,
    LOCKED,
    PENDING
}

/**
 * 批量操作请求
 */
data class BatchOperationRequest(
    @field:NotEmpty(message = "用户ID列表不能为空")
    val userIds: List<String>,
    
    @field:NotBlank(message = "操作类型不能为空")
    val operation: String // ACTIVATE, DEACTIVATE, DELETE
)

/**
 * 批量操作响应
 */
data class BatchOperationResponse(
    val successCount: Int,
    val failureCount: Int,
    val failures: List<BatchOperationFailure> = emptyList()
)

/**
 * 批量操作失败项
 */
data class BatchOperationFailure(
    val userId: String,
    val reason: String
)