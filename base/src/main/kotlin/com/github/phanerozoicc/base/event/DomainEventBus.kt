package com.github.phanerozoicc.base.event

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KLogging

interface DomainEventBus {

    /**
     * 发布事件
     */
    fun publish(event: Event)

    /**
     * 批量发布事件
     */
    fun publishAll(events: List<Event>)
}

class DefaultDomainEventBus: DomainEventBus {

    companion object: KLogging()

    override fun publish(event: Event) {
        logger.debug("publishing event:{}", event::class.simpleName)
        if (event is DomainEvent) {
            // 存储事件
            persistDomainEvent(event)
        }

    }

    private suspend fun persistDomainEvent(event: DomainEvent) {
        try {
            coroutineScope {
                async(Dispatchers.IO) {

                }
            }
        }catch (e: Exception) {

        }
        TODO("Not yet implemented")
    }

    override fun publishAll(events: List<Event>) {
        TODO("Not yet implemented")
    }

}