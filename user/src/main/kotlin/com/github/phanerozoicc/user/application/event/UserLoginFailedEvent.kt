package com.github.phanerozoicc.user.application.event

import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.UserId
import java.time.Instant
import java.time.LocalDateTime
import java.util.*


/**
 * 用户登录失败事件
 * 当用户登录失败时发布（用于安全监控）
 */
class UserLoginFailedEvent(
    val userId: UserId,
    val email: Email,
    val failureReason: String,
    val attemptTime: LocalDateTime = LocalDateTime.now(),
    val ipAddress: String? = null,
    val userAgent: String? = null,
    version: Long = 0,
    eventId: String = UUID.randomUUID().toString(),
    occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserLoggedIn"
) : DomainEvent(userId.value, version, eventId, occurredOn) {
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: String
    ): DomainEvent {
        return UserLoginFailedEvent(
            userId,
            email,
            failureReason,
            attemptTime,
            ipAddress,
            userAgent,
            version,
            eventId,
            occurredOn
        )
    }
}


//@Component
class UserLoginFailedEventHandler(

) {

    fun onEvent(event: UserLoginFailedEvent) {
        // 处理用户登录失败事件的逻辑
        // 例如，记录日志、触发安全警报等
        // 这里暂时什么都不做
    }
}
