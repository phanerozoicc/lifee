package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

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
) : DomainEvent(userId?.value ?: email) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return this.copy(
            userId = if (aggregateId != this.email) UserId(aggregateId) else this.userId
        )
    }
}