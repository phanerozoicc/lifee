package com.lifee.common.eventsourcing

import com.lifee.common.domain.AggregateSnapshot
import com.lifee.common.domain.EventSourcedAggregateRoot

/**
 * 快照服务接口
 */
interface SnapshotService {
    
    /**
     * 为聚合根创建快照
     */
    suspend fun createSnapshot(aggregate: EventSourcedAggregateRoot<*>)
    
    /**
     * 获取最新快照
     */
    suspend fun getLatestSnapshot(aggregateId: String, maxVersion: Long? = null): AggregateSnapshot<*>?
    
    /**
     * 判断是否需要创建快照
     */
    suspend fun shouldCreateSnapshot(aggregate: EventSourcedAggregateRoot<*>): Boolean
    
    /**
     * 清理旧快照
     */
    suspend fun cleanupOldSnapshots(aggregateId: String)
    
    /**
     * 获取快照统计信息
     */
    suspend fun getSnapshotStats(aggregateId: String): SnapshotStats
}