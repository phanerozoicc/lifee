package com.github.phanerozoicc.base.eventsource

import com.github.phanerozoicc.base.domain.EventSourcedAggregateRoot

interface SnapshotService {

    /**
     * 判断是否需要创建快照
     */
    suspend fun shouldCreateSnapshot(aggregate: EventSourcedAggregateRoot<*>): Boolean

    /**
     * 为聚合根创建快照
     */
    suspend fun createSnapshot(aggregateRoot: EventSourcedAggregateRoot<*>)

    /**
     * 为聚合根创建快照，如果聚合根不需要创建快照则不执行任何操作
     */
    suspend fun createSnapshotIfNeeded(aggregateRoot: EventSourcedAggregateRoot<*>)

    /**
     * 获取最新的快照
     * @param aggregateId 聚合根ID
     * @param maxVersion 最大版本号
     */
    suspend fun getLastSnapshot(aggregateId: String, maxVersion: Long?): AggregateSnapshot<*>?

    /**
     * 清理旧的快照
     */
    suspend fun cleanupOldSnapshots(aggregateId: String)
}
