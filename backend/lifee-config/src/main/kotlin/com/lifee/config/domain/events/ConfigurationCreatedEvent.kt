package com.lifee.config.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.config.domain.valueobjects.ConfigId
import com.lifee.config.domain.valueobjects.Environment
import java.time.Instant
import java.util.*

/**
 * 配置创建事件
 */
class ConfigurationCreatedEvent(
    val configurationId: ConfigId,
    val namespace: String,
    val environment: Environment
) : DomainEvent(configurationId.toString()) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return ConfigurationCreatedEvent(
            configurationId = ConfigId.from(aggregateId),
            namespace = this.namespace,
            environment = this.environment
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConfigurationCreatedEvent) return false
        if (!super.equals(other)) return false
        return configurationId == other.configurationId &&
               namespace == other.namespace &&
               environment == other.environment
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + configurationId.hashCode()
        result = 31 * result + namespace.hashCode()
        result = 31 * result + environment.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "ConfigurationCreatedEvent(configurationId=$configurationId, namespace='$namespace', environment=$environment, ${super.toString()})"
    }
}