package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TeamId
import java.time.Instant

/**
 * 团队更新事件
 */
data class TeamUpdatedEvent(
    val teamId: TeamId,
    val name: String,
    val description: String?,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = teamId.value
    override val eventType: String = "TeamUpdated"
}