package com.lifee.common.eventsourcing

import com.lifee.common.domain.AggregateSnapshot
import com.lifee.common.domain.EventSourcedAggregateRoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

/**
 * 事件溯源仓储基类
 * 提供聚合根的加载和保存功能
 */
abstract class EventSourcedRepository<T : EventSourcedAggregateRoot<ID>, ID>(
    private val eventStore: EventStore,
    private val snapshotService: SnapshotService,
    private val aggregateClass: KClass<T>
) {
    
    private val logger = LoggerFactory.getLogger(EventSourcedRepository::class.java)
    
    /**
     * 根据ID加载聚合根
     */
    suspend fun load(aggregateId: ID): T? = withContext(Dispatchers.IO) {
        val aggregateIdStr = aggregateId.toString()
        
        try {
            // 检查聚合根是否存在
            if (!eventStore.aggregateExists(aggregateIdStr)) {
                return@withContext null
            }
            
            // 尝试从快照加载
            val snapshot = snapshotService.getLatestSnapshot(aggregateIdStr, null)
            val fromVersion = snapshot?.version ?: 0L
            
            // 获取快照之后的事件
            val events = eventStore.getEvents(aggregateIdStr, fromVersion)
            
            // 创建聚合根实例
            val aggregate = createAggregateInstance(aggregateId)
            
            // 从快照恢复状态
            snapshot?.let { 
                @Suppress("UNCHECKED_CAST")
                aggregate.restoreFromSnapshot(snapshot as AggregateSnapshot<Map<String, Any>>) 
            }
            
            // 重放事件
            if (events.isNotEmpty()) {
                aggregate.replayEvents(events)
            }
            
            logger.debug("Loaded aggregate {} with {} events from version {}", 
                aggregateIdStr, events.size, fromVersion)
            
            aggregate
            
        } catch (e: Exception) {
            logger.error("Error loading aggregate {}: {}", aggregateIdStr, e.message, e)
            throw RepositoryException("Failed to load aggregate", e)
        }
    }
    
    /**
     * 保存聚合根
     */
    suspend fun save(aggregate: T) = withContext(Dispatchers.IO) {
        val aggregateId = aggregate.getId().toString()
        
        try {
            // 获取未提交的事件
            val uncommittedEvents = aggregate.getUncommittedEvents()
            
            if (uncommittedEvents.isEmpty()) {
                logger.debug("No uncommitted events for aggregate {}", aggregateId)
                return@withContext
            }
            
            // 获取期望版本（当前版本减去未提交事件数量）
            val expectedVersion = aggregate.getVersion() - uncommittedEvents.size
            
            // 保存事件
            eventStore.saveEvents(aggregateId, uncommittedEvents, expectedVersion)
            
            // 标记事件为已提交
            aggregate.markEventsAsCommitted()
            
            // 检查是否需要创建快照
            if (snapshotService.shouldCreateSnapshot(aggregate)) {
                snapshotService.createSnapshot(aggregate)
            }
            
            logger.debug("Saved aggregate {} with {} events", 
                aggregateId, uncommittedEvents.size)
            
        } catch (e: Exception) {
            logger.error("Error saving aggregate {}: {}", aggregateId, e.message, e)
            throw RepositoryException("Failed to save aggregate", e)
        }
    }
    
    /**
     * 检查聚合根是否存在
     */
    suspend fun exists(aggregateId: ID): Boolean {
        return eventStore.aggregateExists(aggregateId.toString())
    }
    
    /**
     * 删除聚合根（慎用）
     */
    suspend fun delete(aggregateId: ID) {
        val aggregateIdStr = aggregateId.toString()
        
        try {
            eventStore.deleteAggregate(aggregateIdStr)
            logger.warn("Deleted aggregate {}", aggregateIdStr)
        } catch (e: Exception) {
            logger.error("Error deleting aggregate {}: {}", aggregateIdStr, e.message, e)
            throw RepositoryException("Failed to delete aggregate", e)
        }
    }
    
    /**
     * 创建聚合根实例
     * 子类需要实现此方法
     */
    protected abstract fun createAggregateInstance(id: ID): T
    
    /**
     * 判断是否需要创建快照
     * 默认每100个事件创建一次快照
     */
    protected open fun shouldCreateSnapshot(aggregate: T): Boolean {
        return aggregate.getVersion() % 100 == 0L
    }
}

/**
 * 仓储异常
 */
class RepositoryException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)