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
    val oldProfile: UserProfile,
    val newProfile: UserProfile
) : DomainEvent(userId.value) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return UserProfileUpdatedEvent(
            userId = UserId(aggregateId),
            oldProfile = this.oldProfile,
            newProfile = this.newProfile
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserProfileUpdatedEvent) return false
        if (!super.equals(other)) return false
        return userId == other.userId &&
               oldProfile == other.oldProfile &&
               newProfile == other.newProfile
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + oldProfile.hashCode()
        result = 31 * result + newProfile.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "UserProfileUpdatedEvent(userId=$userId, oldProfile=$oldProfile, newProfile=$newProfile, ${super.toString()})"
    }
}