package com.github.phanerozoicc.base.eventsource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import mu.KLogging

class SnapshotService(
    private val eventStore: EventStore,
    private val snapshotProperties: SnapshotProperties,
    private val eventReplayService: EventReplayService
) {

    companion object: KLogging()

    // 协程作用域对象
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    // 使用前校验配置
    init {
        snapshotProperties.validate()
        logger.info("snapshotService initialized with config enabled={}, eventCountThreshold={}, timeThreshold={}",
            snapshotProperties.enabled, snapshotProperties.eventCountThreshold, snapshotProperties.timeThreshold)
    }

    fun shouldCreateSnapshot(aggregateId: String): Boolean {
    }

}
