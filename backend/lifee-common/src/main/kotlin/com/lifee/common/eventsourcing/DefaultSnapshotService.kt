package com.lifee.common.eventsourcing

import com.lifee.common.domain.AggregateSnapshot
import com.lifee.common.domain.EventSourcedAggregateRoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * 默认快照服务实现
 */
@Service
class DefaultSnapshotService(
    private val eventStore: EventStore
) : SnapshotService {
    
    private val logger = LoggerFactory.getLogger(DefaultSnapshotService::class.java)
    
    companion object {
        private const val DEFAULT_SNAPSHOT_FREQUENCY = 100L // 每100个事件创建一次快照
        private const val MAX_SNAPSHOTS_PER_AGGREGATE = 10 // 每个聚合根最多保留10个快照
        private const val SNAPSHOT_CLEANUP_DAYS = 30L // 清理30天前的快照
    }
    
    override suspend fun createSnapshot(aggregate: EventSourcedAggregateRoot<*>) = withContext(Dispatchers.IO) {
        try {
            val snapshot = aggregate.createSnapshot()
            eventStore.saveSnapshot(snapshot)
            
            logger.debug("Created snapshot for aggregate {} at version {}", 
                aggregate.getId(), aggregate.getVersion())
            
            // 异步清理旧快照
            cleanupOldSnapshots(aggregate.getId().toString())
            
        } catch (e: Exception) {
            logger.error("Error creating snapshot for aggregate {}: {}", 
                aggregate.getId(), e.message, e)
            throw SnapshotException("Failed to create snapshot", e)
        }
    }
    
    override suspend fun getLatestSnapshot(aggregateId: String, maxVersion: Long?): AggregateSnapshot<*>? {
        return try {
            eventStore.getLatestSnapshot(aggregateId, maxVersion)
        } catch (e: Exception) {
            logger.error("Error retrieving latest snapshot for aggregate {}: {}", 
                aggregateId, e.message, e)
            null // 快照获取失败时返回null，让系统从事件重建
        }
    }
    
    override suspend fun shouldCreateSnapshot(aggregate: EventSourcedAggregateRoot<*>): Boolean {
        val currentVersion = aggregate.getVersion()
        
        // 检查版本是否达到快照频率
        if (currentVersion % DEFAULT_SNAPSHOT_FREQUENCY != 0L) {
            return false
        }
        
        // 检查是否已经存在该版本的快照
        val existingSnapshot = getLatestSnapshot(aggregate.getId().toString(), currentVersion)
        return existingSnapshot?.version != currentVersion
    }
    
    override suspend fun cleanupOldSnapshots(aggregateId: String) = withContext(Dispatchers.IO) {
        try {
            // 获取所有快照
            val allSnapshots = eventStore.getAllSnapshots()
                .filter { it.aggregateId == aggregateId }
                .sortedByDescending { it.version }
            
            // 保留最新的N个快照
            val snapshotsToDelete = allSnapshots.drop(MAX_SNAPSHOTS_PER_AGGREGATE)
            
            // 删除过期快照
            val cutoffTime = Instant.now().minus(SNAPSHOT_CLEANUP_DAYS, ChronoUnit.DAYS)
            val expiredSnapshots = allSnapshots.filter { it.createdAt.isBefore(cutoffTime) }
            
            val toDelete = (snapshotsToDelete + expiredSnapshots).distinctBy { it.version }
            
            toDelete.forEach { snapshot ->
                eventStore.deleteSnapshot(aggregateId, snapshot.version)
                logger.debug("Deleted old snapshot for aggregate {} at version {}", 
                    aggregateId, snapshot.version)
            }
            
            if (toDelete.isNotEmpty()) {
                logger.info("Cleaned up {} old snapshots for aggregate {}", 
                    toDelete.size, aggregateId)
            }
            
        } catch (e: Exception) {
            logger.error("Error cleaning up snapshots for aggregate {}: {}", 
                aggregateId, e.message, e)
        }
    }
    
    override suspend fun getSnapshotStats(aggregateId: String): SnapshotStats {
        return try {
            val snapshots = eventStore.getAllSnapshots()
                .filter { it.aggregateId == aggregateId }
            
            val totalSnapshots = snapshots.size
            val latestSnapshot = snapshots.maxByOrNull { it.version }
            val oldestSnapshot = snapshots.minByOrNull { it.version }
            
            SnapshotStats(
                totalSnapshots = totalSnapshots,
                aggregateTypes = mapOf(aggregateId to totalSnapshots),
                oldestSnapshot = oldestSnapshot?.createdAt,
                newestSnapshot = latestSnapshot?.createdAt,
                aggregatesWithSnapshots = if (totalSnapshots > 0) 1 else 0,
                averageSnapshotsPerAggregate = totalSnapshots.toDouble(),
                oldestSnapshotAge = oldestSnapshot?.let { 
                    java.time.temporal.ChronoUnit.MILLIS.between(it.createdAt, Instant.now()) 
                } ?: 0L,
                newestSnapshotAge = latestSnapshot?.let { 
                    java.time.temporal.ChronoUnit.MILLIS.between(it.createdAt, Instant.now()) 
                } ?: 0L,
                totalSnapshotSize = snapshots.sumOf { it.data.toString().length.toLong() }
            )
            
        } catch (e: Exception) {
            logger.error("Error getting snapshot stats for aggregate {}: {}", 
                aggregateId, e.message, e)
            SnapshotStats(
                totalSnapshots = 0,
                aggregateTypes = emptyMap(),
                oldestSnapshot = null,
                newestSnapshot = null,
                aggregatesWithSnapshots = 0,
                averageSnapshotsPerAggregate = 0.0,
                oldestSnapshotAge = 0L,
                newestSnapshotAge = 0L,
                totalSnapshotSize = 0L
            )
        }
    }
    
    /**
     * 定时清理过期快照
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    suspend fun scheduledSnapshotCleanup() {
        logger.info("Starting scheduled snapshot cleanup")
        
        try {
            val allAggregateIds = eventStore.getAllAggregateIds()
            
            allAggregateIds.forEach { aggregateId ->
                cleanupOldSnapshots(aggregateId)
            }
            
            logger.info("Completed scheduled snapshot cleanup for {} aggregates", 
                allAggregateIds.size)
            
        } catch (e: Exception) {
            logger.error("Error during scheduled snapshot cleanup: {}", e.message, e)
        }
    }
}