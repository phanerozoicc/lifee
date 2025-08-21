package com.github.phanerozoicc.base.eventsource

import com.github.phanerozoicc.base.domain.EventSourcedAggregateRoot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KLogging
import java.time.Instant

class DefaultSnapshotService(
    private val eventStore: EventStore,
    private val snapshotProperties: SnapshotProperties,
    private val eventReplayService: EventReplayService
): SnapshotService {

    companion object: KLogging()


    // 使用前校验配置
    init {
        snapshotProperties.validate()
        logger.info("snapshotService initialized with config enabled={}, eventCountThreshold={}, timeThreshold={}",
            snapshotProperties.enabled, snapshotProperties.eventCountThreshold, snapshotProperties.timeThreshold)
    }

    /**
     * 判断是否需要创建快照
     */
    override suspend fun shouldCreateSnapshot(aggregate: EventSourcedAggregateRoot<*>): Boolean {
        val currentVersion = aggregate.getVersion()
        if (snapshotProperties.strategy==SnapshotStrategy.EVENT_COUNT
            || snapshotProperties.strategy==SnapshotStrategy.HYBRID) {
            if (currentVersion % snapshotProperties.eventCountThreshold != 0L) {
                return false
            }
        }
        // 检查是否已经存在快照
        val lastSnapshot = getLastSnapshot(aggregate.id.toString(), currentVersion)
        if (lastSnapshot!=null) {
            // 判断创建事件是否超过快照创建时间间隔
            if (snapshotProperties.strategy==SnapshotStrategy.TIME_BASED
                || snapshotProperties.strategy==SnapshotStrategy.HYBRID) {
                return lastSnapshot.timestamp.plus(snapshotProperties.timeThreshold)
                    .isBefore(Instant.now())
            }
            // 如果没有策略 比较快照版本
            return lastSnapshot.version < currentVersion
        }
        return true
    }

    override suspend fun getLastSnapshot(aggregateId: String, maxVersion: Long?): AggregateSnapshot<*>? {
        return try {
            eventStore.getLastSnapshot(aggregateId, maxVersion)
        } catch (e: Exception) {
            logger.error("error retrieving latest snapshot for aggregate {}: {}",aggregateId, e.message, e)
            null
        }
    }


    override suspend fun createSnapshot(aggregateRoot: EventSourcedAggregateRoot<*>) {
        try {
            val snapshot= aggregateRoot.createSnapshot()
            eventStore.saveSnapshot(snapshot)
            logger.debug("created snapshot for aggregate {} at version {}", aggregateRoot.id, snapshot.version)
            // 异步清除旧快照
            cleanupOldSnapshots(aggregateRoot.id.toString())
        } catch (e: Exception) {
            logger.error("error creating snapshot for aggregate {}: {}", aggregateRoot.id, e.message, e)
            throw SnapshotException("failed to create snapshot", e)
        }
    }


    override suspend fun cleanupOldSnapshots(aggregateId: String) = withContext(Dispatchers.IO) {
        try {
            val allSnapshots = eventStore.getAllSnapshots(aggregateId).sortedByDescending { it.version }
            // 保留最新的n个快照
            val snapshots2Del= allSnapshots.drop(snapshotProperties.maxSnapshotPerAggregate)

            // 删除过期的快照
            val cutoffTime = Instant.now().minus(snapshotProperties.cleanupInterval)
            val expiredSnapshots = allSnapshots.filter {
                it.createdAt.isBefore(cutoffTime)
            }
            // 合并后去重
            val toDelete = (snapshots2Del + expiredSnapshots).distinctBy { it.version }

            toDelete.forEach { snapshot ->
                eventStore.deleteSnapshot(aggregateId, snapshot.version)
                logger.debug("deleted old snapshot for aggregate {} at version {}", aggregateId, snapshot.version)
            }

            if (toDelete.isNotEmpty()) {
                logger.debug("deleted {} old snapshots for aggregate {}", toDelete.size, aggregateId)
            }
        } catch (e: Exception) {
            logger.error("error cleaning up old snapshots for aggregate {}: {}", aggregateId, e.message, e)
        }
    }

    override suspend fun createSnapshotIfNeeded(aggregateRoot: EventSourcedAggregateRoot<*>) {
        if (shouldCreateSnapshot(aggregateRoot)) {
            createSnapshot(aggregateRoot)
        }
    }
}
