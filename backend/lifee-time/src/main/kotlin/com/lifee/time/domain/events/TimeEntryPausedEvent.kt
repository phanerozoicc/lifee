package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TimeEntryId
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 时间条目暂停事件
 */
data class TimeEntryPausedEvent(
    val timeEntryId: TimeEntryId,
    val userId: UserId,
    val pauseTime: Instant,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = timeEntryId.value
    override val eventType: String = "TimeEntryPaused"
}