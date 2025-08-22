package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TimeEntryId
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 时间条目恢复事件
 */
data class TimeEntryResumedEvent(
    val timeEntryId: TimeEntryId,
    val userId: UserId,
    val resumeTime: Instant,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = timeEntryId.value
    override val eventType: String = "TimeEntryResumed"
}