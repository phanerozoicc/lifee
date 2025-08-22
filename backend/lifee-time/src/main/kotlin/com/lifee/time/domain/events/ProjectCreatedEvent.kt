package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.ProjectId
import com.lifee.user.domain.UserId
import java.math.BigDecimal
import java.time.Instant

/**
 * 项目创建事件
 */
data class ProjectCreatedEvent(
    val projectId: ProjectId,
    val name: String,
    val description: String?,
    val color: String,
    val ownerId: UserId,
    val clientName: String?,
    val hourlyRate: BigDecimal?,
    val currency: String,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = projectId.value
    override val eventType: String = "ProjectCreated"
}