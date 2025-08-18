package com.lifee.common.eventsourcing

import com.lifee.common.domain.AggregateSnapshot
import com.lifee.common.domain.DomainEvent
import com.lifee.common.domain.EventSourcedAggregateRoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * 事件重放服务
 * 负责从事件存储重建聚合根状态
 */
@Service
class EventReplayService(
    private val eventStore: EventStore
) {
    
    private val logger = LoggerFactory.getLogger(EventReplayService::class.java)
    
    /**
     * 重建聚合根到最新状态
     * 
     * @param aggregateId 聚合根ID
     * @param aggregateClass 聚合根类
     * @return 重建的聚合根实例
     */
    suspend fun <T : EventSourcedAggregateRoot<*>> replayAggregate(
        aggregateId: String,
        aggregateClass: KClass<T>
    ): T? = withContext(Dispatchers.IO) {
        try {
            // 检查聚合根是否存在
            if (!eventStore.aggregateExists(aggregateId)) {
                logger.debug("Aggregate {} does not exist", aggregateId)
                return@withContext null
            }
            
            // 尝试从快照开始重建
            val snapshot = eventStore.getLatestSnapshot(aggregateId)
            val fromVersion = snapshot?.version ?: 0L
            
            // 获取快照之后的事件
            val events = eventStore.getEvents(aggregateId, fromVersion)
            
            // 创建聚合根实例
            val aggregate = createAggregateInstance(aggregateClass, aggregateId)
            
            // 如果有快照，先从快照恢复
            snapshot?.let { snap ->
                @Suppress("UNCHECKED_CAST")
                (aggregate as EventSourcedAggregateRoot<Any>).restoreFromSnapshot(
                    snap as AggregateSnapshot<Map<String, Any>>
                )
                logger.debug("Restored aggregate {} from snapshot at version {}", aggregateId, snap.version)
            }
            
            // 重放事件
            if (events.isNotEmpty()) {
                aggregate.replayEvents(events)
                logger.debug("Replayed {} events for aggregate {}", events.size, aggregateId)
            }
            
            aggregate
            
        } catch (e: Exception) {
            logger.error("Error replaying aggregate {}", aggregateId, e)
            throw EventReplayException("Failed to replay aggregate $aggregateId", e)
        }
    }
    
    /**
     * 重建聚合根到指定版本
     * 
     * @param aggregateId 聚合根ID
     * @param aggregateClass 聚合根类
     * @param toVersion 目标版本
     * @return 重建的聚合根实例
     */
    suspend fun <T : EventSourcedAggregateRoot<*>> replayAggregateToVersion(
        aggregateId: String,
        aggregateClass: KClass<T>,
        toVersion: Long
    ): T? = withContext(Dispatchers.IO) {
        try {
            // 获取到指定版本的事件
            val events = eventStore.getEventsToVersion(aggregateId, toVersion)
            
            if (events.isEmpty()) {
                logger.debug("No events found for aggregate {} to version {}", aggregateId, toVersion)
                return@withContext null
            }
            
            // 创建聚合根实例
            val aggregate = createAggregateInstance(aggregateClass, aggregateId)
            
            // 重放事件
            aggregate.replayEvents(events)
            
            logger.debug("Replayed aggregate {} to version {} with {} events", 
                aggregateId, toVersion, events.size)
            
            aggregate
            
        } catch (e: Exception) {
            logger.error("Error replaying aggregate {} to version {}", aggregateId, toVersion, e)
            throw EventReplayException("Failed to replay aggregate $aggregateId to version $toVersion", e)
        }
    }
    
    /**
     * 重建聚合根到指定时间点
     * 
     * @param aggregateId 聚合根ID
     * @param aggregateClass 聚合根类
     * @param pointInTime 时间点
     * @return 重建的聚合根实例
     */
    suspend fun <T : EventSourcedAggregateRoot<*>> replayAggregateToPointInTime(
        aggregateId: String,
        aggregateClass: KClass<T>,
        pointInTime: Instant
    ): T? = withContext(Dispatchers.IO) {
        try {
            // 获取到指定时间的事件
            val events = eventStore.getEventsByTimeRange(
                aggregateId, 
                Instant.EPOCH, 
                pointInTime
            )
            
            if (events.isEmpty()) {
                logger.debug("No events found for aggregate {} to time {}", aggregateId, pointInTime)
                return@withContext null
            }
            
            // 创建聚合根实例
            val aggregate = createAggregateInstance(aggregateClass, aggregateId)
            
            // 重放事件
            aggregate.replayEvents(events)
            
            logger.debug("Replayed aggregate {} to time {} with {} events", 
                aggregateId, pointInTime, events.size)
            
            aggregate
            
        } catch (e: Exception) {
            logger.error("Error replaying aggregate {} to time {}", aggregateId, pointInTime, e)
            throw EventReplayException("Failed to replay aggregate $aggregateId to time $pointInTime", e)
        }
    }
    
    /**
     * 批量重建聚合根
     * 
     * @param aggregateIds 聚合根ID列表
     * @param aggregateClass 聚合根类
     * @return 重建的聚合根实例列表
     */
    suspend fun <T : EventSourcedAggregateRoot<*>> replayAggregates(
        aggregateIds: List<String>,
        aggregateClass: KClass<T>
    ): List<T> = withContext(Dispatchers.IO) {
        try {
            val results = mutableListOf<T>()
            
            aggregateIds.forEach { aggregateId ->
                val aggregate = replayAggregate(aggregateId, aggregateClass)
                aggregate?.let { results.add(it) }
            }
            
            logger.debug("Replayed {} aggregates out of {} requested", results.size, aggregateIds.size)
            
            results
            
        } catch (e: Exception) {
            logger.error("Error replaying multiple aggregates", e)
            throw EventReplayException("Failed to replay multiple aggregates", e)
        }
    }
    
    /**
     * 重建所有指定类型的聚合根
     * 
     * @param aggregateClass 聚合根类
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 重建的聚合根实例列表
     */
    suspend fun <T : EventSourcedAggregateRoot<*>> replayAllAggregates(
        aggregateClass: KClass<T>,
        limit: Int = 100,
        offset: Int = 0
    ): List<T> = withContext(Dispatchers.IO) {
        try {
            val aggregateType = aggregateClass.simpleName
            val aggregateIds = eventStore.getAllAggregateIds(aggregateType, limit, offset)
            
            replayAggregates(aggregateIds, aggregateClass)
            
        } catch (e: Exception) {
            logger.error("Error replaying all aggregates of type {}", aggregateClass.simpleName, e)
            throw EventReplayException("Failed to replay all aggregates of type ${aggregateClass.simpleName}", e)
        }
    }
    
    /**
     * 创建快照
     * 
     * @param aggregateId 聚合根ID
     * @param aggregateClass 聚合根类
     */
    suspend fun <T : EventSourcedAggregateRoot<*>> createSnapshot(
        aggregateId: String,
        aggregateClass: KClass<T>
    ) = withContext(Dispatchers.IO) {
        try {
            val aggregate = replayAggregate(aggregateId, aggregateClass)
                ?: throw AggregateNotFoundException(aggregateId)
            
            val snapshot = aggregate.createSnapshot()
            eventStore.saveSnapshot(snapshot)
            
            logger.debug("Created snapshot for aggregate {} at version {}", 
                aggregateId, snapshot.version)
            
        } catch (e: Exception) {
            logger.error("Error creating snapshot for aggregate {}", aggregateId, e)
            throw EventReplayException("Failed to create snapshot for aggregate $aggregateId", e)
        }
    }
    
    /**
     * 批量创建快照
     * 
     * @param aggregateClass 聚合根类
     * @param minEventsSinceLastSnapshot 自上次快照以来的最小事件数
     */
    suspend fun <T : EventSourcedAggregateRoot<*>> createSnapshotsForAllAggregates(
        aggregateClass: KClass<T>,
        minEventsSinceLastSnapshot: Int = 10
    ) = withContext(Dispatchers.IO) {
        try {
            val aggregateType = aggregateClass.simpleName
            val aggregateIds = eventStore.getAllAggregateIds(aggregateType)
            
            var snapshotsCreated = 0
            
            aggregateIds.forEach { aggregateId ->
                try {
                    val currentVersion = eventStore.getCurrentVersion(aggregateId)
                    val latestSnapshot = eventStore.getLatestSnapshot(aggregateId)
                    val lastSnapshotVersion = latestSnapshot?.version ?: 0L
                    
                    // 只有当事件数量足够时才创建快照
                    if (currentVersion - lastSnapshotVersion >= minEventsSinceLastSnapshot) {
                        createSnapshot(aggregateId, aggregateClass)
                        snapshotsCreated++
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to create snapshot for aggregate {}: {}", aggregateId, e.message)
                }
            }
            
            logger.info("Created {} snapshots for {} aggregates of type {}", 
                snapshotsCreated, aggregateIds.size, aggregateType)
            
        } catch (e: Exception) {
            logger.error("Error creating snapshots for aggregates of type {}", aggregateClass.simpleName, e)
            throw EventReplayException("Failed to create snapshots for aggregates of type ${aggregateClass.simpleName}", e)
        }
    }
    
    /**
     * 创建聚合根实例
     */
    private fun <T : EventSourcedAggregateRoot<*>> createAggregateInstance(
        aggregateClass: KClass<T>,
        aggregateId: String
    ): T {
        // 这里需要根据具体的聚合根类型来创建实例
        // 可能需要使用工厂模式或依赖注入
        return try {
            // 尝试使用无参构造函数创建实例
            aggregateClass.createInstance()
        } catch (e: Exception) {
            throw EventReplayException("Failed to create instance of ${aggregateClass.simpleName}", e)
        }
    }
}

/**
 * 事件重放异常
 */
class EventReplayException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)