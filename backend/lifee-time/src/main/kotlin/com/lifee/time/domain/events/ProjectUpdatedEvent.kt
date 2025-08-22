package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.ProjectId
import java.time.Instant

/**
 * 项目更新事件
 */
data class ProjectUpdatedEvent(
    val projectId: ProjectId,
    val name: String,
    val description: String?,
    val color: String,
    val clientName: String?,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = projectId.value
    override val eventType: String = "ProjectUpdated"
}