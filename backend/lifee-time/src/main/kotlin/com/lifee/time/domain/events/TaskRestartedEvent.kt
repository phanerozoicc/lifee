package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TaskId
import java.time.Instant

/**
 * 任务重启事件
 */
data class TaskRestartedEvent(
    val taskId: TaskId,
    val restartedAt: Instant,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = taskId.value
    override val eventType: String = "TaskRestarted"
}