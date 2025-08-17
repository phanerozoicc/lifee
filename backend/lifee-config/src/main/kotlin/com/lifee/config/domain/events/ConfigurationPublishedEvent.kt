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
        return ConfigurationPublishedEvent(
            configurationId = ConfigId(UUID.fromString(aggregateId)),
            configVersion = this.configVersion,
            publisherId = this.publisherId,
            publishTime = this.publishTime,
            releaseNotes = this.releaseNotes
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
        return ConfigurationValidatedEvent(
            configurationId = ConfigId(UUID.fromString(aggregateId)),
            isValid = this.isValid,
            errorCount = this.errorCount,
            validationTimeMs = this.validationTimeMs
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
        return ConfigurationRolledBackEvent(
            configurationId = ConfigId(UUID.fromString(aggregateId)),
            fromVersion = this.fromVersion,
            toVersion = this.toVersion,
            rollbackReason = this.rollbackReason,
            operatorId = this.operatorId
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
        return ConfigurationNotificationSentEvent(
            configurationId = ConfigId(UUID.fromString(aggregateId)),
            notificationType = this.notificationType,
            channels = this.channels,
            recipientCount = this.recipientCount
        )
    }
}