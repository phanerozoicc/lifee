package com.github.phanerozoicc.user.application.handlers

import com.github.phanerozoicc.base.event.DomainEventHandler
import com.github.phanerozoicc.user.application.service.EmailService
import com.github.phanerozoicc.user.domain.event.UserRegisteredEvent
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class UserRegisteredEventHandler(
    private val emailService: EmailService
): DomainEventHandler<UserRegisteredEvent, Unit> {

    companion object: KLogging()

    override fun onDomainEvent(event: UserRegisteredEvent) {
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
