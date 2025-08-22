package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.ProjectId
import java.time.Instant

/**
 * 项目恢复事件
 */
data class ProjectRestoredEvent(
    val projectId: ProjectId,
    val restoredAt: Instant,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = projectId.value
    override val eventType: String = "ProjectRestored"
}