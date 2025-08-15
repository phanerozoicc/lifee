package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.user.domain.model.UserId
import java.time.Instant
import java.util.*

/**
 * 用户邮箱验证事件
 * 当用户邮箱验证状态发生变更时发布
 */
data class UserActivatedEvent(
    val userId: UserId,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserActivated"
) : DomainEvent(userId.value, "UserActivated")