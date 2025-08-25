package com.github.phanerozoicc.user.bak.domain.query

import com.github.phanerozoicc.user.domain.model.*
import java.time.LocalDateTime

/**
 * 用户查询相关的数据传输对象和查询模型
 */

/**
 * 用户摘要DTO
 * 用于列表显示的简化用户信息
 */
data class UserSummaryDTO(
    val userId: String,
    val email: String,
    val nickname: String,
    val displayName: String,
    val avatar: String?,
    val status: String,
    val statusDisplayName: String,
    val emailVerified: Boolean,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime?
) {
    companion object {
        /**
         * 从用户聚合根创建DTO
         */
        fun fromUser(user: User): UserSummaryDTO {
            val profile = user.getProfile()
            val status = user.getStatus()
            
            return UserSummaryDTO(
                userId = user.id.getValue(),
                email = user.getEmail().getValue(),
                nickname = profile.getNickname(),
                displayName = profile.getDisplayName(),
                avatar = profile.getAvatar(),
                status = status.getStatus().name,
                statusDisplayName = status.getDisplayName(),
                emailVerified = user.isEmailVerified(),
                createdAt = user.getCreatedAt(),
                lastLoginAt = user.getLastLoginAt()
            )
        }
    }
}

/**
 * 用户权限DTO
 * 用于返回用户的权限信息
 */
data class UserPermissionsDTO(
    val userId: String,
    val canLogin: Boolean,
    val canPerformActions: Boolean,
    val needsPasswordUpdate: Boolean,
    val accountLocked: Boolean,
    val emailVerified: Boolean,
    val permissions: List<String>, // 具体权限列表
    val roles: List<String> // 角色列表
) {
    companion object {
        /**
         * 从用户聚合根创建DTO
         */
        fun fromUser(user: User, permissions: List<String> = emptyList(), roles: List<String> = emptyList()): UserPermissionsDTO {
            return UserPermissionsDTO(
                userId = user.id.getValue(),
                canLogin = user.canLogin(),
                canPerformActions = user.canPerformActions(),
                needsPasswordUpdate = user.needsPasswordUpdate(),
                accountLocked = user.getStatus().isLocked(),
                emailVerified = user.isEmailVerified(),
                permissions = permissions,
                roles = roles
            )
        }
    }
}

/**
 * 用户统计DTO
 * 用于返回用户相关的统计信息
 */
data class UserStatisticsDTO(
    val totalUsers: Long,
    val activeUsers: Long,
    val pendingUsers: Long,
    val lockedUsers: Long,
    val deletedUsers: Long,
    val verifiedUsers: Long,
    val unverifiedUsers: Long,
    val newUsersToday: Long,
    val newUsersThisWeek: Long,
    val newUsersThisMonth: Long,
    val activeUsersToday: Long,
    val activeUsersThisWeek: Long,
    val activeUsersThisMonth: Long,
    val averageLoginFrequency: Double, // 平均登录频率（天）
    val passwordExpiringUsers: Long, // 密码即将过期的用户数
    val inactiveUsers: Long // 长期未登录的用户数
)

/**
 * 用户偏好设置DTO
 * 用于返回用户的个性化配置
 */
data class UserPreferencesDTO(
    val userId: String,
    val language: String,
    val timezone: String,
    val theme: String,
    val dateFormat: String,
    val notificationSettings: NotificationSettingsDTO,
    val updatedAt: LocalDateTime
) {
    companion object {
        /**
         * 从用户聚合根创建DTO
         */
        fun fromUser(user: User): UserPreferencesDTO {
            val preferences = user.getPreferences()
            val notificationSettings = preferences.getNotificationSettings()
            
            return UserPreferencesDTO(
                userId = user.id.getValue(),
                language = preferences.getLanguage().toString(),
                timezone = preferences.getTimezone().toString(),
                theme = preferences.getTheme().name,
                dateFormat = preferences.getDateFormat().name,
                notificationSettings = NotificationSettingsDTO(
                    emailNotifications = true, // TODO: 从用户偏好中获取
                    pushNotifications = true, // TODO: 从用户偏好中获取
                    smsNotifications = false, // TODO: 从用户偏好中获取
                    marketingEmails = false, // TODO: 从用户偏好中获取
                    securityAlerts = true // TODO: 从用户偏好中获取
                ),
                updatedAt = user.getUpdatedAt()
            )
        }
    }
}

/**
 * 通知设置DTO
 */
data class NotificationSettingsDTO(
    val emailNotifications: Boolean,
    val pushNotifications: Boolean,
    val smsNotifications: Boolean,
    val marketingEmails: Boolean,
    val securityAlerts: Boolean
)

/**
 * 认证令牌DTO
 * 用于返回用户认证相关信息
 */
data class AuthTokenDTO(
    val userId: String,
    val accessToken: String,
    val refreshToken: String?,
    val tokenType: String = "Bearer",
    val expiresIn: Long, // 过期时间（秒）
    val scope: String?,
    val issuedAt: LocalDateTime,
    val expiresAt: LocalDateTime
)

/**
 * 用户搜索结果DTO
 * 用于返回搜索结果
 */
data class UserSearchResultDTO(
    val users: List<UserSummaryDTO>,
    val totalCount: Long,
    val pageSize: Int,
    val currentPage: Int,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    companion object {
        /**
         * 创建搜索结果DTO
         */
        fun create(
            users: List<User>,
            totalCount: Long,
            pageSize: Int,
            currentPage: Int
        ): UserSearchResultDTO {
            val totalPages = ((totalCount + pageSize - 1) / pageSize).toInt()
            
            return UserSearchResultDTO(
                users = users.map { UserSummaryDTO.fromUser(it) },
                totalCount = totalCount,
                pageSize = pageSize,
                currentPage = currentPage,
                totalPages = totalPages,
                hasNext = currentPage < totalPages,
                hasPrevious = currentPage > 1
            )
        }
    }
}

/**
 * 用户活动记录DTO
 * 用于返回用户的活动历史
 */
data class UserActivityDTO(
    val userId: String,
    val activityType: String,
    val description: String,
    val ipAddress: String?,
    val userAgent: String?,
    val timestamp: LocalDateTime,
    val metadata: Map<String, Any>? = null
)

/**
 * 用户安全报告DTO
 * 用于返回用户的安全状态
 */
data class UserSecurityReportDTO(
    val userId: String,
    val securityLevel: String,
    val securityScore: Int, // 安全评分（0-100）
    val issues: List<SecurityIssueDTO>,
    val recommendations: List<String>,
    val lastSecurityCheck: LocalDateTime
)

/**
 * 安全问题DTO
 */
data class SecurityIssueDTO(
    val type: String,
    val severity: String, // HIGH, MEDIUM, LOW
    val description: String,
    val recommendation: String
)