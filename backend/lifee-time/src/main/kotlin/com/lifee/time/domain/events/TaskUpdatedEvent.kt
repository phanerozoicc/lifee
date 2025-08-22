package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TaskId
import com.lifee.time.domain.TaskPriority
import com.lifee.time.domain.Duration
import java.time.Instant
import java.time.LocalDate

/**
 * 任务更新事件
 */
data class TaskUpdatedEvent(
    val taskId: TaskId,
    val name: String,
    val description: String?,
    val priority: TaskPriority,
    val dueDate: LocalDate?,
    val estimatedDuration: Duration?,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = taskId.value
    override val eventType: String = "TaskUpdated"
}