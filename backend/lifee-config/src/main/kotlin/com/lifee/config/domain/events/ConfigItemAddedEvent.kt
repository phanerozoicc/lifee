package com.lifee.config.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.config.domain.valueobjects.ConfigId
import com.lifee.config.domain.valueobjects.ConfigKey
import com.lifee.config.domain.valueobjects.ConfigValue
import com.lifee.config.domain.valueobjects.ConfigType
import com.lifee.config.domain.valueobjects.Environment
import java.time.Instant
import java.util.*

/**
 * 配置项添加事件
 */
class ConfigItemAddedEvent(
    val configurationId: ConfigId,
    val key: ConfigKey,
    val value: ConfigValue,
    val type: ConfigType,
    val environment: Environment
) : DomainEvent(configurationId.toString()) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return ConfigItemAddedEvent(
            configurationId = ConfigId.from(aggregateId),
            key = this.key,
            value = this.value,
            type = this.type,
            environment = this.environment
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConfigItemAddedEvent) return false
        if (!super.equals(other)) return false
        return configurationId == other.configurationId &&
               key == other.key &&
               value == other.value &&
               type == other.type &&
               environment == other.environment
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + configurationId.hashCode()
        result = 31 * result + key.hashCode()
        result = 31 * result + value.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + environment.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "ConfigItemAddedEvent(configurationId=$configurationId, key=$key, value=$value, type=$type, environment=$environment, ${super.toString()})"
    }
}