package com.lifee.common.cqrs.events

import com.lifee.common.domain.DomainEvent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.pow

/**
 * 事件重试处理器
 * 提供事件处理失败时的重试机制和幂等性保证
 */
@Component
class EventRetryHandler {
    
    private val logger = LoggerFactory.getLogger(EventRetryHandler::class.java)
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(5)
    
    // 存储事件处理状态，用于幂等性检查
    private val processedEvents = ConcurrentHashMap<String, EventProcessingStatus>()
    
    // 重试配置
    private val maxRetryAttempts = 3
    private val baseDelaySeconds = 2L
    private val maxDelaySeconds = 60L
    
    /**
     * 事件处理状态
     */
    data class EventProcessingStatus(
        val eventId: String,
        val eventType: String,
        val attempts: Int = 0,
        val lastAttemptTime: Long = System.currentTimeMillis(),
        val status: ProcessingStatus = ProcessingStatus.PENDING
    )
    
    enum class ProcessingStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }
    
    /**
     * 处理事件，包含重试逻辑和幂等性检查
     */
    fun <T : Event> processEventWithRetry(
        event: T,
        handler: EventHandler<T>,
        eventId: String = generateEventId(event)
    ) {
        // 幂等性检查
        val existingStatus = processedEvents[eventId]
        if (existingStatus?.status == ProcessingStatus.COMPLETED) {
            logger.debug("Event already processed successfully: eventId={}, eventType={}", eventId, event::class.simpleName)
            return
        }
        
        if (existingStatus?.status == ProcessingStatus.PROCESSING) {
            logger.debug("Event is currently being processed: eventId={}, eventType={}", eventId, event::class.simpleName)
            return
        }
        
        val currentAttempts = existingStatus?.attempts ?: 0
        
        if (currentAttempts >= maxRetryAttempts) {
            logger.error("Event processing failed after {} attempts: eventId={}, eventType={}", maxRetryAttempts, eventId, event::class.simpleName)
            markEventAsFailed(eventId, event::class.simpleName ?: "Unknown")
            return
        }
        
        // 标记为处理中
        markEventAsProcessing(eventId, event::class.simpleName ?: "Unknown", currentAttempts + 1)
        
        try {
            handler.handle(event)
            markEventAsCompleted(eventId, event::class.simpleName ?: "Unknown")
            logger.debug("Event processed successfully: eventId={}, eventType={}", eventId, event::class.simpleName)
        } catch (e: Exception) {
            logger.warn("Event processing failed (attempt {}): eventId={}, eventType={}, error={}", 
                currentAttempts + 1, eventId, event::class.simpleName, e.message)
            
            if (currentAttempts + 1 < maxRetryAttempts) {
                scheduleRetry(event, handler, eventId, currentAttempts + 1)
            } else {
                markEventAsFailed(eventId, event::class.simpleName ?: "Unknown")
                logger.error("Event processing failed permanently: eventId={}, eventType={}", eventId, event::class.simpleName, e)
            }
        }
    }
    
    /**
     * 调度重试
     */
    private fun <T : Event> scheduleRetry(
        event: T,
        handler: EventHandler<T>,
        eventId: String,
        attemptNumber: Int
    ) {
        val delay = calculateRetryDelay(attemptNumber)
        
        logger.info("Scheduling retry for event: eventId={}, eventType={}, attempt={}, delay={}s", 
            eventId, event::class.simpleName, attemptNumber, delay)
        
        scheduler.schedule({
            processEventWithRetry(event, handler, eventId)
        }, delay, TimeUnit.SECONDS)
    }
    
    /**
     * 计算重试延迟（指数退避）
     */
    private fun calculateRetryDelay(attemptNumber: Int): Long {
        val delay = baseDelaySeconds * 2.0.pow(attemptNumber - 1).toLong()
        return min(delay, maxDelaySeconds)
    }
    
    /**
     * 生成事件ID
     */
    private fun generateEventId(event: Event): String {
        // 使用事件类型和当前时间生成唯一ID
        return if (event is DomainEvent) {
            "${event.aggregateId}_${event::class.simpleName}_${event.occurredOn.toEpochMilli()}"
        } else {
            "${event::class.simpleName}_${System.currentTimeMillis()}_${event.hashCode()}"
        }
    }
    
    /**
     * 标记事件为处理中
     */
    private fun markEventAsProcessing(eventId: String, eventType: String, attempts: Int) {
        processedEvents[eventId] = EventProcessingStatus(
            eventId = eventId,
            eventType = eventType,
            attempts = attempts,
            status = ProcessingStatus.PROCESSING
        )
    }
    
    /**
     * 标记事件为已完成
     */
    private fun markEventAsCompleted(eventId: String, eventType: String) {
        processedEvents[eventId] = processedEvents[eventId]?.copy(
            status = ProcessingStatus.COMPLETED
        ) ?: EventProcessingStatus(
            eventId = eventId,
            eventType = eventType,
            status = ProcessingStatus.COMPLETED
        )
    }
    
    /**
     * 标记事件为失败
     */
    private fun markEventAsFailed(eventId: String, eventType: String) {
        processedEvents[eventId] = processedEvents[eventId]?.copy(
            status = ProcessingStatus.FAILED
        ) ?: EventProcessingStatus(
            eventId = eventId,
            eventType = eventType,
            status = ProcessingStatus.FAILED
        )
    }
    
    /**
     * 获取事件处理状态
     */
    fun getEventStatus(eventId: String): EventProcessingStatus? {
        return processedEvents[eventId]
    }
    
    /**
     * 清理过期的事件状态记录
     */
    fun cleanupExpiredEvents(maxAgeHours: Long = 24) {
        val cutoffTime = System.currentTimeMillis() - (maxAgeHours * 60 * 60 * 1000)
        
        val expiredEvents = processedEvents.filter { (_, status) ->
            status.lastAttemptTime < cutoffTime && 
            (status.status == ProcessingStatus.COMPLETED || status.status == ProcessingStatus.FAILED)
        }
        
        expiredEvents.forEach { (eventId, _) ->
            processedEvents.remove(eventId)
        }
        
        if (expiredEvents.isNotEmpty()) {
            logger.debug("Cleaned up {} expired event records", expiredEvents.size)
        }
    }
    
    /**
     * 关闭调度器
     */
    fun shutdown() {
        scheduler.shutdown()
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow()
            }
        } catch (e: InterruptedException) {
            scheduler.shutdownNow()
            Thread.currentThread().interrupt()
        }
    }
}