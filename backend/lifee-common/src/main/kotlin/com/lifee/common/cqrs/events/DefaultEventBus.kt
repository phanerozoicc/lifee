package com.lifee.common.cqrs.events

import com.fasterxml.jackson.databind.ObjectMapper
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
    private val objectMapper: ObjectMapper
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
        
        // 本地事件处理
        handleLocalEvent(event)
        
        // 发布到Kafka
        publishToKafka(event)
    }
    
    override fun publishAll(events: List<Event>) {
        logger.debug("Publishing {} events", events.size)
        events.forEach { publish(it) }
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
}