package com.github.phanerozoicc.base.eventsource

import java.time.Instant

class AggregateSnapshot<T>(
    /**
     * Aggregate id
     */
    val aggregateId: String,
    /**
     * 聚合类型
     */
    val aggregateType: String,
    /**
     * 快照对应的聚合根lastEvent版本号
     */
    val version: Long,
    /**
     * 快照数据
     */
    val snapshotData: T,

    /**
     * 快照创建时间
     */
    val createdAt: Instant = Instant.now(),

    /**
     * 快照时间戳
     */
    val timestamp: Instant = createdAt
) {
    // 兼容性属性
    val data = snapshotData
}
