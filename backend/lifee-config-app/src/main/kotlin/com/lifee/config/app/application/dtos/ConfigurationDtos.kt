package com.lifee.config.app.application.dtos

import com.lifee.config.domain.aggregates.Configuration
import com.lifee.config.domain.entities.ConfigItem
import com.lifee.config.domain.services.ConfigurationSummary
import com.lifee.config.domain.valueobjects.*
import java.time.LocalDateTime

/**
 * 配置DTO
 */
data class ConfigurationDto(
    val id: String,
    val namespace: String,
    val environment: String,
    val itemCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(configuration: Configuration): ConfigurationDto {
            return ConfigurationDto(
                id = configuration.id.toString(),
                namespace = configuration.namespace,
                environment = configuration.environment.toString(),
                itemCount = configuration.getItemCount(),
                createdAt = configuration.createdAt,
                updatedAt = configuration.getUpdatedAt()
            )
        }
    }
}

/**
 * 配置项DTO
 */
data class ConfigItemDto(
    val key: String,
    val value: String,
    val displayValue: String,
    val type: ConfigType,
    val environment: String,
    val description: String,
    val isEncrypted: Boolean,
    val isSensitive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(item: ConfigItem): ConfigItemDto {
            return ConfigItemDto(
                key = item.key.toString(),
                value = item.getValue().toString(),
                displayValue = item.getDisplayValue(),
                type = item.type,
                environment = item.environment.toString(),
                description = item.getDescription(),
                isEncrypted = item.isEncrypted(),
                isSensitive = item.isSensitive(),
                createdAt = item.createdAt,
                updatedAt = item.getUpdatedAt()
            )
        }
    }
}

/**
 * 配置详情DTO
 */
data class ConfigurationDetailDto(
    val id: String,
    val namespace: String,
    val environment: String,
    val items: List<ConfigItemDto>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(configuration: Configuration): ConfigurationDetailDto {
            return ConfigurationDetailDto(
                id = configuration.id.toString(),
                namespace = configuration.namespace,
                environment = configuration.environment.toString(),
                items = configuration.getItems().values.map { ConfigItemDto.from(it) },
                createdAt = configuration.createdAt,
                updatedAt = configuration.getUpdatedAt()
            )
        }
    }
}

/**
 * 配置统计DTO
 */
data class ConfigurationStatsDto(
    val totalItems: Int,
    val itemsByType: Map<ConfigType, Int>,
    val sensitiveItems: Int,
    val encryptedItems: Int,
    val lastUpdated: LocalDateTime
) {
    companion object {
        fun from(summary: ConfigurationSummary): ConfigurationStatsDto {
            return ConfigurationStatsDto(
                totalItems = summary.totalItems,
                itemsByType = summary.itemsByType,
                sensitiveItems = summary.sensitiveItems,
                encryptedItems = summary.encryptedItems,
                lastUpdated = summary.lastUpdated
            )
        }
    }
}

/**
 * 命名空间统计DTO
 */
data class NamespaceStatsDto(
    val namespace: String,
    val environments: List<String>,
    val totalConfigurations: Int,
    val totalItems: Int,
    val lastUpdated: LocalDateTime?
)

/**
 * 环境统计DTO
 */
data class EnvironmentStatsDto(
    val environment: String,
    val namespaces: List<String>,
    val totalConfigurations: Int,
    val totalItems: Int,
    val lastUpdated: LocalDateTime?
)

/**
 * 配置分页DTO
 */
data class ConfigurationPageDto(
    val content: List<ConfigurationDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

/**
 * 配置项分页DTO
 */
data class ConfigItemPageDto(
    val content: List<ConfigItemDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)

/**
 * 配置比较DTO
 */
data class ConfigurationComparisonDto(
    val sourceConfiguration: ConfigurationDto,
    val targetConfiguration: ConfigurationDto,
    val added: List<ConfigItemDto>,
    val modified: List<ConfigItemComparisonDto>,
    val removed: List<ConfigItemDto>,
    val hasChanges: Boolean
)

/**
 * 配置项比较DTO
 */
data class ConfigItemComparisonDto(
    val key: String,
    val oldValue: String,
    val newValue: String,
    val oldDisplayValue: String,
    val newDisplayValue: String,
    val type: ConfigType
)

/**
 * 配置验证结果DTO
 */
data class ConfigurationValidationDto(
    val configurationId: String,
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>
)

/**
 * 配置导出DTO
 */
data class ConfigurationExportDto(
    val namespace: String,
    val environment: String,
    val format: String,
    val data: String,
    val exportedAt: LocalDateTime
)

/**
 * 配置值DTO
 */
data class ConfigValueDto(
    val key: String,
    val value: String,
    val type: ConfigType,
    val isSensitive: Boolean
) {
    companion object {
        fun from(item: ConfigItem): ConfigValueDto {
            return ConfigValueDto(
                key = item.key.toString(),
                value = if (item.isSensitive()) "***" else item.getValue().toString(),
                type = item.type,
                isSensitive = item.isSensitive()
            )
        }
    }
}