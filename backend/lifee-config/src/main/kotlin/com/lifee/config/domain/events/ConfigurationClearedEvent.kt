package com.lifee.config.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.config.domain.valueobjects.ConfigId
import com.lifee.config.domain.valueobjects.Environment
import java.time.Instant
import java.util.*

/**
 * 配置清空事件
 */
data class ConfigurationClearedEvent(
    val configurationId: ConfigId,
    val namespace: String,
    val environment: Environment
) : DomainEvent(
    aggregateId = configurationId.value.toString(),
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
            configurationId = ConfigId(UUID.fromString(aggregateId))
        )
    }
}