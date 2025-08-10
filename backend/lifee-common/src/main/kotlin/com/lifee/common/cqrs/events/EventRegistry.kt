package com.lifee.common.cqrs.events

import org.springframework.stereotype.Component
import kotlin.reflect.KClass

/**
 * 事件注册表
 * 管理事件类型名称和事件类的映射关系
 */
@Component
class EventRegistry {
    
    private val eventClasses = mutableMapOf<String, KClass<out Event>>()
    
    init {
        // 注册已知的事件类型
        registerEventTypes()
    }
    
    /**
     * 注册事件类型
     */
    private fun registerEventTypes() {
        try {
            // 注册用户相关事件
            registerEvent("UserRegisteredEvent", "com.lifee.user.domain.events.UserRegisteredEvent")
            registerEvent("UserLoginSuccessEvent", "com.lifee.user.domain.events.UserLoginSuccessEvent")
            registerEvent("UserLoginFailedEvent", "com.lifee.user.domain.events.UserLoginFailedEvent")
            
            // 可以在这里注册更多事件类型
        } catch (e: Exception) {
            // 忽略类加载错误，某些模块可能不存在
        }
    }
    
    /**
     * 注册单个事件类型
     */
    private fun registerEvent(eventTypeName: String, className: String) {
        try {
            @Suppress("UNCHECKED_CAST")
            val eventClass = Class.forName(className).kotlin as KClass<out Event>
            eventClasses[eventTypeName] = eventClass
        } catch (e: ClassNotFoundException) {
            // 忽略不存在的类
        }
    }
    
    /**
     * 获取事件类
     */
    fun getEventClass(eventTypeName: String): KClass<out Event>? {
        return eventClasses[eventTypeName]
    }
    
    /**
     * 获取所有注册的事件类型
     */
    fun getAllEventTypes(): Set<String> {
        return eventClasses.keys.toSet()
    }
    
    /**
     * 动态注册事件类型
     */
    fun registerEventClass(eventTypeName: String, eventClass: KClass<out Event>) {
        eventClasses[eventTypeName] = eventClass
    }
}