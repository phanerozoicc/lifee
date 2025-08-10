package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 用户登录失败事件
 */
data class UserLoginFailedEvent(
    val userId: UserId?,
    val email: String,
    val ipAddress: String?,
    val userAgent: String?,
    val failureReason: String,
    val loginAt: Instant
) : DomainEvent(userId?.value ?: email)