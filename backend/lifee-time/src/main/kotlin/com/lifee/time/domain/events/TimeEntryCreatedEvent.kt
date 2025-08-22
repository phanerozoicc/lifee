package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TimeEntryId
import com.lifee.time.domain.ProjectId
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 时间条目创建事件
 */
data class TimeEntryCreatedEvent(
    val timeEntryId: TimeEntryId,
    val userId: UserId,
    val description: String,
    val projectId: ProjectId?,
    val taskId: String?,
    val tags: Set<String>,
    val billable: Boolean,
    val hourlyRate: Double?,
    val createdAt: Instant,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = timeEntryId.value
    override val eventType: String = "TimeEntryCreated"
}