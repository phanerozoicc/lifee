package com.lifee.config.app.handlers

import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.cqrs.events.EventHandler
import com.lifee.common.cqrs.events.Idempotent
import com.lifee.common.cqrs.events.IdempotentKeyStrategy
import com.lifee.user.domain.events.UserRegisteredEvent
import com.lifee.config.domain.events.UserConfigurationInitializedEvent
import com.lifee.config.app.services.UserConfigurationService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 用户注册事件处理器 - Config模块
 * 负责为新注册用户初始化配置数据
 */
@Component
class UserRegisteredEventHandler(
    private val userConfigurationService: UserConfigurationService,
    private val eventBus: EventBus
) : EventHandler<UserRegisteredEvent> {
    
    private val logger = LoggerFactory.getLogger(UserRegisteredEventHandler::class.java)
    
    @Idempotent(keyStrategy = IdempotentKeyStrategy.AGGREGATE_EVENT_TYPE)
    override fun handle(event: UserRegisteredEvent) {
        logger.info("Config模块处理用户注册事件: userId={}, email={}", event.userId.value, event.email.value)
        
        try {
            runBlocking {
                // 初始化用户配置
                userConfigurationService.initializeUserConfiguration(
                    userId = event.userId,
                    email = event.email.value,
                    firstName = event.firstName,
                    lastName = event.lastName
                )
                
                // 发布配置初始化完成事件
                val configInitializedEvent = UserConfigurationInitializedEvent(
                    userId = event.userId,
                    email = event.email.value,
                    firstName = event.firstName,
                    lastName = event.lastName
                )
                eventBus.publish(configInitializedEvent)
            }
            
            logger.info("Config模块用户注册事件处理完成: userId={}", event.userId.value)
        } catch (e: Exception) {
            logger.error("Config模块处理用户注册事件失败: userId={}", event.userId.value, e)
            // 实现重试机制或补偿逻辑
            throw e
        }
    }
}