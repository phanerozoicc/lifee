package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TeamId
import com.lifee.time.domain.TeamRole
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 团队成员角色更新事件
 */
data class TeamMemberRoleUpdatedEvent(
    val teamId: TeamId,
    val userId: UserId,
    val oldRole: TeamRole,
    val newRole: TeamRole,
    val updatedBy: UserId,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = teamId.value
    override val eventType: String = "TeamMemberRoleUpdated"
}