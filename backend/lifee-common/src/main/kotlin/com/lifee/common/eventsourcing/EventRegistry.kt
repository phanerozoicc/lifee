package com.lifee.common.eventsourcing

import com.lifee.common.domain.DomainEvent
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * 事件注册表
 * 用于管理事件类型和反序列化
 */
@Component
class EventRegistry {
    
    private val eventTypes = ConcurrentHashMap<String, KClass<out DomainEvent>>()
    
    /**
     * 注册事件类型
     */
    fun registerEventType(eventType: KClass<out DomainEvent>) {
        val eventName = eventType.simpleName ?: throw IllegalArgumentException("Event class must have a name")
        eventTypes[eventName] = eventType
    }
    
    /**
     * 获取事件类型
     */
    fun getEventType(eventName: String): KClass<out DomainEvent>? {
        return eventTypes[eventName]
    }
    
    /**
     * 获取所有注册的事件类型
     */
    fun getAllEventTypes(): Map<String, KClass<out DomainEvent>> {
        return eventTypes.toMap()
    }
    
    /**
     * 检查事件类型是否已注册
     */
    fun isEventTypeRegistered(eventName: String): Boolean {
        return eventTypes.containsKey(eventName)
    }
    
    /**
     * 批量注册事件类型
     */
    fun registerEventTypes(eventTypes: List<KClass<out DomainEvent>>) {
        eventTypes.forEach { registerEventType(it) }
    }
}