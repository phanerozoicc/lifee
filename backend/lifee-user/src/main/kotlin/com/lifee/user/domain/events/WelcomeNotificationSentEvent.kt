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
    val notificationChannel: String,
    val message: String,
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
        return WelcomeNotificationSentEvent(
            userId = UserId(aggregateId),
            notificationChannel = this.notificationChannel,
            message = this.message,
            aggregateId = aggregateId,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WelcomeNotificationSentEvent) return false
        if (!super.equals(other)) return false
        return userId == other.userId &&
               notificationChannel == other.notificationChannel &&
               message == other.message
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + notificationChannel.hashCode()
        result = 31 * result + message.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "WelcomeNotificationSentEvent(userId=$userId, notificationChannel=$notificationChannel, message=$message, ${super.toString()})"
    }
}