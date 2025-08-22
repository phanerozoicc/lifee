package com.lifee.time.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.time.domain.ProjectId
import java.math.BigDecimal
import java.time.Instant

/**
 * 项目时薪更新事件
 */
data class ProjectHourlyRateUpdatedEvent(
    val projectId: ProjectId,
    val hourlyRate: BigDecimal?,
    val currency: String,
    override val occurredAt: Instant = Instant.now()
) : DomainEvent {
    override val aggregateId: String = projectId.value
    override val eventType: String = "ProjectHourlyRateUpdated"
}