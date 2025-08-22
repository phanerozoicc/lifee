package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TaskId
import java.time.Instant

/**
 * 任务取消事件
 */
data class TaskCancelledEvent(
    val taskId: TaskId,
    val cancelledAt: Instant,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = taskId.value
    override val eventType: String = "TaskCancelled"
}