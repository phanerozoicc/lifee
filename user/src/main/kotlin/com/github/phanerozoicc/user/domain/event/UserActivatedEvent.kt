package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.base.event.EventHandler
import com.github.phanerozoicc.user.application.service.EmailService
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.repository.UserRepository
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

/**
 * 用户邮箱验证事件
 * 当用户邮箱验证状态发生变更时发布
 */
class UserActivatedEvent(
    val userId: UserId,
    override val eventType: String = "UserActivated",
    version: Long = 0,
    occurredOn: Instant = Instant.now(),
    eventId: String = UUID.randomUUID().toString()
) : DomainEvent(userId.value, version, eventId, occurredOn) {
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant
    ): DomainEvent {
        return UserActivatedEvent(
            UserId(aggregateId),
            eventType,
            version,
            occurredOn,
            eventId
        )
    }
}


@Component
class UserActivatedEventHandler(
    private val emailService: EmailService,
    private val userRepository: UserRepository
): EventHandler<UserActivatedEvent, Unit> {

    companion object: KLogging()

    override fun onEvent(event: UserActivatedEvent) {
        logger.debug("handle user activate event, userId:{}", event.userId)
        try {
            runBlocking {
                val user = userRepository.findById(event.userId)
                if (user!=null) {
                    emailService.sendWelcomeEmail(
                        userId = event.userId,
                        email = user.getEmail(),
                        nickname = user.getProfile().nickname
                    )
                    logger.info("send welcome email to user, userId:{}", event.userId)
                } else {
                    logger.warn("handle user activate event, user not found, userId:{}", event.userId)
                }
            }
        } catch (e: Exception) {
            logger.error("handle user activate event error, userId:{}", event.userId, e)
        }
    }

}