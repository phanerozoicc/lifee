package com.lifee.config.app.application.queries

import com.lifee.common.cqrs.queries.Query
import com.lifee.config.domain.valueobjects.*
import jakarta.validation.constraints.*

/**
 * 获取配置查询
 */
data class GetConfigurationQuery(
    @field:NotNull(message = "配置ID不能为空")
    val configurationId: ConfigId
) : Query

/**
 * 根据命名空间和环境获取配置查询
 */
data class GetConfigurationByNamespaceQuery(
    @field:NotBlank(message = "命名空间不能为空")
    val namespace: String,
    
    @field:NotNull(message = "环境不能为空")
    val environment: Environment
) : Query

/**
 * 获取配置项查询
 */
data class GetConfigItemQuery(
    @field:NotNull(message = "配置ID不能为空")
    val configurationId: ConfigId,
    
    @field:NotNull(message = "配置键不能为空")
    val key: ConfigKey
) : Query

/**
 * 获取配置值查询
 */
data class GetConfigValueQuery(
    @field:NotBlank(message = "命名空间不能为空")
    val namespace: String,
    
    @field:NotNull(message = "环境不能为空")
    val environment: Environment,
    
    @field:NotNull(message = "配置键不能为空")
    val key: ConfigKey
) : Query

/**
 * 获取命名空间配置列表查询
 */
data class GetNamespaceConfigurationsQuery(
    @field:NotBlank(message = "命名空间不能为空")
    val namespace: String
) : Query

/**
 * 获取环境配置列表查询
 */
data class GetEnvironmentConfigurationsQuery(
    @field:NotNull(message = "环境不能为空")
    val environment: Environment,
    
    @field:Min(value = 0, message = "页码不能小于0")
    val page: Int = 0,
    
    @field:Min(value = 1, message = "页大小不能小于1")
    @field:Max(value = 100, message = "页大小不能超过100")
    val size: Int = 20
) : Query

/**
 * 获取配置项列表查询
 */
data class GetConfigItemsQuery(
    @field:NotNull(message = "配置ID不能为空")
    val configurationId: ConfigId,
    
    val type: ConfigType? = null,
    
    val includeSensitive: Boolean = false
) : Query

/**
 * 搜索配置项查询
 */
data class SearchConfigItemsQuery(
    @field:NotBlank(message = "命名空间不能为空")
    val namespace: String,
    
    @field:NotNull(message = "环境不能为空")
    val environment: Environment,
    
    @field:NotBlank(message = "搜索关键词不能为空")
    val keyword: String,
    
    val type: ConfigType? = null,
    
    @field:Min(value = 0, message = "页码不能小于0")
    val page: Int = 0,
    
    @field:Min(value = 1, message = "页大小不能小于1")
    @field:Max(value = 50, message = "页大小不能超过50")
    val size: Int = 20
) : Query

/**
 * 获取配置统计查询
 */
data class GetConfigurationStatsQuery(
    @field:NotNull(message = "配置ID不能为空")
    val configurationId: ConfigId
) : Query

/**
 * 获取命名空间统计查询
 */
data class GetNamespaceStatsQuery(
    @field:NotBlank(message = "命名空间不能为空")
    val namespace: String
) : Query

/**
 * 获取环境统计查询
 */
data class GetEnvironmentStatsQuery(
    @field:NotNull(message = "环境不能为空")
    val environment: Environment
) : Query

/**
 * 获取所有命名空间查询
 */
data class GetAllNamespacesQuery(
    val environment: Environment? = null
) : Query

/**
 * 比较配置查询
 */
data class CompareConfigurationsQuery(
    @field:NotNull(message = "源配置ID不能为空")
    val sourceConfigurationId: ConfigId,
    
    @field:NotNull(message = "目标配置ID不能为空")
    val targetConfigurationId: ConfigId
) : Query

/**
 * 验证配置查询
 */
data class ValidateConfigurationQuery(
    @field:NotNull(message = "配置ID不能为空")
    val configurationId: ConfigId
) : Query

/**
 * 导出配置查询
 */
data class ExportConfigurationQuery(
    @field:NotNull(message = "配置ID不能为空")
    val configurationId: ConfigId,
    
    @field:NotBlank(message = "导出格式不能为空")
    val format: String = "json", // json, yaml, properties
    
    val includeSensitive: Boolean = false
) : Query