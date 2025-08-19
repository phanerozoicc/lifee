package com.github.phanerozoicc.base.eventsource

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@ConfigurationProperties(prefix = "lifee.event-sourcing.snapshot")
data class SnapshotProperties(
    /**
     * 是否启用快照
     */
    var enabled: Boolean = true,

    /**
     * 快照策略
     */
    var strategy: SnapshotStrategy = SnapshotStrategy.EVENT_COUNT,

    /**
     * 触发快照创建的事件数量阈值
     */
    var eventCountThreshold: Int = 100,

    /**
     * 触发快照创建的间隔时间阈值
     */
    var timeThreshold: Duration = Duration.ofHours(24),

    /**
     * 每个聚合根的快照数量限制
     */
    var maxSnapshotPerAggregate: Int = 5,

    /**
     * 快照清理间隔
     */
    var cleanupInterval: Duration = Duration.ofDays(7),

    /**
     * 快照清理策略
     */
    var cleanupStrategy: SnapshotCleanupStrategy = SnapshotCleanupStrategy.KEEP_LATEST,

    /**
     * 快照压缩配置
     */
    var compression: CompressionProperties = CompressionProperties(),

    /**
     * 异步快照创建策略
     */
    var async: AsyncProperties = AsyncProperties(),
) {
    fun validate() {
        require(eventCountThreshold > 0) { "eventCountThreshold must be greater than 0"}
        require(maxSnapshotPerAggregate > 0) { "maxSnapshotPerAggregate must be greater than 0"}
        require(timeThreshold.toMillis() > 0) { "timeThreshold must be greater than 0"}
        require(cleanupInterval.toMillis() > 0) { "cleanupInterval must be positive"}

        if(compression.enabled) {
            require(compression.algorithm in listOf<String>("gzip", "lz4", "snappy")) {
                "compression.algorithm must be one of gzip, lz4, snappy"
            }
            require(compression.level in 1..9) {
                "compression.level must be between 1 and 9"
            }
        }

        if(async.enabled) {
            require(async.threadPoolSize > 0) { "async.threadPoolSize must be greater than 0"}
            require(async.queueCapacity > 0) { "async.queueCapacity must be greater than 0"}
            require(async.batchSize > 0) { "async.batchSize must be greater than 0"}
        }

    }

}

/**
 * 快照压缩配置
 */
data class CompressionProperties(
    /**
     * 是否启用快照压缩
     */
    var enabled: Boolean = false,

    /**
     * 快照压缩算法
     */
    var algorithm: String = "gzip",

    /**
     * 快照压缩级别(1-9)
     */
    var level: Int = 6
)


/**
 * 异步快照创建配置
 */
data class AsyncProperties(
    /**
     * 是否启用异步快照创建
     */
    var enabled: Boolean = false,

    /**
     * 异步快照创建线程池大小
     */
    var threadPoolSize: Int = 2,

    /**
     * 异步快照创建队列容量
     */
    var queueCapacity: Int = 1000,

    /**
     * 批处理大小
     */
    var batchSize: Int = 10
)

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
