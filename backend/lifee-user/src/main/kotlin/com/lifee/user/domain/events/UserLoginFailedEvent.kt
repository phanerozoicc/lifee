package com.lifee.user.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 用户登录失败事件
 */
class UserLoginFailedEvent(
    val userId: UserId?,
    val email: String,
    val ipAddress: String?,
    val userAgent: String?,
    val failureReason: String,
    val loginAt: Instant
) : DomainEvent(userId?.value ?: email) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return UserLoginFailedEvent(
            userId = if (aggregateId.isNotEmpty()) UserId(aggregateId) else null,
            email = this.email,
            ipAddress = this.ipAddress,
            userAgent = this.userAgent,
            failureReason = this.failureReason,
            loginAt = this.loginAt
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UserLoginFailedEvent) return false
        if (!super.equals(other)) return false
        return userId == other.userId &&
               email == other.email &&
               ipAddress == other.ipAddress &&
               userAgent == other.userAgent &&
               failureReason == other.failureReason &&
               loginAt == other.loginAt
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (userId?.hashCode() ?: 0)
        result = 31 * result + email.hashCode()
        result = 31 * result + (ipAddress?.hashCode() ?: 0)
        result = 31 * result + (userAgent?.hashCode() ?: 0)
        result = 31 * result + failureReason.hashCode()
        result = 31 * result + loginAt.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "UserLoginFailedEvent(userId=$userId, email=$email, ipAddress=$ipAddress, userAgent=$userAgent, failureReason=$failureReason, loginAt=$loginAt, ${super.toString()})"
    }
}