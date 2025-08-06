package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.base.domain.DomainEvent
import com.github.phanerozoicc.user.domain.model.Email
import java.time.Instant
import java.time.LocalDateTime
import java.util.*


/**
 * 用户登录失败事件
 * 当用户登录失败时发布（用于安全监控）
 */
data class UserLoginFailed(
    val email: Email,
    val failureReason: String,
    val attemptTime: LocalDateTime = LocalDateTime.now(),
    val ipAddress: String? = null,
    val userAgent: String? = null,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserLoginFailed"
) : DomainEvent
