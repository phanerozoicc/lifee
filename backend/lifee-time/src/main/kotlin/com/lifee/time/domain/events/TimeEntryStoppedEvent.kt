package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TimeEntryId
import com.lifee.time.domain.Duration
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 时间条目停止事件
 */
data class TimeEntryStoppedEvent(
    val timeEntryId: TimeEntryId,
    val userId: UserId,
    val endTime: Instant,
    val duration: Duration,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = timeEntryId.value
    override val eventType: String = "TimeEntryStopped"
}