package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.base.domain.DomainEvent
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.UserId
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

/**
 * 用户邮箱验证事件
 * 当用户邮箱验证状态发生变更时发布
 */
data class UserEmailVerified(
    val userId: UserId,
    val email: Email,
    val verificationTime: LocalDateTime = LocalDateTime.now(),
    val verificationToken: String? = null,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserEmailVerified"
) : DomainEvent