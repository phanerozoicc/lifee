package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 用户登录成功事件
 */
class UserLoginSuccessEvent(
    val userId: UserId,
    val loginTime: Instant,
    val ipAddress: String,
    val userAgent: String? = null,
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
        return UserLoginSuccessEvent(
            userId = UserId(aggregateId),
            loginTime = this.loginTime,
            ipAddress = this.ipAddress,
            userAgent = this.userAgent,
            aggregateId = aggregateId,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserLoginSuccessEvent) return false
        if (!super.equals(other)) return false
        return userId == other.userId &&
               loginTime == other.loginTime &&
               ipAddress == other.ipAddress &&
               userAgent == other.userAgent
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + loginTime.hashCode()
        result = 31 * result + ipAddress.hashCode()
        result = 31 * result + (userAgent?.hashCode() ?: 0)
        return result
    }
    
    override fun toString(): String {
        return "UserLoginSuccessEvent(userId=$userId, loginTime=$loginTime, ipAddress=$ipAddress, userAgent=$userAgent, ${super.toString()})"
    }
}