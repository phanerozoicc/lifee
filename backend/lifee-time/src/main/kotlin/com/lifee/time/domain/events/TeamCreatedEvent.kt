package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TeamId
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 团队创建事件
 */
data class TeamCreatedEvent(
    val teamId: TeamId,
    val name: String,
    val description: String?,
    val ownerId: UserId,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = teamId.value
    override val eventType: String = "TeamCreated"
}