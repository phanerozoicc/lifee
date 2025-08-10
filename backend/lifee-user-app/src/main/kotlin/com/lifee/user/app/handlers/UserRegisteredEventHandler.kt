package com.lifee.user.app.handlers

import com.lifee.common.cqrs.events.EventHandler
import com.lifee.user.domain.events.UserRegisteredEvent
import com.lifee.user.domain.services.EmailService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 用户注册事件处理器
 * 负责发送激活邮件，其他模块初始化由各自模块的事件处理器处理
 */
@Component
class UserRegisteredEventHandler(
    private val emailService: EmailService
) : EventHandler<UserRegisteredEvent> {
    
    private val logger = LoggerFactory.getLogger(UserRegisteredEventHandler::class.java)
    
    override fun handle(event: UserRegisteredEvent) {
        logger.info("处理用户注册事件: userId={}, email={}", event.userId.value, event.email.value)
        
        try {
            runBlocking {
                // 发送激活邮件
                emailService.sendActivationEmail(
                    userId = event.userId,
                    email = event.email,
                    firstName = event.firstName,
                    lastName = event.lastName,
                    activationToken = event.activationToken
                )
            }
            
            logger.info("用户注册事件处理完成（激活邮件已发送）: userId={}", event.userId.value)
        } catch (e: Exception) {
            logger.error("处理用户注册事件失败: userId={}", event.userId.value, e)
            // 这里可以考虑重试机制或者将失败的任务放入队列
            throw e
        }
    }
}