package com.lifee.common.domain

import java.time.Instant

/**
 * 聚合根快照
 * 用于存储聚合根在特定版本的状态
 */
data class AggregateSnapshot<T>(
    /**
     * 聚合根ID
     */
    val aggregateId: String,
    
    /**
     * 聚合根类型
     */
    val aggregateType: String,
    
    /**
     * 快照对应的聚合根版本
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
     * 快照时间戳（兼容性属性）
     */
    val timestamp: Instant = createdAt
) {
    /**
     * 数据属性（兼容性属性）
     */
    val data: T
        get() = snapshotData
}