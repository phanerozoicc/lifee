package com.github.phanerozoicc.base.eventsource

import com.github.phanerozoicc.base.event.Event

/**
 * 默认使用postgres实现
 */
interface EventStore {

    /**
     * 根据aggregateId获取aggregate当前版本
     */
    suspend fun getCurrentVersion(aggregateId: String): Long?

    /**
     * 保存事件到事件存储
     */
    suspend fun saveEvents(aggregateId: String, events: List<Event>, expectedVersion: Long)

}

