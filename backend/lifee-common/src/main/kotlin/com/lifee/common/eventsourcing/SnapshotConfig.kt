package com.lifee.common.eventsourcing

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.Duration

/**
 * 快照配置属性
 */
@Configuration
@ConfigurationProperties(prefix = "lifee.eventsourcing.snapshot")
data class SnapshotConfig(
    /**
     * 是否启用快照功能
     */
    var enabled: Boolean = true,
    
    /**
     * 触发快照创建的事件数量阈值
     */
    var eventCountThreshold: Int = 100,
    
    /**
     * 触发快照创建的时间间隔
     */
    var timeThreshold: Duration = Duration.ofHours(24),
    
    /**
     * 每个聚合根保留的最大快照数量
     */
    var maxSnapshotsPerAggregate: Int = 5,
    
    /**
     * 快照清理间隔
     */
    var cleanupInterval: Duration = Duration.ofDays(7),
    
    /**
     * 快照压缩配置
     */
    var compression: CompressionConfig = CompressionConfig(),
    
    /**
     * 异步快照创建配置
     */
    var async: AsyncConfig = AsyncConfig()
) {
    
    /**
     * 压缩配置
     */
    data class CompressionConfig(
        /**
         * 是否启用压缩
         */
        var enabled: Boolean = true,
        
        /**
         * 压缩算法 (gzip, lz4, snappy)
         */
        var algorithm: String = "gzip",
        
        /**
         * 压缩级别 (1-9)
         */
        var level: Int = 6
    )
    
    /**
     * 异步配置
     */
    data class AsyncConfig(
        /**
         * 是否启用异步快照创建
         */
        var enabled: Boolean = true,
        
        /**
         * 线程池大小
         */
        var threadPoolSize: Int = 2,
        
        /**
         * 队列容量
         */
        var queueCapacity: Int = 1000,
        
        /**
         * 批处理大小
         */
        var batchSize: Int = 10
    )
    
    /**
     * 验证配置的有效性
     */
    fun validate() {
        require(eventCountThreshold > 0) { "Event count threshold must be positive" }
        require(maxSnapshotsPerAggregate > 0) { "Max snapshots per aggregate must be positive" }
        require(timeThreshold.toMillis() > 0) { "Time threshold must be positive" }
        require(cleanupInterval.toMillis() > 0) { "Cleanup interval must be positive" }
        
        if (compression.enabled) {
            require(compression.algorithm in listOf("gzip", "lz4", "snappy")) {
                "Unsupported compression algorithm: ${compression.algorithm}"
            }
            require(compression.level in 1..9) {
                "Compression level must be between 1 and 9"
            }
        }
        
        if (async.enabled) {
            require(async.threadPoolSize > 0) { "Thread pool size must be positive" }
            require(async.queueCapacity > 0) { "Queue capacity must be positive" }
            require(async.batchSize > 0) { "Batch size must be positive" }
        }
    }
}

/**
 * 快照策略枚举
 */
enum class SnapshotStrategy {
    /**
     * 基于事件数量的策略
     */
    EVENT_COUNT,
    
    /**
     * 基于时间的策略
     */
    TIME_BASED,
    
    /**
     * 混合策略（事件数量和时间）
     */
    HYBRID,
    
    /**
     * 手动策略
     */
    MANUAL
}

/**
 * 快照清理策略
 */
enum class SnapshotCleanupStrategy {
    /**
     * 保留最新的N个快照
     */
    KEEP_LATEST,
    
    /**
     * 保留指定时间内的快照
     */
    KEEP_BY_TIME,
    
    /**
     * 混合策略
     */
    HYBRID
}