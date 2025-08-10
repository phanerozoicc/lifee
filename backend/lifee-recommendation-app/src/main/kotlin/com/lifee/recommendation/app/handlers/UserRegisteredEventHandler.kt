package com.lifee.recommendation.app.handlers

import com.lifee.common.cqrs.events.EventHandler
import com.lifee.common.cqrs.events.Idempotent
import com.lifee.common.cqrs.events.IdempotentKeyStrategy
import com.lifee.user.domain.events.UserRegisteredEvent
import com.lifee.recommendation.app.services.UserRecommendationService
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 用户注册事件处理器 - Recommendation模块
 * 负责为新注册用户初始化推荐数据
 */
@Component
class UserRegisteredEventHandler(
    private val userRecommendationService: UserRecommendationService
) : EventHandler<UserRegisteredEvent> {
    
    private val logger = LoggerFactory.getLogger(UserRegisteredEventHandler::class.java)
    
    @Idempotent(keyStrategy = IdempotentKeyStrategy.AGGREGATE_EVENT_TYPE)
    override fun handle(event: UserRegisteredEvent) {
        logger.info("Recommendation模块处理用户注册事件: userId={}, email={}", event.userId.value, event.email.value)
        
        try {
            runBlocking {
                // 初始化用户推荐
                userRecommendationService.initializeUserRecommendation(
                    userId = event.userId,
                    email = event.email.value,
                    firstName = event.firstName,
                    lastName = event.lastName
                )
            }
            
            logger.info("Recommendation模块用户注册事件处理完成: userId={}", event.userId.value)
        } catch (e: Exception) {
            logger.error("Recommendation模块处理用户注册事件失败: userId={}", event.userId.value, e)
            // 实现重试机制或补偿逻辑
            throw e
        }
    }
}