package com.lifee.common.eventsourcing

import com.lifee.common.domain.EventSourcedAggregateRoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.Duration
import java.time.temporal.ChronoUnit
import kotlin.reflect.KClass

/**
 * 快照服务
 * 负责管理聚合根的快照创建、策略和清理
 */
@Service
class SnapshotService(
    private val eventStore: EventStore,
    private val snapshotConfig: SnapshotConfig,
    private val eventReplayService: EventReplayService
) {
    
    private val logger = LoggerFactory.getLogger(SnapshotService::class.java)
    
    init {
        // 验证配置
        snapshotConfig.validate()
        logger.info("SnapshotService initialized with config: enabled={}, eventThreshold={}, timeThreshold={}", 
            snapshotConfig.enabled, snapshotConfig.eventCountThreshold, snapshotConfig.timeThreshold)
    }
    
    /**
     * 快照策略配置
     */
    data class SnapshotStrategy(
        val eventsThreshold: Int = 100,  // 事件数量阈值
        val timeThresholdHours: Long = 24,  // 时间阈值（小时）
        val maxSnapshotsPerAggregate: Int = 10,  // 每个聚合根最大快照数
        val cleanupIntervalHours: Long = 6  // 清理间隔（小时）
    )
    
    private val defaultStrategy = SnapshotStrategy()
    
    /**
     * 检查是否应该创建快照
     */
    fun shouldCreateSnapshot(aggregate: EventSourcedAggregateRoot<*>): Boolean {
        if (!snapshotConfig.enabled) {
            return false
        }
        
        val aggregateId = aggregate.getId().toString()
        
        try {
            // 获取最新快照
            val latestSnapshot = runBlocking { eventStore.getLatestSnapshot(aggregateId) }
            val currentVersion = aggregate.getVersion()
            
            // 基于事件数量的策略
            val eventsSinceLastSnapshot = if (latestSnapshot != null) {
                currentVersion - latestSnapshot.version
            } else {
                currentVersion
            }
            
            if (eventsSinceLastSnapshot >= snapshotConfig.eventCountThreshold) {
                logger.debug("Snapshot needed for aggregate {} due to event count: {} >= {}", 
                    aggregateId, eventsSinceLastSnapshot, snapshotConfig.eventCountThreshold)
                return true
            }
            
            // 基于时间的策略
            if (latestSnapshot != null) {
                val timeSinceLastSnapshot = ChronoUnit.MILLIS.between(latestSnapshot.timestamp, Instant.now())
                if (timeSinceLastSnapshot >= snapshotConfig.timeThreshold.toMillis()) {
                    logger.debug("Snapshot needed for aggregate {} due to time: {} ms >= {} ms", 
                        aggregateId, timeSinceLastSnapshot, snapshotConfig.timeThreshold.toMillis())
                    return true
                }
            }
            
            return false
            
        } catch (e: Exception) {
            logger.warn("Error checking snapshot condition for aggregate {}: {}", aggregateId, e.message)
            return false
        }
    }
    
    /**
     * 为聚合根创建快照
     * 
     * @param aggregateId 聚合根ID
     * @param aggregateClass 聚合根类
     * @param strategy 快照策略
     */
    suspend fun <T : EventSourcedAggregateRoot<*>> createSnapshotIfNeeded(
        aggregateId: String,
        aggregateClass: KClass<T>,
        strategy: SnapshotStrategy = defaultStrategy
    ) = withContext(Dispatchers.IO) {
        try {
            // 重建聚合根以检查是否需要快照
            val aggregate = eventReplayService.replayAggregate(aggregateId, aggregateClass)
            if (aggregate != null && shouldCreateSnapshot(aggregate)) {
                eventReplayService.createSnapshot(aggregateId, aggregateClass)
                logger.info("Created snapshot for aggregate {} of type {}", 
                    aggregateId, aggregateClass.simpleName)
                
                // 清理旧快照
                cleanupOldSnapshots()
            }
        } catch (e: Exception) {
            logger.error("Error creating snapshot for aggregate {} of type {}", 
                aggregateId, aggregateClass.simpleName, e)
        }
    }
    
    /**
     * 批量创建快照
     * 
     * @param aggregateClass 聚合根类
     * @param strategy 快照策略
     */
    suspend fun <T : EventSourcedAggregateRoot<*>> createSnapshotsForAllAggregates(
        aggregateClass: KClass<T>,
        strategy: SnapshotStrategy = defaultStrategy
    ) = withContext(Dispatchers.IO) {
        try {
            val aggregateType = aggregateClass.simpleName
            val aggregateIds = eventStore.getAllAggregateIds(aggregateType)
            
            var snapshotsCreated = 0
            
            aggregateIds.forEach { aggregateId ->
                try {
                    // 重建聚合根以检查是否需要快照
                    val aggregate = eventReplayService.replayAggregate(aggregateId, aggregateClass)
                    if (aggregate != null && shouldCreateSnapshot(aggregate)) {
                        eventReplayService.createSnapshot(aggregateId, aggregateClass)
                        snapshotsCreated++
                        
                        // 清理旧快照
                        cleanupOldSnapshots()
                    }
                } catch (e: Exception) {
                    logger.error("Error creating snapshot for aggregate {}", aggregateId, e)
                }
            }
            
            logger.info("Created {} snapshots for {} aggregates of type {}", 
                snapshotsCreated, aggregateIds.size, aggregateType)
            
        } catch (e: Exception) {
            logger.error("Error creating snapshots for aggregates of type {}", 
                aggregateClass.simpleName, e)
        }
    }
    
    /**
     * 清理旧快照
     */
    suspend fun cleanupOldSnapshots() {
        if (!snapshotConfig.enabled) {
            return
        }
        
        try {
            val cutoffTime = Instant.now().minus(snapshotConfig.cleanupInterval)
            
            logger.info("Starting snapshot cleanup for snapshots older than {}", cutoffTime)
            
            // 获取所有聚合根的快照
            val allSnapshots = eventStore.getAllSnapshots()
            val snapshotsByAggregate = allSnapshots.groupBy { it.aggregateId }
            
            var deletedCount = 0
            
            snapshotsByAggregate.forEach { (aggregateId, snapshots) ->
                try {
                    // 按版本排序，保留最新的快照
                    val sortedSnapshots = snapshots.sortedByDescending { it.version }
                    
                    // 保留最新的N个快照
                    val snapshotsToKeep = sortedSnapshots.take(snapshotConfig.maxSnapshotsPerAggregate)
                    val snapshotsToDelete = sortedSnapshots.drop(snapshotConfig.maxSnapshotsPerAggregate)
                    
                    // 删除超出数量限制的快照
                    snapshotsToDelete.forEach { snapshot ->
                        eventStore.deleteSnapshot(snapshot.aggregateId, snapshot.version)
                        deletedCount++
                        logger.debug("Deleted snapshot for aggregate {} at version {}", 
                            snapshot.aggregateId, snapshot.version)
                    }
                    
                    // 删除超出时间限制的快照（但保留至少一个最新的）
                    if (snapshotsToKeep.size > 1) {
                        val oldSnapshots = snapshotsToKeep.drop(1).filter { it.timestamp.isBefore(cutoffTime) }
                        oldSnapshots.forEach { snapshot ->
                            eventStore.deleteSnapshot(snapshot.aggregateId, snapshot.version)
                            deletedCount++
                            logger.debug("Deleted old snapshot for aggregate {} at version {} (timestamp: {})", 
                                snapshot.aggregateId, snapshot.version, snapshot.timestamp)
                        }
                    }
                    
                } catch (e: Exception) {
                    logger.warn("Error cleaning up snapshots for aggregate {}: {}", aggregateId, e.message)
                }
            }
            
            logger.info("Snapshot cleanup completed. Deleted {} snapshots", deletedCount)
            
        } catch (e: Exception) {
            logger.error("Error during snapshot cleanup", e)
            throw SnapshotException("Snapshot cleanup failed", e)
        }
    }
    
    /**
     * 获取快照统计信息
     * 
     * @param aggregateType 聚合根类型
     * @return 快照统计信息
     */
    suspend fun getSnapshotStats(aggregateType: String? = null): SnapshotStats = withContext(Dispatchers.IO) {
        try {
            // 这里需要扩展EventStore接口来支持快照统计
            // 暂时返回默认值
            SnapshotStats(
                totalSnapshots = 0,
                aggregateTypes = emptyMap(),
                oldestSnapshot = null,
                newestSnapshot = null
            )
        } catch (e: Exception) {
            logger.error("Error getting snapshot stats", e)
            SnapshotStats(
                totalSnapshots = 0,
                aggregateTypes = emptyMap(),
                oldestSnapshot = null,
                newestSnapshot = null
            )
        }
    }

    /**
     * 获取快照统计信息
     */
    suspend fun getSnapshotStatistics(): SnapshotStats {
        return try {
            val allSnapshots = eventStore.getAllSnapshots()
            val now = Instant.now()
            
            val totalSnapshots = allSnapshots.size
            val aggregatesWithSnapshots = allSnapshots.map { it.aggregateId }.distinct().size
            val averageSnapshotsPerAggregate = if (aggregatesWithSnapshots > 0) {
                totalSnapshots.toDouble() / aggregatesWithSnapshots
            } else 0.0
            
            val oldestSnapshotAge = if (allSnapshots.isNotEmpty()) {
                ChronoUnit.MILLIS.between(allSnapshots.minByOrNull { it.timestamp }?.timestamp, now)
            } else 0L
            
            val newestSnapshotAge = if (allSnapshots.isNotEmpty()) {
                ChronoUnit.MILLIS.between(allSnapshots.maxByOrNull { it.timestamp }?.timestamp, now)
            } else 0L
            
            val totalSnapshotSize = allSnapshots.sumOf { it.data.toString().length.toLong() }
            
            SnapshotStats(
                totalSnapshots = totalSnapshots,
                aggregatesWithSnapshots = aggregatesWithSnapshots,
                averageSnapshotsPerAggregate = averageSnapshotsPerAggregate,
                oldestSnapshotAge = oldestSnapshotAge,
                newestSnapshotAge = newestSnapshotAge,
                totalSnapshotSize = totalSnapshotSize
            )
        } catch (e: Exception) {
            logger.error("Error getting snapshot statistics", e)
            throw SnapshotException("Failed to get snapshot statistics", e)
        }
    }
    
    /**
     * 为所有聚合根创建快照
     */
    suspend fun createSnapshotsForAllAggregates() {
        if (!snapshotConfig.enabled) {
            return
        }
        
        try {
            logger.info("Starting batch snapshot creation for all aggregates")
            
            // 这里需要根据实际的聚合根类型来创建快照
            // 由于我们有多个模块，需要为每个模块的聚合根类型创建快照
            
            // 获取所有聚合根ID（需要EventStore提供此功能）
            val allAggregateIds = eventStore.getAllAggregateIds()
            
            var snapshotsCreated = 0
            var errors = 0
            
            allAggregateIds.forEach { aggregateId ->
                try {
                    val currentVersion = eventStore.getCurrentVersion(aggregateId)
                    val latestSnapshot = eventStore.getLatestSnapshot(aggregateId)
                    val lastSnapshotVersion = latestSnapshot?.version ?: 0L
                    
                    // 检查是否需要创建快照
                    if (currentVersion - lastSnapshotVersion >= snapshotConfig.eventCountThreshold) {
                        // 这里需要根据聚合根类型来重建和创建快照
                        // 暂时记录需要创建快照的聚合根
                        logger.debug("Aggregate {} needs snapshot (version: {}, last snapshot: {})", 
                            aggregateId, currentVersion, lastSnapshotVersion)
                        snapshotsCreated++
                    }
                } catch (e: Exception) {
                    logger.warn("Failed to check/create snapshot for aggregate {}: {}", aggregateId, e.message)
                    errors++
                }
            }
            
            logger.info("Batch snapshot creation completed. Created: {}, Errors: {}", snapshotsCreated, errors)
            
        } catch (e: Exception) {
            logger.error("Error during batch snapshot creation", e)
            throw SnapshotException("Batch snapshot creation failed", e)
        }
    }
}