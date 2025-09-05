package com.github.phanerozoicc.user.application.event

import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.base.event.EventHandler
import com.github.phanerozoicc.user.application.service.EmailService
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.UserId
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

/**
 * 用户注册事件
 * 当新用户成功注册时发布
 */
class UserRegisteredEvent(
    val userId: UserId,
    val email: Email,
    val nickname: String,
    val registrationTime: LocalDateTime = LocalDateTime.now(),
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val activationToken: String? = null,
    aggregateId: String = userId.value,
    version: Long = 0,
    occurredOn: Instant = Instant.now(),
    eventId: String = UUID.randomUUID().toString(),
    override val eventType: String = "UserRegistered"
    ) : DomainEvent(aggregateId, version, eventId, occurredOn) {
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: String
    ): DomainEvent {
        return UserRegisteredEvent(
            userId,
            email,
            nickname,
            registrationTime,
            ipAddress,
            userAgent,
            activationToken,
            aggregateId,
            version,
            occurredOn,
            eventId
        )
    }

    fun copy(activationToken: String): UserRegisteredEvent {
        return UserRegisteredEvent(
            userId,
            email,
            nickname,
            registrationTime,
            ipAddress,
            userAgent,
            activationToken,
            aggregateId,
            version,
            occurredOn,
            eventId
        )
    }

}

@Component
class UserRegisteredEventHandler(
    private val emailService: EmailService
): EventHandler<UserRegisteredEvent, Unit> {

    companion object: KLogging()

    override fun onEvent(event: UserRegisteredEvent) {
        logger.info("处理用户注册事件: userId={}, email={}", event.userId, event.email)
        try {
            runBlocking {
                emailService.sendActivationEmail(
                    userId = event.userId,
                    email = event.email,
                    activationToken = event.activationToken!!,
                    nickname = event.nickname,
                )
            }
            logger.info("发送激活邮件成功: userId={}, email={}", event.userId, event.email)
        } catch (e: Exception) {
            logger.error("发送激活邮件失败: userId={}, email={}", event.userId, event.email, e)
            throw e
        }
    }

}
