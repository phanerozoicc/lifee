package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.TeamId
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 团队成员移除事件
 */
data class TeamMemberRemovedEvent(
    val teamId: TeamId,
    val userId: UserId,
    val removedBy: UserId,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = teamId.value
    override val eventType: String = "TeamMemberRemoved"
}