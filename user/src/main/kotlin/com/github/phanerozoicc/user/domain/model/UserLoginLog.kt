package com.github.phanerozoicc.user.domain.model

import java.time.Instant

data class UserLoginLog(
    val id: UserLoginLogId,
    val userId: UserId,
    val email: String,
    val ipAddress: String?,
    val userAgent: String?,
    val loginResult: LoginResult,
    val failureReason: String? = null,
    val createdAt: Instant = Instant.now()
) {
    companion object {

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
                userId = userId ?: UserId.of("unknown"),
                email = email,
                ipAddress = ipAddress,
                userAgent = userAgent,
                loginResult = loginResult,
                failureReason = failureReason
            )
        }
    }
    
}

data class UserLoginLogId(val value: String) {
    companion object {
        fun generate(): UserLoginLogId {
            return UserLoginLogId(java.util.UUID.randomUUID().toString())
        }
        fun from(value: String): UserLoginLogId {
            return UserLoginLogId(value)
        }
    }
}


enum class LoginResult {
    // 登录成功
    SUCCESS,
    // 登录失败 - 用户不存在
    USER_NOT_FOUND,
    // 登录失败 - 密码错误
    INVALID_PASSWORD,
    // 登录失败 - 用户被锁定
    USER_LOCKED,
    // 登录失败 - 用户未激活
    USER_NOT_ACTIVE,
    // 登录失败 - 其他原因
    OTHER_FAILURE
}