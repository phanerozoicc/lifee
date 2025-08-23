package com.lifee.common.eventsourcing

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.lifee.common.domain.DomainEvent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * 事件序列化器
 * 负责事件的序列化和反序列化
 */
@Component
class EventSerializer(
    private val objectMapper: ObjectMapper,
    // @Qualifier("eventSourcingEventRegistry") private val eventRegistry: EventSourcingEventRegistry
) {
    
    /**
     * 序列化事件
     */
    fun serialize(event: DomainEvent): String {
        return objectMapper.writeValueAsString(event)
    }
    
    /**
     * 反序列化事件
     */
    fun deserialize(eventType: String, eventData: String): DomainEvent {
        // val eventClass = eventRegistry.getEventType(eventType)
        //     ?: throw IllegalArgumentException("Unknown event type: $eventType")
        
        // return try {
        //     objectMapper.readValue(eventData, eventClass.java)
        // } catch (e: Exception) {
        //     throw EventDeserializationException("Failed to deserialize event of type $eventType", e)
        // }
        throw UnsupportedOperationException("EventSerializer is temporarily disabled")
    }
    
    /**
     * 获取事件类型名称
     */
    fun getEventTypeName(event: DomainEvent): String {
        return event::class.simpleName ?: throw IllegalArgumentException("Event class must have a name")
    }
    
    /**
     * 批量序列化事件
     */
    fun serializeEvents(events: List<DomainEvent>): List<Pair<String, String>> {
        return events.map { event ->
            getEventTypeName(event) to serialize(event)
        }
    }
    
    /**
     * 批量反序列化事件
     */
    fun deserializeEvents(eventData: List<Pair<String, String>>): List<DomainEvent> {
        return eventData.map { (eventType, data) ->
            deserialize(eventType, data)
        }
    }
}

/**
 * 事件反序列化异常
 */
class EventDeserializationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)