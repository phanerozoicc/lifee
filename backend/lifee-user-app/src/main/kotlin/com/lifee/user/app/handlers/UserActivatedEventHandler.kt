package com.lifee.user.app.handlers

import com.lifee.common.cqrs.events.EventHandler
import com.lifee.user.domain.events.UserActivatedEvent
import com.lifee.user.domain.services.EmailService
import com.lifee.user.domain.UserRepository
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 用户激活事件处理器
 */
@Component
class UserActivatedEventHandler(
    private val emailService: EmailService,
    private val userRepository: UserRepository
) : EventHandler<UserActivatedEvent> {
    
    private val logger = LoggerFactory.getLogger(UserActivatedEventHandler::class.java)
    
    override fun handle(event: UserActivatedEvent) {
        logger.info("处理用户激活事件: userId={}", event.userId.value)
        
        try {
            runBlocking {
                // 获取用户信息
                val user = userRepository.findById(event.userId)
                if (user != null) {
                    // 发送欢迎邮件
                    emailService.sendWelcomeEmail(
                        userId = user.getId(),
                        email = user.getEmail(),
                        firstName = user.getProfile().firstName,
                        lastName = user.getProfile().lastName
                    )
                    
                    logger.info("用户激活事件处理完成: userId={}", event.userId.value)
                } else {
                    logger.warn("用户激活事件处理失败，用户不存在: userId={}", event.userId.value)
                }
            }
        } catch (e: Exception) {
            logger.error("处理用户激活事件失败: userId={}", event.userId.value, e)
        }
    }
}