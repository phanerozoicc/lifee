package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TaskId
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 任务取消分配事件
 */
data class TaskUnassignedEvent(
    val taskId: TaskId,
    val previousAssigneeId: UserId,
    val unassignedAt: Instant,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = taskId.value
    override val eventType: String = "TaskUnassigned"
}