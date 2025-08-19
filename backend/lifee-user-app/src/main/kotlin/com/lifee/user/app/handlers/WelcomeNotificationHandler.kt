package com.lifee.user.app.handlers

import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.cqrs.events.EventHandler
import com.lifee.common.cqrs.events.Idempotent
import com.lifee.common.cqrs.events.IdempotentKeyStrategy
import com.lifee.knowledge.domain.events.DefaultKnowledgeBaseCreatedEvent
import com.lifee.user.domain.events.WelcomeNotificationSentEvent
import com.lifee.user.domain.services.EmailService
import com.lifee.user.domain.Email
import com.lifee.user.domain.UserId
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 欢迎通知处理器
 * 在默认知识库创建完成后发送欢迎通知
 */
@Component
class WelcomeNotificationHandler(
    private val emailService: EmailService,
    private val eventBus: EventBus
) : EventHandler<DefaultKnowledgeBaseCreatedEvent> {
    
    private val logger = LoggerFactory.getLogger(WelcomeNotificationHandler::class.java)
    
    @Idempotent(keyStrategy = IdempotentKeyStrategy.AGGREGATE_EVENT_TYPE)
    override fun handle(event: DefaultKnowledgeBaseCreatedEvent) {
        logger.info("处理默认知识库创建完成事件，发送欢迎通知: userId={}, email={}", event.userId.value, event.email)
        
        try {
            runBlocking {
                // 发送欢迎邮件
                emailService.sendWelcomeEmail(
                    userId = UserId(event.userId.value),
                    email = Email.of(event.email),
                    firstName = event.firstName ?: "",
                    lastName = event.lastName ?: ""
                )
                
                // 发布欢迎通知发送完成事件
                val welcomeNotificationEvent = WelcomeNotificationSentEvent(
                    userId = UserId(event.userId.value),
                    notificationChannel = "email",
                    message = "Welcome to Lifee! Your account has been successfully created."
                )
                eventBus.publish(welcomeNotificationEvent)
            }
            
            logger.info("欢迎通知发送完成: userId={}", event.userId.value)
        } catch (e: Exception) {
            logger.error("发送欢迎通知失败: userId={}", event.userId.value, e)
            throw e
        }
    }
}