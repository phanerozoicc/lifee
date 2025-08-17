package com.lifee.common.eventsourcing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * 快照定时任务调度器
 * 负责定期执行快照清理和维护任务
 */
@Component
@ConditionalOnProperty(
    prefix = "lifee.eventsourcing.snapshot",
    name = ["enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class SnapshotScheduler(
    private val snapshotService: SnapshotService,
    private val snapshotConfig: SnapshotConfig
) {
    
    private val logger = LoggerFactory.getLogger(SnapshotScheduler::class.java)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    
    /**
     * 定期清理旧快照
     * 默认每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点
    fun scheduleSnapshotCleanup() {
        if (!snapshotConfig.enabled) {
            return
        }
        
        logger.info("Starting scheduled snapshot cleanup")
        
        coroutineScope.launch {
            try {
                snapshotService.cleanupOldSnapshots()
                logger.info("Scheduled snapshot cleanup completed successfully")
            } catch (e: Exception) {
                logger.error("Scheduled snapshot cleanup failed", e)
            }
        }
    }
    
    /**
     * 定期创建快照
     * 默认每小时执行一次
     */
    @Scheduled(fixedRateString = "#{@snapshotConfig.timeThreshold.toMillis()}")
    fun scheduleSnapshotCreation() {
        if (!snapshotConfig.enabled) {
            return
        }
        
        logger.debug("Starting scheduled snapshot creation check")
        
        coroutineScope.launch {
            try {
                snapshotService.createSnapshotsForAllAggregates()
                logger.debug("Scheduled snapshot creation check completed")
            } catch (e: Exception) {
                logger.warn("Scheduled snapshot creation failed", e)
            }
        }
    }
    
    /**
     * 快照统计报告
     * 每周一上午9点执行
     */
    @Scheduled(cron = "0 0 9 * * MON") // 每周一上午9点
    fun generateSnapshotReport() {
        if (!snapshotConfig.enabled) {
            return
        }
        
        logger.info("Generating weekly snapshot statistics report")
        
        coroutineScope.launch {
            try {
                val stats = snapshotService.getSnapshotStatistics()
                logger.info("Snapshot Statistics Report: {}", stats)
            } catch (e: Exception) {
                logger.warn("Failed to generate snapshot statistics report", e)
            }
        }
    }
}