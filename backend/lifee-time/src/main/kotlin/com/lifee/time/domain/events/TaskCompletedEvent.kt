package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TaskId
import com.lifee.time.domain.Duration
import java.time.Instant

/**
 * 任务完成事件
 */
data class TaskCompletedEvent(
    val taskId: TaskId,
    val completedAt: Instant,
    val actualDuration: Duration?,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = taskId.value
    override val eventType: String = "TaskCompleted"
}