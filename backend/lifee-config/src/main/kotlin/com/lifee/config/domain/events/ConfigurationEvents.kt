package com.lifee.config.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.config.domain.valueobjects.*

/**
 * 配置创建事件
 */
data class ConfigurationCreatedEvent(
    val configurationId: ConfigId,
    val namespace: String,
    val environment: Environment
) : DomainEvent

/**
 * 配置项添加事件
 */
data class ConfigItemAddedEvent(
    val configurationId: ConfigId,
    val key: ConfigKey,
    val value: ConfigValue,
    val type: ConfigType,
    val environment: Environment
) : DomainEvent

/**
 * 配置项更新事件
 */
data class ConfigItemUpdatedEvent(
    val configurationId: ConfigId,
    val key: ConfigKey,
    val oldValue: ConfigValue,
    val newValue: ConfigValue,
    val environment: Environment
) : DomainEvent

/**
 * 配置项移除事件
 */
data class ConfigItemRemovedEvent(
    val configurationId: ConfigId,
    val key: ConfigKey,
    val environment: Environment
) : DomainEvent

/**
 * 配置清空事件
 */
data class ConfigurationClearedEvent(
    val configurationId: ConfigId,
    val namespace: String,
    val environment: Environment
) : DomainEvent

/**
 * 配置复制事件
 */
data class ConfigurationCopiedEvent(
    val sourceConfigurationId: ConfigId,
    val targetConfigurationId: ConfigId,
    val sourceEnvironment: Environment,
    val targetEnvironment: Environment,
    val namespace: String
) : DomainEvent

/**
 * 配置发布事件
 */
data class ConfigurationPublishedEvent(
    val configurationId: ConfigId,
    val namespace: String,
    val environment: Environment,
    val version: String
) : DomainEvent

/**
 * 配置回滚事件
 */
data class ConfigurationRolledBackEvent(
    val configurationId: ConfigId,
    val namespace: String,
    val environment: Environment,
    val fromVersion: String,
    val toVersion: String
) : DomainEvent