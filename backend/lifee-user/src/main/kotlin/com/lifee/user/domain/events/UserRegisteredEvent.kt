package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.Email
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 用户注册事件
 */
class UserRegisteredEvent(
    val userId: UserId,
    val email: Email,
    val firstName: String,
    val lastName: String,
    val registeredAt: Instant,
    val activationToken: String? = null
) : DomainEvent(userId.value) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return UserRegisteredEvent(
            userId = UserId(aggregateId),
            email = this.email,
            firstName = this.firstName,
            lastName = this.lastName,
            registeredAt = this.registeredAt,
            activationToken = this.activationToken
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserRegisteredEvent) return false
        if (!super.equals(other)) return false
        return userId == other.userId &&
               email == other.email &&
               firstName == other.firstName &&
               lastName == other.lastName &&
               registeredAt == other.registeredAt &&
               activationToken == other.activationToken
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + firstName.hashCode()
        result = 31 * result + lastName.hashCode()
        result = 31 * result + registeredAt.hashCode()
        result = 31 * result + (activationToken?.hashCode() ?: 0)
        return result
    }
    
    override fun toString(): String {
        return "UserRegisteredEvent(userId=$userId, email=$email, firstName=$firstName, lastName=$lastName, registeredAt=$registeredAt, activationToken=$activationToken, ${super.toString()})"
    }
}