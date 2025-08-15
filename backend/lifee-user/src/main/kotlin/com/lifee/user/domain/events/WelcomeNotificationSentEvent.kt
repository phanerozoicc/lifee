package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.Email
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 欢迎通知发送完成事件
 */
data class WelcomeNotificationSentEvent(
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
        return this.copy(
            userId = UserId(aggregateId)
        )
    }
}