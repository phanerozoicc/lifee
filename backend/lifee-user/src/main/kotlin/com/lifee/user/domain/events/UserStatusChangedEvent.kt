package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import com.lifee.user.domain.UserStatus
import java.time.Instant
import java.util.*

/**
 * 用户状态更改事件
 */
data class UserStatusChangedEvent(
    val userId: UserId,
    val oldStatus: UserStatus,
    val newStatus: UserStatus,
    val reason: String? = null
) : DomainEvent(userId.value) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return this.copy(
            userId = UserId(aggregateId)
        )
    }
}