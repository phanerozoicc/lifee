package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import com.lifee.user.domain.UserStatus
import java.time.Instant
import java.util.*

/**
 * 用户状态更改事件
 */
class UserStatusChangedEvent(
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
        return UserStatusChangedEvent(
            userId = UserId(aggregateId),
            oldStatus = this.oldStatus,
            newStatus = this.newStatus,
            reason = this.reason
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserStatusChangedEvent) return false
        if (!super.equals(other)) return false
        return userId == other.userId &&
               oldStatus == other.oldStatus &&
               newStatus == other.newStatus &&
               reason == other.reason
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + oldStatus.hashCode()
        result = 31 * result + newStatus.hashCode()
        result = 31 * result + (reason?.hashCode() ?: 0)
        return result
    }
    
    override fun toString(): String {
        return "UserStatusChangedEvent(userId=$userId, oldStatus=$oldStatus, newStatus=$newStatus, reason=$reason, ${super.toString()})"
    }
}