package com.lifee.common.domain

import java.time.Instant
import java.util.*

/**
 * 事件溯源聚合根基类
 * 支持从事件重建聚合根状态
 * 
 * @param ID 聚合根标识符类型
 */
abstract class EventSourcedAggregateRoot<ID>(
    id: ID
) : AggregateRoot<ID>(id) {
    
    private val uncommittedEvents = mutableListOf<DomainEvent>()
    private var lastEventVersion: Long = 0
    
    /**
     * 应用领域事件到聚合根
     * 子类需要实现此方法来处理具体的事件类型
     * 
     * @param event 要应用的领域事件
     */
    protected abstract fun applyEvent(event: DomainEvent)
    
    /**
     * 记录新的领域事件
     * 会自动应用事件并添加到未提交事件列表
     * 
     * @param event 要记录的领域事件
     */
    protected fun recordEvent(event: DomainEvent) {
        // 设置事件的聚合根ID和版本
        val eventWithMetadata = event.copy(
            aggregateId = getId().toString(),
            version = getVersion() + 1,
            occurredOn = Instant.now(),
            eventId = UUID.randomUUID()
        )
        
        // 应用事件到聚合根状态
        applyEvent(eventWithMetadata)
        
        // 添加到未提交事件列表
        uncommittedEvents.add(eventWithMetadata)
        
        // 增加聚合根版本
        incrementVersion()
        lastEventVersion = getVersion()
    }
    
    /**
     * 从历史事件重建聚合根状态
     * 
     * @param events 历史事件列表
     */
    fun replayEvents(events: List<DomainEvent>) {
        events.sortedBy { it.version }.forEach { event ->
            applyEvent(event)
            setVersion(event.version)
            lastEventVersion = event.version
        }
    }
    
    /**
     * 获取所有未提交的事件
     * 
     * @return 未提交事件列表
     */
    fun getUncommittedEvents(): List<DomainEvent> = uncommittedEvents.toList()
    
    /**
     * 标记所有事件为已提交
     * 通常在事件成功保存到事件存储后调用
     */
    fun markEventsAsCommitted() {
        uncommittedEvents.clear()
    }
    
    /**
     * 获取最后一个事件的版本号
     * 
     * @return 最后事件版本号
     */
    fun getLastEventVersion(): Long = lastEventVersion
    
    /**
     * 检查是否有未提交的事件
     * 
     * @return 如果有未提交事件返回true
     */
    fun hasUncommittedEvents(): Boolean = uncommittedEvents.isNotEmpty()
    
    /**
     * 获取未提交事件的数量
     * 
     * @return 未提交事件数量
     */
    fun getUncommittedEventsCount(): Int = uncommittedEvents.size
    
    /**
     * 创建聚合根快照
     * 子类可以重写此方法来提供自定义快照逻辑
     * 
     * @return 聚合根快照
     */
    open fun createSnapshot(): AggregateSnapshot<ID> {
        return AggregateSnapshot(
            aggregateId = getId(),
            aggregateType = this::class.simpleName ?: "Unknown",
            version = getVersion(),
            snapshotData = serializeState(),
            createdAt = Instant.now()
        )
    }
    
    /**
     * 从快照恢复聚合根状态
     * 子类需要实现此方法来反序列化状态
     * 
     * @param snapshot 聚合根快照
     */
    open fun restoreFromSnapshot(snapshot: AggregateSnapshot<ID>) {
        setVersion(snapshot.version)
        lastEventVersion = snapshot.version
        deserializeState(snapshot.snapshotData)
    }
    
    /**
     * 序列化聚合根状态
     * 子类需要实现此方法来提供状态序列化逻辑
     * 
     * @return 序列化后的状态数据
     */
    protected abstract fun serializeState(): Map<String, Any>
    
    /**
     * 反序列化聚合根状态
     * 子类需要实现此方法来提供状态反序列化逻辑
     * 
     * @param stateData 状态数据
     */
    protected abstract fun deserializeState(stateData: Map<String, Any>)
}

/**
 * 聚合根快照
 * 
 * @param ID 聚合根标识符类型
 */
data class AggregateSnapshot<ID>(
    val aggregateId: ID,
    val aggregateType: String,
    val version: Long,
    val snapshotData: Map<String, Any>,
    val createdAt: Instant
)