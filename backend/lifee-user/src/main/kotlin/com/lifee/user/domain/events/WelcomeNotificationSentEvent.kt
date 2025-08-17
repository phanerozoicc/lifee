package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.Email
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 欢迎通知发送完成事件
 */
class WelcomeNotificationSentEvent(
    val userId: UserId,
    val email: Email,
    val firstName: String?,
    val lastName: String?,
    val sentAt: Instant = Instant.now(),
    val notificationType: String = "welcome_email"
) : DomainEvent(userId.value) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return WelcomeNotificationSentEvent(
            userId = UserId(aggregateId),
            email = this.email,
            firstName = this.firstName,
            lastName = this.lastName,
            sentAt = this.sentAt,
            notificationType = this.notificationType
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WelcomeNotificationSentEvent) return false
        if (!super.equals(other)) return false
        return userId == other.userId &&
               email == other.email &&
               firstName == other.firstName &&
               lastName == other.lastName &&
               sentAt == other.sentAt &&
               notificationType == other.notificationType
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + (firstName?.hashCode() ?: 0)
        result = 31 * result + (lastName?.hashCode() ?: 0)
        result = 31 * result + sentAt.hashCode()
        result = 31 * result + notificationType.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "WelcomeNotificationSentEvent(userId=$userId, email=$email, firstName=$firstName, lastName=$lastName, sentAt=$sentAt, notificationType=$notificationType, ${super.toString()})"
    }
}