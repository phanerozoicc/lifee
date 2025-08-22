package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TeamId
import com.lifee.time.domain.TeamRole
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 团队成员添加事件
 */
data class TeamMemberAddedEvent(
    val teamId: TeamId,
    val userId: UserId,
    val role: TeamRole,
    val invitedBy: UserId,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = teamId.value
    override val eventType: String = "TeamMemberAdded"
}