package com.lifee.config.app.application.commands

import com.lifee.common.cqrs.commands.Command
import com.lifee.config.domain.valueobjects.*
import jakarta.validation.constraints.*

/**
 * 创建配置命令
 */
data class CreateConfigurationCommand(
    val configurationId: ConfigId,
    
    @field:NotBlank(message = "命名空间不能为空")
    @field:Size(max = 100, message = "命名空间长度不能超过100个字符")
    val namespace: String,
    
    @field:NotNull(message = "环境不能为空")
    val environment: Environment
) : Command

/**
 * 添加配置项命令
 */
data class AddConfigItemCommand(
    @field:NotNull(message = "配置ID不能为空")
    val configurationId: ConfigId,
    
    @field:NotNull(message = "配置键不能为空")
    val key: ConfigKey,
    
    @field:NotNull(message = "配置值不能为空")
    val value: ConfigValue,
    
    @field:NotNull(message = "配置类型不能为空")
    val type: ConfigType,
    
    @field:Size(max = 500, message = "描述长度不能超过500个字符")
    val description: String = "",
    
    val isEncrypted: Boolean = false
) : Command

/**
 * 更新配置项命令
 */
data class UpdateConfigItemCommand(
    @field:NotNull(message = "配置ID不能为空")
    val configurationId: ConfigId,
    
    @field:NotNull(message = "配置键不能为空")
    val key: ConfigKey,
    
    @field:NotNull(message = "配置值不能为空")
    val value: ConfigValue
) : Command

/**
 * 移除配置项命令
 */
data class RemoveConfigItemCommand(
    @field:NotNull(message = "配置ID不能为空")
    val configurationId: ConfigId,
    
    @field:NotNull(message = "配置键不能为空")
    val key: ConfigKey
) : Command

/**
 * 批量更新配置项命令
 */
data class BatchUpdateConfigItemsCommand(
    @field:NotNull(message = "配置ID不能为空")
    val configurationId: ConfigId,
    
    @field:NotEmpty(message = "更新项不能为空")
    val updates: Map<ConfigKey, ConfigValue>
) : Command

/**
 * 清空配置命令
 */
data class ClearConfigurationCommand(
    @field:NotNull(message = "配置ID不能为空")
    val configurationId: ConfigId
) : Command

/**
 * 删除配置命令
 */
data class DeleteConfigurationCommand(
    @field:NotNull(message = "配置ID不能为空")
    val configurationId: ConfigId
) : Command

/**
 * 复制配置到环境命令
 */
data class CopyConfigurationToEnvironmentCommand(
    @field:NotNull(message = "源配置ID不能为空")
    val sourceConfigurationId: ConfigId,
    
    @field:NotNull(message = "目标环境不能为空")
    val targetEnvironment: Environment
) : Command

/**
 * 导入配置命令
 */
data class ImportConfigurationCommand(
    @field:NotBlank(message = "命名空间不能为空")
    val namespace: String,
    
    @field:NotNull(message = "环境不能为空")
    val environment: Environment,
    
    @field:NotEmpty(message = "配置数据不能为空")
    val configData: Map<String, Any>,
    
    val overwrite: Boolean = false
) : Command

/**
 * 发布配置命令
 */
data class PublishConfigurationCommand(
    @field:NotNull(message = "配置ID不能为空")
    val configurationId: ConfigId,
    
    @field:NotBlank(message = "版本号不能为空")
    val version: String,
    
    @field:Size(max = 500, message = "发布说明长度不能超过500个字符")
    val releaseNotes: String = ""
) : Command