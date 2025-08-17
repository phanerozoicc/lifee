package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 用户密码更改事件
 */
class UserPasswordChangedEvent(
    val userId: UserId
) : DomainEvent(userId.value) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return UserPasswordChangedEvent(
            userId = UserId(aggregateId)
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