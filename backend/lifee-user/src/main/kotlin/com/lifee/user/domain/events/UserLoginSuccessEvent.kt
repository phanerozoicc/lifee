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
    val email: String,
    val ipAddress: String?,
    val userAgent: String?,
    val loginAt: Instant
) : DomainEvent(userId.value) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return UserLoginSuccessEvent(
            userId = UserId(aggregateId),
            email = this.email,
            ipAddress = this.ipAddress,
            userAgent = this.userAgent,
            loginAt = this.loginAt
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserLoginSuccessEvent) return false
        if (!super.equals(other)) return false
        return userId == other.userId &&
               email == other.email &&
               loginAt == other.loginAt &&
               ipAddress == other.ipAddress &&
               userAgent == other.userAgent
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + email.hashCode()
        result = 31 * result + loginAt.hashCode()
        result = 31 * result + (ipAddress?.hashCode() ?: 0)
        result = 31 * result + (userAgent?.hashCode() ?: 0)
        return result
    }
    
    override fun toString(): String {
        return "UserLoginSuccessEvent(userId=$userId, email=$email, loginAt=$loginAt, ipAddress=$ipAddress, userAgent=$userAgent, ${super.toString()})"
    }
}