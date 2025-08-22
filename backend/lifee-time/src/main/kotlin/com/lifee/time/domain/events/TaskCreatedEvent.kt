package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TaskId
import com.lifee.time.domain.ProjectId
import com.lifee.time.domain.TaskPriority
import com.lifee.time.domain.Duration
import com.lifee.user.domain.UserId
import java.time.Instant
import java.time.LocalDate

/**
 * 任务创建事件
 */
data class TaskCreatedEvent(
    val taskId: TaskId,
    val name: String,
    val description: String?,
    val projectId: ProjectId?,
    val assigneeId: UserId?,
    val creatorId: UserId,
    val priority: TaskPriority,
    val tags: Set<String>,
    val dueDate: LocalDate?,
    val estimatedDuration: Duration?,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = taskId.value
    override val eventType: String = "TaskCreated"
}