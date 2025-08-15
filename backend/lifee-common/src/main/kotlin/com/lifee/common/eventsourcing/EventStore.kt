package com.lifee.common.eventsourcing

import com.lifee.common.domain.AggregateSnapshot
import com.lifee.common.domain.DomainEvent
import java.time.Instant

/**
 * 事件存储接口
 * 负责持久化和检索领域事件
 */
interface EventStore {
    
    /**
     * 保存事件到事件存储
     * 
     * @param aggregateId 聚合根ID
     * @param events 要保存的事件列表
     * @param expectedVersion 期望的聚合根版本（用于并发控制）
     * @throws ConcurrencyException 当版本冲突时抛出
     */
    suspend fun saveEvents(
        aggregateId: String,
        events: List<DomainEvent>,
        expectedVersion: Long
    )
    
    /**
     * 获取聚合根的所有事件
     * 
     * @param aggregateId 聚合根ID
     * @param fromVersion 起始版本号（可选）
     * @return 事件列表
     */
    suspend fun getEvents(
        aggregateId: String,
        fromVersion: Long = 0
    ): List<DomainEvent>
    
    /**
     * 获取聚合根在指定版本的事件
     * 
     * @param aggregateId 聚合根ID
     * @param toVersion 结束版本号
     * @return 事件列表
     */
    suspend fun getEventsToVersion(
        aggregateId: String,
        toVersion: Long
    ): List<DomainEvent>
    
    /**
     * 获取指定时间范围内的事件
     * 
     * @param aggregateId 聚合根ID
     * @param fromTime 开始时间
     * @param toTime 结束时间
     * @return 事件列表
     */
    suspend fun getEventsByTimeRange(
        aggregateId: String,
        fromTime: Instant,
        toTime: Instant
    ): List<DomainEvent>
    
    /**
     * 获取聚合根的当前版本
     * 
     * @param aggregateId 聚合根ID
     * @return 当前版本号，如果聚合根不存在返回0
     */
    suspend fun getCurrentVersion(aggregateId: String): Long
    
    /**
     * 检查聚合根是否存在
     * 
     * @param aggregateId 聚合根ID
     * @return 如果存在返回true
     */
    suspend fun aggregateExists(aggregateId: String): Boolean
    
    /**
     * 保存聚合根快照
     * 
     * @param snapshot 聚合根快照
     */
    suspend fun saveSnapshot(snapshot: AggregateSnapshot<*>)
    
    /**
     * 获取聚合根的最新快照
     * 
     * @param aggregateId 聚合根ID
     * @param maxVersion 最大版本号（可选）
     * @return 快照，如果不存在返回null
     */
    suspend fun getLatestSnapshot(
        aggregateId: String,
        maxVersion: Long? = null
    ): AggregateSnapshot<*>?
    
    /**
     * 获取所有聚合根ID
     * 
     * @param aggregateType 聚合根类型（可选）
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 聚合根ID列表
     */
    suspend fun getAllAggregateIds(
        aggregateType: String? = null,
        limit: Int = 100,
        offset: Int = 0
    ): List<String>
    
    /**
     * 删除聚合根的所有事件和快照
     * 注意：这是一个危险操作，通常只在测试或数据清理时使用
     * 
     * @param aggregateId 聚合根ID
     */
    suspend fun deleteAggregate(aggregateId: String)
}

/**
 * 并发异常
 * 当事件存储检测到版本冲突时抛出
 */
class ConcurrencyException(
    val aggregateId: String,
    val expectedVersion: Long,
    val actualVersion: Long,
    message: String = "Concurrency conflict for aggregate $aggregateId. Expected version: $expectedVersion, actual version: $actualVersion"
) : RuntimeException(message)

/**
 * 聚合根不存在异常
 */
class AggregateNotFoundException(
    val aggregateId: String,
    message: String = "Aggregate with ID $aggregateId not found"
) : RuntimeException(message)

/**
 * 事件存储异常
 */
class EventStoreException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)