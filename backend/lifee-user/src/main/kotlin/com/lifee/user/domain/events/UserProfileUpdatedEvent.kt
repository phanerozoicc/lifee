package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import com.lifee.user.domain.UserProfile
import java.time.Instant
import java.util.*

/**
 * 用户档案更新事件
 */
class UserProfileUpdatedEvent(
    val userId: UserId,
    val updatedFields: Map<String, Any>,
    val updatedAt: Instant,
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
        return UserProfileUpdatedEvent(
            userId = UserId(aggregateId),
            updatedFields = this.updatedFields,
            updatedAt = this.updatedAt,
            aggregateId = aggregateId,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserProfileUpdatedEvent) return false
        if (!super.equals(other)) return false
        return userId == other.userId &&
               updatedFields == other.updatedFields &&
               updatedAt == other.updatedAt
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + updatedFields.hashCode()
        result = 31 * result + updatedAt.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "UserProfileUpdatedEvent(userId=$userId, updatedFields=$updatedFields, updatedAt=$updatedAt, ${super.toString()})"
    }
}