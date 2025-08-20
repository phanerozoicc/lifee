package com.github.phanerozoicc.base.domain

import com.github.phanerozoicc.base.event.DomainEvent

/**
 * 聚合根基类
 * 提供领域事件管理功能
 */
abstract class AggregateRoot<ID>(
    val id: ID
) {

    private val domainEvents = mutableListOf<DomainEvent>()

    /**
     * 聚合版本
     * 用于并发控制和幂等处理
     */
    private var version: Long = 0

    fun addDomainEvent(event: DomainEvent) {
        domainEvents.add(event)
    }

    fun getDomainEvents() = domainEvents.toList()

    fun clearDomainEvents() {
        domainEvents.clear()
    }

    fun getVersion() = version

    fun setVersion(version: Long) {
        this.version = version
    }

    protected fun incrementVersion() {
        version++
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