package com.github.phanerozoicc.base.event

import com.github.phanerozoicc.base.eventsource.EventStore
import com.github.phanerozoicc.base.utils.JsonUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mu.KLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
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


// TODO 临时直接注入
@Component
class DefaultEventBus(
    val eventStore: EventStore?,
    val kafkaTemplate: KafkaTemplate<String, Any>
): EventBus {

    private val localHandlers = ConcurrentHashMap<KClass<out Event>, MutableList<EventHandler<Event, *>>>()

    companion object: KLogging()

    override fun publish(event: Event) {
        logger.debug("publishing event:{}", event::class.simpleName)
        if (event is DomainEvent) {
            // 存储事件
            persistDomainEvent(event)
        }

        // 处理本地事件
        handleLocalEvent(event)

        // 远程事件发布到kafka
        publish2Kafka(event)

    }

    private fun publish2Kafka(event: Event) {
        val topic = "event-${event::class.simpleName?.lowercase()}"
        val eventJson = JsonUtil.writeValueAsString(event)

        kafkaTemplate.send(topic, eventJson).whenComplete { result, ex ->
                if (ex == null) {
                    logger.debug("event {} published to kafka topic:{}", event::class.simpleName, topic)
                } else {
                    logger.error("failed to publish event {} to kafka topic:{}, error:{}", event::class.simpleName,
                        topic, ex.message, ex)
                }
            }
    }

    private fun handleLocalEvent(event: Event) {
        val handlers = localHandlers[event::class]
        handlers?.forEach { handler ->
            try {
                val res = handler.onEvent(event)
                logger.debug("event {} handler by local handler result:{}", event::class.simpleName, res)
            } catch (e: Exception) {
                logger.error("failed to handle event {} by local handler:{}", event::class.simpleName, e.message, e)
            }
        }
    }

    /**
     * 持久化领域事件
     */
    private fun persistDomainEvent(event: DomainEvent) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                val currentVersion = eventStore?.getCurrentVersion(event.aggregateId)?:0
                eventStore?.saveEvents(event.aggregateId, listOf<DomainEvent>(event), currentVersion)
                logger.debug("Domain event {} persisted to event store", event::class.simpleName)
            }
        }catch (e: Exception) {
            logger.error("failed to persist domain event {} to event store:{}", event::class.simpleName, e.message, e)
            // TODO 暂时忽略异常 避免影响事件发布
        }
    }

    override fun publishAll(events: List<Event>) {
        logger.debug("publishing {} events", events.size)

        // 持久化领域事件
        val domainEvents = events.filterIsInstance<DomainEvent>()
        if (domainEvents.isNotEmpty()) {
            persistDomainEvents(domainEvents)
        }
        events.forEach { event ->
            // 本地事件处理
            handleLocalEvent(event)
            // 发布到Kafka
            publish2Kafka(event)
        }
    }

    /**
     * 批量持久化领域事件
     */
    private fun persistDomainEvents(domainEvents: List<DomainEvent>) {
        try {
            val eventByAggregate = domainEvents.groupBy { it.aggregateId }
            CoroutineScope(Dispatchers.IO).launch {
                eventByAggregate.forEach { (aggregateId, aggregateEvents) ->
                    // 获取当前版本
                    val currentVersion = eventStore?.getCurrentVersion(aggregateId)?:0
                    // 保存事件
                    eventStore?.saveEvents(aggregateId, aggregateEvents, currentVersion)
                }
            }
        } catch (e: Exception) {
            logger.error("failed to persist domain events to event store:{}", e.message, e)
            // TODO 暂时忽略异常 避免影响事件发布
        }
    }

}