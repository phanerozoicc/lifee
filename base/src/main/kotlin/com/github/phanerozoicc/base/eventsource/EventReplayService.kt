package com.github.phanerozoicc.base.eventsource

import mu.KLogging

/**
 * 事件重放服务
 * 负责从事件存储重建到聚合根状态
 */
class EventReplayService(
    private val eventStore: EventStore,
) {
    companion object: KLogging()


}