package com.lifee.common.config

import com.lifee.common.cqrs.events.EventRetryHandler
import com.lifee.common.cqrs.events.IdempotentAspect
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

/**
 * 事件重试调度器配置
 * 负责定期清理过期的事件状态记录
 */
@Configuration
@EnableScheduling
class EventRetrySchedulerConfig(
    private val eventRetryHandler: EventRetryHandler,
    private val idempotentAspect: IdempotentAspect
) {
    
    private val logger = LoggerFactory.getLogger(EventRetrySchedulerConfig::class.java)
    
    /**
     * 每小时清理一次过期的事件状态记录
     */
    @Scheduled(fixedRate = 3600000) // 1小时 = 3600000毫秒
    fun cleanupExpiredEventRecords() {
        try {
            logger.debug("开始清理过期的事件状态记录")
            eventRetryHandler.cleanupExpiredEvents(24) // 清理24小时前的记录
            logger.debug("过期事件状态记录清理完成")
        } catch (e: Exception) {
            logger.error("清理过期事件状态记录时发生错误", e)
        }
    }
    
    /**
     * 每2小时清理一次过期的幂等性记录
     */
    @Scheduled(fixedRate = 7200000) // 2小时 = 7200000毫秒
    fun cleanupExpiredIdempotentRecords() {
        try {
            logger.debug("开始清理过期的幂等性记录")
            idempotentAspect.cleanupExpiredRecords(24) // 清理24小时前的记录
            logger.debug("过期幂等性记录清理完成")
        } catch (e: Exception) {
            logger.error("清理过期幂等性记录时发生错误", e)
        }
    }
}