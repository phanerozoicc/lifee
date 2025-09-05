package com.github.phanerozoicc.user.application.event

import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.UserId
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

/**
 * 用户登录事件
 * 当用户成功登录时发布
 */
class UserLoggedInEvent(
    val userId: UserId,
    val email: Email,
    val loginTime: LocalDateTime = LocalDateTime.now(),
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val sessionId: String? = null,
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
        return UserLoggedInEvent(
            userId,
            email,
            loginTime,
            ipAddress,
            userAgent,
            sessionId,
            version,
            eventId,
            occurredOn
        )
    }
}


//@Component
class UserLoggedInEventHandler(

) {

    fun onEvent(event: UserLoggedInEvent) {
        // 处理用户登录事件的逻辑
        // 例如，记录日志、更新最后登录时间等
        // 这里暂时什么都不做
    }
}