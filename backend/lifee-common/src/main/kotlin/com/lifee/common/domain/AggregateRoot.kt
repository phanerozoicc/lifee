package com.lifee.common.domain

/**
 * 聚合根基类
 * 所有聚合根都应该继承此类
 * 
 * @param ID 聚合根标识符类型
 */
abstract class AggregateRoot<ID>(
    private val _id: ID
) {
    private val domainEvents = mutableListOf<DomainEvent>()
    private var version: Long = 0
    
    /**
     * 添加领域事件
     * 
     * @param event 要添加的领域事件
     */
    protected fun addDomainEvent(event: DomainEvent) {
        domainEvents.add(event)
    }
    
    /**
     * 获取所有未提交的领域事件
     * 
     * @return 领域事件列表
     */
    fun getDomainEvents(): List<DomainEvent> = domainEvents.toList()
    
    /**
     * 清除所有领域事件
     * 通常在事件发布后调用
     */
    fun clearDomainEvents() {
        domainEvents.clear()
    }
    
    /**
     * 获取聚合根标识符
     * 
     * @return 聚合根ID
     */
    fun getId(): ID = _id
    
    /**
     * 获取聚合根版本
     * 
     * @return 版本号
     */
    fun getVersion(): Long = version
    
    /**
     * 设置聚合根版本
     * 通常在从事件存储重建聚合根时使用
     * 
     * @param version 版本号
     */
    fun setVersion(version: Long) {
        this.version = version
    }
    
    /**
     * 增加版本号
     */
    protected fun incrementVersion() {
        this.version++
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AggregateRoot<*>) return false
        return _id == other._id
    }
    
    override fun hashCode(): Int {
        return _id?.hashCode() ?: 0
    }
    
    override fun toString(): String {
        return "${this::class.simpleName}(id=$_id, version=$version)"
    }
}