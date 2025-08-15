package com.lifee.common.domain

import com.lifee.common.cqrs.events.Event
import java.time.Instant
import java.util.*

/**
 * 领域事件基类
 * 所有领域事件都应该继承此类
 * 领域事件表示在领域中发生的重要业务事件
 */
abstract class DomainEvent(
    /**
     * 聚合根标识符
     */
    open val aggregateId: String,
    
    /**
     * 事件版本号
     */
    open val version: Long = 0,
    
    /**
     * 事件发生时间
     */
    open val occurredOn: Instant = Instant.now(),
    
    /**
     * 事件唯一标识符
     */
    open val eventId: UUID = UUID.randomUUID()
) : Event {
    
    /**
     * 获取事件类型名称
     */
    fun getEventType(): String = this::class.simpleName ?: "UnknownEvent"
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DomainEvent) return false
        return eventId == other.eventId
    }
    
    override fun hashCode(): Int {
        return eventId.hashCode()
    }
    
    override fun toString(): String {
        return "${getEventType()}(eventId=$eventId, aggregateId=$aggregateId, version=$version, occurredOn=$occurredOn)"
    }
    
    /**
     * 复制事件并更新元数据
     * 用于事件溯源中设置聚合根ID和版本
     */
    abstract fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent
}