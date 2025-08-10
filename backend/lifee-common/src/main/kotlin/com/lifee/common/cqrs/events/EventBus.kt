package com.lifee.common.cqrs.events

/**
 * 事件总线接口
 * 负责发布事件到相应的处理器
 */
interface EventBus {
    
    /**
     * 发布单个事件
     * 
     * @param event 要发布的事件
     */
    fun publish(event: Event)
    
    /**
     * 批量发布事件
     * 
     * @param events 要发布的事件列表
     */
    fun publishAll(events: List<Event>)
}