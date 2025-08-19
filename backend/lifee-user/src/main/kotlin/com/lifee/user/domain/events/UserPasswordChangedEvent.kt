package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 用户密码更改事件
 */
class UserPasswordChangedEvent(
    val userId: UserId,
    val changedAt: Instant,
    aggregateId: String = userId.value,
    version: Long = 0,
    occurredOn: Instant = Instant.now(),
    eventId: UUID = UUID.randomUUID()
) : DomainEvent(aggregateId, version, occurredOn, eventId) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return UserPasswordChangedEvent(
            userId = UserId(aggregateId),
            changedAt = this.changedAt,
            aggregateId = aggregateId,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserPasswordChangedEvent) return false
        if (!super.equals(other)) return false
        return userId == other.userId
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + userId.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "UserPasswordChangedEvent(userId=$userId, ${super.toString()})"
    }
}