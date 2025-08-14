package com.github.phanerozoicc.base.domain

import com.github.phanerozoicc.base.event.DomainEvent

/**
 * 聚合根基类
 * 提供领域事件管理功能
 */
abstract class AggregateRoot<T>(
    val id: T
) {

    private val domainEvents = mutableListOf<DomainEvent>()

    fun addDomainEvent(event: DomainEvent) {
        domainEvents.add(event)
    }

    fun getDomainEvents() = domainEvents.toList()

    fun clearDomainEvents() {
        domainEvents.clear()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AggregateRoot<*>) return false
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
    
    override fun toString(): String {
        return "${this::class.simpleName}(id=$id)"
    }
}