package com.github.phanerozoicc.base.domain

import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.base.eventsource.AggregateSnapshot
import java.time.Instant

abstract class EventSourcedAggregateRoot<ID>(
    id: ID
): AggregateRoot<ID>(id) {

    /**
     * 最后的事件版本 用于事件溯源
     */
    private var lastEventVersion: Long = 0

    /**
     * 未提交的事件
     * 代替AggregateRoot中domainEvents的事件暂存职责
     * 但该列表中的事件会应用到聚合根(影响聚合状态)
     */
    private val unCommittedEvents = mutableListOf<DomainEvent>()


    /**
     * 应用领域事件到聚合根
     * 用于聚合重建
     */
    protected abstract fun applyEvent(event: DomainEvent)


    /**
     * 记录新的领域事件
     * 这些事件会应用到聚合根并添加到未提交事件列表中
     */
    protected fun recordEvent(event: DomainEvent) {
        // 填充聚合根信息到领域事件
        val eventWithMetadata = event.copy(
            aggregateId = id.toString(),
            version = getVersion() + 1,
            occurredOn = Instant.now ()
        )

        // 应用事件到聚合根
        applyEvent(eventWithMetadata)

        // 添加事件到未提交事件列表
        unCommittedEvents.add(eventWithMetadata)

        // 更新聚合根版本
        incrementVersion()
        lastEventVersion = getVersion()
    }


    /**
     * 重放事件到聚合根
     * 用于历史事件重建聚合根
     */
    fun replayEvents(events: List<DomainEvent>) {
        events.sortedBy { it.version }.forEach { event ->
            applyEvent(event)
            setVersion(event.version)
            lastEventVersion = event.version
        }
    }

    /**
     * 获取未提交的事件
     */
    fun getUnCommittedEvents() = unCommittedEvents.toList()

    /**
     * 标记所有事件为已提交
     * 一般在事件存储后调用
     */
    fun markEventsAsCommitted() {
        unCommittedEvents.clear()
    }

    /**
     * 获取最后一个事件的版本号
     */
    fun getLastEventVersion() = lastEventVersion

    /**
     * 判断聚合根是否有未提交的事件
     */
    fun hasUnCommittedEvents() = unCommittedEvents.isNotEmpty()

    /**
     * 获取未提交事件的数量
     */
    fun getUnCommittedEventCount() = unCommittedEvents.size


    /**
     * 创建聚合快照
     * 子类重写该方法来实现自定义快照逻辑
     */
    open fun createSnapshot(): AggregateSnapshot<Map<String, Any>> {
        return AggregateSnapshot(
            aggregateId = id.toString(),
            aggregateType = this::class.simpleName?:"unkown",
            version = getVersion(),
            snapshotData = serializeState(this)
        )
    }

    /**
     * 从快照中恢复聚合根
     * 子类重写该方法来实现自定义恢复逻辑
     */
    open fun restoreFromSnapshot(snapshot: AggregateSnapshot<Map<String, Any>>) {
        setVersion(snapshot.version)
        lastEventVersion = snapshot.version
        deserializeState(snapshot.data)
    }

    /**
     * 聚合根状态序列化
     */
    protected abstract fun serializeState(state: Any): Map<String, Any>

    /**
     * 聚合根状态反序列化
     */
    protected abstract fun deserializeState(state: Map<String, Any>): Any
}