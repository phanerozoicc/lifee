package com.github.phanerozoicc.base.event

import com.github.phanerozoicc.base.eventsource.EventStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KLogging
import java.beans.EventHandler
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

interface EventBus {

    /**
     * 发布事件
     */
    fun publish(event: Event)

    /**
     * 批量发布事件
     */
    fun publishAll(events: List<Event>)
}

class DefaultEventBus(
    val eventStore: EventStore,
): EventBus {

    private val localHandlers = ConcurrentHashMap<KClass<out Event>, MutableList<EventHandler>>()

    companion object: KLogging()

    override fun publish(event: Event) {
        logger.debug("publishing event:{}", event::class.simpleName)
        if (event is Event) {
            // 存储事件
            persistDomainEvent(event)
        }

        // 处理本地事件
        handleLocalEvent(event)

        // 远程事件发布到kafka
        publish2Kafka(event)

    }

    private fun handleLocalEvent(event: Event) {
        val handlers = localHandlers[event::class]
        handlers?.forEach { handler ->
            try {
                handler.action
            }
        }
    }

    /**
     * 持久化领域事件
     */
    private fun persistDomainEvent(event: Event) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val currentVersion = eventStore.getCurrentVersion(event.aggregateId)?:0
                eventStore.saveEvents(event.aggregateId, listOf<Event>(event), currentVersion)
                logger.debug("Domain event {} persisted to event store", event::class.simpleName)
            }
        }catch (e: Exception) {
            logger.error("failed to persist domain event {} to event store:{}", event::class.simpleName, e.message, e)
            // TODO 暂时忽略异常 避免影响事件发布
        }
    }

    override fun publishAll(events: List<Event>) {
        TODO("Not yet implemented")
    }

}