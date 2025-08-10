package com.lifee.common.cqrs.events

import java.time.Instant
import java.util.*

/**
 * 事件存储接口
 * 负责持久化和检索事件
 */
interface EventStore {
    
    /**
     * 保存事件
     * 
     * @param aggregateId 聚合根ID
     * @param events 要保存的事件列表
     * @param expectedVersion 期望的版本号，用于乐观锁控制
     */
    fun saveEvents(aggregateId: UUID, events: List<Event>, expectedVersion: Long)
    
    /**
     * 获取聚合根的所有事件
     * 
     * @param aggregateId 聚合根ID
     * @return 事件列表
     */
    fun getEvents(aggregateId: UUID): List<StoredEvent>
    
    /**
     * 获取聚合根从指定版本开始的事件
     * 
     * @param aggregateId 聚合根ID
     * @param fromVersion 起始版本号
     * @return 事件列表
     */
    fun getEventsFromVersion(aggregateId: UUID, fromVersion: Long): List<StoredEvent>
    
    /**
     * 获取指定时间范围内的所有事件
     * 
     * @param from 开始时间
     * @param to 结束时间
     * @return 事件列表
     */
    fun getEventsByTimeRange(from: Instant, to: Instant): List<StoredEvent>
}

/**
 * 存储的事件
 */
data class StoredEvent(
    val eventId: UUID,
    val aggregateId: UUID,
    val eventType: String,
    val eventData: String,
    val version: Long,
    val timestamp: Instant
)