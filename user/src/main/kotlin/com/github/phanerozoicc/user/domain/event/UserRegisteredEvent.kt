package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.UserId
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

/**
 * 用户注册事件
 * 当新用户成功注册时发布
 */
class UserRegisteredEvent(
    val userId: UserId,
    val email: Email,
    val nickname: String,
    val registrationTime: LocalDateTime = LocalDateTime.now(),
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val activationToken: String? = null,
    aggregateId: String = userId.value,
    version: Long = 0,
    occurredOn: Instant = Instant.now(),
    eventId: String = UUID.randomUUID().toString()
    ) : DomainEvent(aggregateId, version) {
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: String
    ): DomainEvent {
        return UserRegisteredEvent(
            userId,
            email,
            nickname,
            registrationTime,
            ipAddress,
            userAgent,
            activationToken,
            aggregateId,
            version,
            occurredOn,
            eventId
        )
    }

    fun copy(activationToken: String): UserRegisteredEvent {
        return UserRegisteredEvent(
            userId,
            email,
            nickname,
            registrationTime,
            ipAddress,
            userAgent,
            activationToken,
            aggregateId,
            version,
            occurredOn,
            eventId
        )
    }

}

