package com.lifee.config.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.config.domain.valueobjects.ConfigId
import com.lifee.config.domain.valueobjects.ConfigKey
import com.lifee.config.domain.valueobjects.ConfigValue
import com.lifee.config.domain.valueobjects.Environment
import java.time.Instant
import java.util.*

/**
 * 配置项更新事件
 */
class ConfigItemUpdatedEvent(
    val configurationId: ConfigId,
    val key: ConfigKey,
    val oldValue: ConfigValue,
    val newValue: ConfigValue,
    val environment: Environment
) : DomainEvent(configurationId.toString()) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return ConfigItemUpdatedEvent(
            configurationId = ConfigId.from(aggregateId),
            key = this.key,
            oldValue = this.oldValue,
            newValue = this.newValue,
            environment = this.environment
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConfigItemUpdatedEvent) return false
        if (!super.equals(other)) return false
        return configurationId == other.configurationId &&
               key == other.key &&
               oldValue == other.oldValue &&
               newValue == other.newValue &&
               environment == other.environment
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + configurationId.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + oldValue.hashCode()
        result = 31 * result + newValue.hashCode()
        result = 31 * result + environment.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "ConfigItemUpdatedEvent(configurationId=$configurationId, key=$key, oldValue=$oldValue, newValue=$newValue, environment=$environment, ${super.toString()})"
    }
}