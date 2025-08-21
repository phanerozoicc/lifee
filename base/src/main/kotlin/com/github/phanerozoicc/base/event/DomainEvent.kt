package com.github.phanerozoicc.base.event

import java.time.Instant
import java.util.*

abstract class DomainEvent(
    // 聚合根id
    open val aggregateId: String,
    // 事件版本号
    open val version: Long = 0,
//    // 事件类型
//    open val eventType: String,
    // 事件id
    open val eventId: String = UUID.randomUUID().toString(),
    // 事件发生时间
    open val occurredOn: Instant = Instant.now(),
): Event {

    open val eventType: String = this::class.simpleName ?: ""

    override fun toString(): String {
        return "DomainEvent(aggregateId='$aggregateId', version=$version, eventType='$eventType', eventId='$eventId', occurredOn=$occurredOn)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DomainEvent) return false
        return eventId == other.eventId
    }

    override fun hashCode(): Int {
        return aggregateId.hashCode()
    }

    abstract fun copy(aggregateId: String, version: Long, occurredOn: Instant): DomainEvent
}


interface Event