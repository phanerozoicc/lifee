package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.Email
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 用户登录失败事件
 */
class UserLoginFailedEvent(
    val userId: UserId?,
    val email: Email,
    val reason: String,
    val attemptTime: Instant,
    val ipAddress: String,
    val userAgent: String? = null,
    aggregateId: String = userId?.value ?: email.value,
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
        return UserLoginFailedEvent(
            userId = this.userId,
            email = this.email,
            reason = this.reason,
            attemptTime = this.attemptTime,
            ipAddress = this.ipAddress,
            userAgent = this.userAgent,
            aggregateId = aggregateId,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserLoginFailedEvent) return false
        if (!super.equals(other)) return false
        return userId == other.userId &&
               email == other.email &&
               reason == other.reason &&
               attemptTime == other.attemptTime &&
               ipAddress == other.ipAddress &&
               userAgent == other.userAgent
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (userId?.hashCode() ?: 0)
        result = 31 * result + email.hashCode()
        result = 31 * result + reason.hashCode()
        result = 31 * result + attemptTime.hashCode()
        result = 31 * result + ipAddress.hashCode()
        result = 31 * result + (userAgent?.hashCode() ?: 0)
        return result
    }
    
    override fun toString(): String {
        return "UserLoginFailedEvent(userId=$userId, email=$email, reason=$reason, attemptTime=$attemptTime, ipAddress=$ipAddress, userAgent=$userAgent, ${super.toString()})"
    }
}