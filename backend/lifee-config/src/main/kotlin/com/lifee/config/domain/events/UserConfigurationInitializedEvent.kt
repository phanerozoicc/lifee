package com.lifee.config.domain.events

import com.lifee.common.domain.DomainEvent
import java.time.Instant
import java.util.*

/**
 * 用户配置初始化完成事件
 */
class UserConfigurationInitializedEvent(
    val userId: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val initializedAt: Instant = Instant.now(),
    aggregateId: String = userId,
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
        return UserConfigurationInitializedEvent(
            userId = this.userId,
            email = this.email,
            firstName = this.firstName,
            lastName = this.lastName,
            initializedAt = this.initializedAt,
            aggregateId = aggregateId,
            version = version,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserConfigurationInitializedEvent) return false
        if (!super.equals(other)) return false
        return userId == other.userId &&
               email == other.email &&
               firstName == other.firstName &&
               lastName == other.lastName &&
               initializedAt == other.initializedAt
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + (firstName?.hashCode() ?: 0)
        result = 31 * result + (lastName?.hashCode() ?: 0)
        result = 31 * result + initializedAt.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "UserConfigurationInitializedEvent(userId=$userId, email=$email, firstName=$firstName, lastName=$lastName, initializedAt=$initializedAt, ${super.toString()})"
    }
}