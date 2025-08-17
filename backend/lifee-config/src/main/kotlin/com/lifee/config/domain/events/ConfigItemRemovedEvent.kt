package com.lifee.config.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.config.domain.valueobjects.ConfigId
import com.lifee.config.domain.valueobjects.ConfigKey
import com.lifee.config.domain.valueobjects.Environment
import java.time.Instant
import java.util.*

/**
 * 配置项移除事件
 */
class ConfigItemRemovedEvent(
    val configurationId: ConfigId,
    val key: ConfigKey,
    val environment: Environment
) : DomainEvent(configurationId.toString()) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return ConfigItemRemovedEvent(
            configurationId = ConfigId.from(aggregateId),
            key = this.key,
            environment = this.environment
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConfigItemRemovedEvent) return false
        if (!super.equals(other)) return false
        return configurationId == other.configurationId &&
               key == other.key &&
               environment == other.environment
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + configurationId.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + environment.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "ConfigItemRemovedEvent(configurationId=$configurationId, key=$key, environment=$environment, ${super.toString()})"
    }
}