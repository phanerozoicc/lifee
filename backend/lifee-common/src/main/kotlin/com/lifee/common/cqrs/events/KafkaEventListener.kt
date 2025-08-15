package com.lifee.common.cqrs.events

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.KafkaHeaders
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

/**
 * Kafka事件监听器
 * 负责监听Kafka事件并分发到相应的处理器
 */
@Component
class KafkaEventListener(
    private val eventBus: DefaultEventBus,
    private val objectMapper: ObjectMapper,
    private val eventRegistry: EventRegistry,
    private val eventRetryHandler: EventRetryHandler
) {
    
    private val logger = LoggerFactory.getLogger(KafkaEventListener::class.java)
    
    /**
     * 监听用户注册事件
     */
    @KafkaListener(
        topics = ["userregisteredevent-events"],
        groupId = "lifee-event-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleUserRegisteredEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String,
        @Header(KafkaHeaders.RECEIVED_PARTITION) partition: Int,
        @Header(KafkaHeaders.OFFSET) offset: Long
    ) {
        logger.debug("Received message from topic: {}, partition: {}, offset: {}", topic, partition, offset)
        handleEvent(message, "UserRegisteredEvent")
    }
    
    /**
     * 监听用户登录成功事件
     */
    @KafkaListener(
        topics = ["userloginsuccessevent-events"],
        groupId = "lifee-event-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleUserLoginSuccessEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String
    ) {
        logger.debug("Received message from topic: {}", topic)
        handleEvent(message, "UserLoginSuccessEvent")
    }
    
    /**
     * 监听用户登录失败事件
     */
    @KafkaListener(
        topics = ["userloginfailedevent-events"],
        groupId = "lifee-event-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    fun handleUserLoginFailedEvent(
        @Payload message: String,
        @Header(KafkaHeaders.RECEIVED_TOPIC) topic: String
    ) {
        logger.debug("Received message from topic: {}", topic)
        handleEvent(message, "UserLoginFailedEvent")
    }
    
    /**
     * 通用事件处理方法
     */
    private fun handleEvent(message: String, eventTypeName: String) {
        try {
            val eventClass = eventRegistry.getEventClass(eventTypeName)
            if (eventClass != null) {
                val event = objectMapper.readValue(message, eventClass.java) as Event
                
                // 使用重试机制处理事件
                val eventHandler = object : EventHandler<Event> {
                    override fun handle(event: Event) {
                        eventBus.handleRemoteEvent(event)
                    }
                }
                
                eventRetryHandler.processEventWithRetry(event, eventHandler)
                logger.debug("Successfully processed {} event", eventTypeName)
            } else {
                logger.warn("Unknown event type: {}", eventTypeName)
            }
        } catch (e: Exception) {
            logger.error("Error processing {} event: {}", eventTypeName, e.message, e)
            // 事件重试机制已在eventRetryHandler中处理
        }
    }
}