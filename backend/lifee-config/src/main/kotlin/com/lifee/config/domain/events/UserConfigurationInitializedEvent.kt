package com.lifee.config.domain.events

import com.lifee.common.domain.DomainEvent
import java.time.Instant
import java.util.*

/**
 * 用户配置初始化完成事件
 */
data class UserConfigurationInitializedEvent(
    val userId: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val initializedAt: Instant = Instant.now()
) : DomainEvent(
    aggregateId = userId,
    occurredOn = Instant.now(),
    eventId = UUID.randomUUID()
) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return this.copy(
            userId = aggregateId
        )
    }
}