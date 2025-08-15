package com.lifee.config.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.config.domain.valueobjects.ConfigId
import java.time.Instant
import java.util.*

/**
 * 配置发布事件
 */
data class ConfigurationPublishedEvent(
    val configurationId: ConfigId,
    val configVersion: String,
    val publisherId: String,
    val publishTime: Instant,
    val releaseNotes: String
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

/**
 * 配置验证事件
 */
data class ConfigurationValidatedEvent(
    val configurationId: ConfigId,
    val isValid: Boolean,
    val errorCount: Int,
    val validationTimeMs: Long
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

/**
 * 配置回滚事件
 */
data class ConfigurationRolledBackEvent(
    val configurationId: ConfigId,
    val fromVersion: String,
    val toVersion: String,
    val rollbackReason: String,
    val operatorId: String
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

/**
 * 配置通知发送事件
 */
data class ConfigurationNotificationSentEvent(
    val configurationId: ConfigId,
    val notificationType: String,
    val channels: List<String>,
    val recipientCount: Int
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