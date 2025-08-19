package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 用户激活事件
 */
class UserActivatedEvent(
    val userId: UserId,
    val activatedAt: Instant,
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
        return UserActivatedEvent(
            userId = UserId(aggregateId),
            activatedAt = this.activatedAt,
            aggregateId = aggregateId,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserActivatedEvent) return false
        if (!super.equals(other)) return false
        return userId == other.userId &&
               activatedAt == other.activatedAt
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + activatedAt.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "UserActivatedEvent(userId=$userId, activatedAt=$activatedAt, ${super.toString()})"
    }
}