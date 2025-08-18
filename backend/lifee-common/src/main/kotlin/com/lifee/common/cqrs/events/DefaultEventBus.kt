package com.lifee.common.cqrs.events

import com.fasterxml.jackson.databind.ObjectMapper
import com.lifee.common.domain.DomainEvent
import com.lifee.common.eventsourcing.EventStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * 默认事件总线实现
 * 支持本地事件处理和Kafka事件发布
 */
@Component
class DefaultEventBus(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
    private val eventStore: EventStore? = null
) : EventBus {
    
    private val logger = LoggerFactory.getLogger(DefaultEventBus::class.java)
    private val localHandlers = ConcurrentHashMap<KClass<out Event>, MutableList<EventHandler<*>>>()
    
    /**
     * 注册本地事件处理器
     * 
     * @param eventType 事件类型
     * @param handler 事件处理器
     */
    fun <T : Event> registerHandler(eventType: KClass<T>, handler: EventHandler<T>) {
        logger.debug("Registering event handler for {}", eventType.simpleName)
        localHandlers.computeIfAbsent(eventType) { mutableListOf() }.add(handler)
    }
    
    override fun publish(event: Event) {
        logger.debug("Publishing event: {}", event::class.simpleName)
        
        // 如果是领域事件，先持久化到事件存储
        if (event is DomainEvent && eventStore != null) {
            persistDomainEvent(event)
        }
        
        // 本地事件处理
        handleLocalEvent(event)
        
        // 发布到Kafka
        publishToKafka(event)
    }
    
    override fun publishAll(events: List<Event>) {
        logger.debug("Publishing {} events", events.size)
        
        // 批量持久化领域事件
        val domainEvents = events.filterIsInstance<DomainEvent>()
        if (domainEvents.isNotEmpty() && eventStore != null) {
            persistDomainEvents(domainEvents)
        }
        
        events.forEach { event ->
            // 本地事件处理
            handleLocalEvent(event)
            
            // 发布到Kafka
            publishToKafka(event)
        }
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun handleLocalEvent(event: Event) {
        val handlers = localHandlers[event::class] as? List<EventHandler<Event>>
        handlers?.forEach { handler ->
            try {
                handler.handle(event)
                logger.debug("Event {} handled by local handler", event::class.simpleName)
            } catch (e: Exception) {
                // 记录错误日志，但不影响其他处理器
                logger.error("Error handling event {} by local handler: {}", event::class.simpleName, e.message, e)
            }
        }
    }
    
    private fun publishToKafka(event: Event) {
        try {
            val topic = "${event::class.simpleName?.lowercase()}-events"
            val eventJson = objectMapper.writeValueAsString(event)
            
            kafkaTemplate.send(topic, event.hashCode().toString(), eventJson)
                .whenComplete { result, ex ->
                    if (ex == null) {
                        logger.debug("Event {} published to Kafka topic {}", event::class.simpleName, topic)
                    } else {
                        logger.error("Failed to publish event {} to Kafka: {}", event::class.simpleName, ex.message, ex)
                    }
                }
        } catch (e: Exception) {
            logger.error("Error serializing event {} for Kafka: {}", event::class.simpleName, e.message, e)
        }
    }
    
    /**
     * 处理从Kafka接收到的远程事件
     * 只分发到本地事件处理器，不再发布到Kafka（避免循环）
     */
    fun handleRemoteEvent(event: Event) {
        logger.debug("Handling remote event: {}", event::class.simpleName)
        
        val handlers = localHandlers[event::class] as? List<EventHandler<Event>>
        
        if (handlers.isNullOrEmpty()) {
            logger.debug("No handlers found for remote event: {}", event::class.simpleName)
            return
        }
        
        handlers.forEach { handler ->
            try {
                logger.debug("Dispatching remote event {} to handler {}", event::class.simpleName, handler::class.simpleName)
                handler.handle(event)
            } catch (e: Exception) {
                logger.error("Error handling remote event {} with handler {}", event::class.simpleName, handler::class.simpleName, e)
                // 继续处理其他处理器，不因为一个处理器失败而中断
            }
        }
    }
    
    /**
     * 获取已注册的本地处理器数量
     */
    fun getRegisteredHandlersCount(): Int {
        return localHandlers.values.sumOf { it.size }
    }
    
    /**
     * 检查是否有本地处理器注册给指定的事件类型
     */
    fun <T : Event> hasLocalHandler(eventType: KClass<T>): Boolean {
        return localHandlers[eventType]?.isNotEmpty() == true
    }
    
    /**
     * 持久化单个领域事件
     */
    private fun persistDomainEvent(event: DomainEvent) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val currentVersion = eventStore?.getCurrentVersion(event.aggregateId.toString()) ?: 0
                eventStore?.saveEvents(event.aggregateId.toString(), listOf(event), currentVersion)
                logger.debug("Domain event {} persisted to event store", event::class.simpleName)
            }
        } catch (e: Exception) {
            logger.error("Failed to persist domain event {} to event store: {}", 
                event::class.simpleName, e.message, e)
            // 不抛出异常，避免影响事件发布流程
        }
    }
    
    /**
     * 批量持久化领域事件
     */
    private fun persistDomainEvents(events: List<DomainEvent>) {
        try {
            val eventsByAggregate = events.groupBy { it.aggregateId }
            
            CoroutineScope(Dispatchers.IO).launch {
                eventsByAggregate.forEach { (aggregateId, aggregateEvents) ->
                    // 获取当前版本并保存事件
                    val currentVersion = eventStore?.getCurrentVersion(aggregateId.toString()) ?: 0
                    eventStore?.saveEvents(aggregateId.toString(), aggregateEvents, currentVersion)
                }
                logger.debug("Batch persisted {} domain events to event store", events.size)
            }
        } catch (e: Exception) {
            logger.error("Failed to batch persist {} domain events to event store: {}", 
                events.size, e.message, e)
            // 不抛出异常，避免影响事件发布流程
        }
    }
    
    /**
     * 发布聚合根的未提交事件
     * 用于事件溯源场景
     */
    fun publishUncommittedEvents(aggregateId: String, events: List<DomainEvent>) {
        logger.debug("Publishing {} uncommitted events for aggregate {}", events.size, aggregateId)
        
        if (events.isEmpty()) {
            return
        }
        
        // 批量持久化事件
        if (eventStore != null) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val currentVersion = eventStore.getCurrentVersion(aggregateId)
                    eventStore.saveEvents(aggregateId, events, currentVersion)
                    logger.debug("Persisted {} uncommitted events for aggregate {}", events.size, aggregateId)
                }
            } catch (e: Exception) {
                logger.error("Failed to persist uncommitted events for aggregate {}: {}", 
                    aggregateId, e.message, e)
                throw EventPersistenceException("Failed to persist events for aggregate $aggregateId", e)
            }
        }
        
        // 发布事件到本地处理器和Kafka
        events.forEach { event ->
            handleLocalEvent(event)
            publishToKafka(event)
        }
    }
    
    /**
     * 获取事件存储统计信息
     */
    fun getEventStoreStats(): EventStoreStats? {
        return try {
            eventStore?.let {
                EventStoreStats(
                    totalEvents = 0, // 需要EventStore提供统计方法
                    totalAggregates = 0,
                    totalSnapshots = 0
                )
            }
        } catch (e: Exception) {
            logger.error("Failed to get event store stats: {}", e.message, e)
            null
        }
    }
}

/**
 * 事件持久化异常
 */
class EventPersistenceException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

/**
 * 事件存储统计信息
 */
data class EventStoreStats(
    val totalEvents: Long,
    val totalAggregates: Long,
    val totalSnapshots: Long
)