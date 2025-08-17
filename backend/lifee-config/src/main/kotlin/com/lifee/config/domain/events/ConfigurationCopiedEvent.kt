package com.lifee.config.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.config.domain.valueobjects.ConfigId
import com.lifee.config.domain.valueobjects.Environment
import java.time.Instant
import java.util.*

/**
 * 配置复制事件
 */
data class ConfigurationCopiedEvent(
    val sourceConfigurationId: ConfigId,
    val targetConfigurationId: ConfigId,
    val sourceEnvironment: Environment,
    val targetEnvironment: Environment,
    val namespace: String
) : DomainEvent(
    aggregateId = targetConfigurationId.value.toString(),
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
            targetConfigurationId = ConfigId(UUID.fromString(aggregateId))
        )
    }
}