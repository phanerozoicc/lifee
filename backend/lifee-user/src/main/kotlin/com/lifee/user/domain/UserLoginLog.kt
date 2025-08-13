package com.lifee.user.domain

import java.time.Instant
import java.util.*

/**
 * 用户登录日志
 */
data class UserLoginLog(
    private val id: UserLoginLogId,
    private val userId: UserId,
    private val email: String,
    private val ipAddress: String?,
    private val userAgent: String?,
    private val loginResult: LoginResult,
    private val failureReason: String? = null,
    private val createdAt: Instant = Instant.now()
) {
    
    fun getId(): UserLoginLogId = id
    fun getUserId(): UserId = userId
    fun getEmail(): String = email
    fun getIpAddress(): String? = ipAddress
    fun getUserAgent(): String? = userAgent
    fun getLoginResult(): LoginResult = loginResult
    fun getFailureReason(): String? = failureReason
    fun getCreatedAt(): Instant = createdAt
    
    companion object {
        /**
         * 创建成功登录日志
         */
        fun createSuccessLog(
            userId: UserId,
            email: String,
            ipAddress: String?,
            userAgent: String?
        ): UserLoginLog {
            return UserLoginLog(
                id = UserLoginLogId.generate(),
                userId = userId,
                email = email,
                ipAddress = ipAddress,
                userAgent = userAgent,
                loginResult = LoginResult.SUCCESS
            )
        }
        
        /**
         * 创建失败登录日志
         */
        fun createFailureLog(
            email: String,
            ipAddress: String?,
            userAgent: String?,
            loginResult: LoginResult,
            failureReason: String? = null,
            userId: UserId? = null
        ): UserLoginLog {
            return UserLoginLog(
                id = UserLoginLogId.generate(),
                userId = userId ?: UserId.fromString("U99999999"), // 如果用户不存在，使用临时ID
                email = email,
                ipAddress = ipAddress,
                userAgent = userAgent,
                loginResult = loginResult,
                failureReason = failureReason
            )
        }
    }
}

/**
 * 用户登录日志ID
 */
@JvmInline
value class UserLoginLogId(val value: String) {
    companion object {
        fun generate(): UserLoginLogId = UserLoginLogId(UUID.randomUUID().toString())
        fun of(value: String): UserLoginLogId = UserLoginLogId(value)
    }
}

/**
 * 登录结果枚举
 */
enum class LoginResult {
    SUCCESS,                    // 登录成功
    FAILED_INVALID_CREDENTIALS, // 凭据无效（用户名或密码错误）
    FAILED_ACCOUNT_LOCKED,      // 账户被锁定
    FAILED_ACCOUNT_DISABLED     // 账户被禁用
}