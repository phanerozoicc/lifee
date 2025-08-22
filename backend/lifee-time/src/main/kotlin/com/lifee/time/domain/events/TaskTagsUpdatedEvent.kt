package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TaskId
import java.time.Instant

/**
 * 任务标签更新事件
 */
data class TaskTagsUpdatedEvent(
    val taskId: TaskId,
    val tags: Set<String>,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = taskId.value
    override val eventType: String = "TaskTagsUpdated"
}