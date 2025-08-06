package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.base.domain.DomainEvent
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.UserId
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

/**
 * 用户注册事件
 * 当新用户成功注册时发布
 */
data class UserRegistered(
    val userId: UserId,
    val email: Email,
    val nickname: String,
    val registrationTime: LocalDateTime = LocalDateTime.now(),
    val ipAddress: String? = null,
    val userAgent: String? = null,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserRegistered"
) : DomainEvent

