package com.lifee.common.eventsourcing

import java.time.Instant

/**
 * 快照统计信息
 */
data class SnapshotStats(
    /**
     * 总快照数量
     */
    val totalSnapshots: Int = 0,
    
    /**
     * 按聚合根类型分组的快照数量
     */
    val aggregateTypes: Map<String, Int> = emptyMap(),
    
    /**
     * 最旧快照时间
     */
    val oldestSnapshot: Instant? = null,
    
    /**
     * 最新快照时间
     */
    val newestSnapshot: Instant? = null,
    
    /**
     * 有快照的聚合根数量
     */
    val aggregatesWithSnapshots: Int = 0,
    
    /**
     * 每个聚合根的平均快照数量
     */
    val averageSnapshotsPerAggregate: Double = 0.0,
    
    /**
     * 最旧快照的年龄（毫秒）
     */
    val oldestSnapshotAge: Long = 0L,
    
    /**
     * 最新快照的年龄（毫秒）
     */
    val newestSnapshotAge: Long = 0L,
    
    /**
     * 快照总大小（字节）
     */
    val totalSnapshotSize: Long = 0L
)