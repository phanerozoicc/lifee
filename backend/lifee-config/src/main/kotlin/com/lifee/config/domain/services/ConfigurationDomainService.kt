package com.lifee.config.domain.services

import com.lifee.config.domain.aggregates.Configuration
import com.lifee.config.domain.entities.ConfigItem
import com.lifee.config.domain.exceptions.*
import com.lifee.config.domain.valueobjects.*
import java.time.Duration
import java.time.LocalDateTime

/**
 * 配置领域服务
 */
class ConfigurationDomainService {
    
    /**
     * 验证配置项是否可以添加到配置中
     */
    fun canAddItem(configuration: Configuration, item: ConfigItem): Boolean {
        // 检查环境匹配
        if (item.environment != configuration.environment) {
            return false
        }
        
        // 检查命名空间匹配
        if (!item.key.belongsToNamespace(configuration.namespace)) {
            return false
        }
        
        // 检查是否已存在
        if (configuration.hasItem(item.key)) {
            return false
        }
        
        return true
    }
    
    /**
     * 验证配置值类型
     */
    fun validateValueType(value: ConfigValue, type: ConfigType): Boolean {
        return type.validateValue(value)
    }
    
    /**
     * 合并两个配置
     * 目标配置的值会被源配置覆盖
     */
    fun mergeConfigurations(
        source: Configuration,
        target: Configuration
    ): Configuration {
        require(source.namespace == target.namespace) {
            "无法合并不同命名空间的配置: ${source.namespace} vs ${target.namespace}"
        }
        
        require(source.environment == target.environment) {
            "无法合并不同环境的配置: ${source.environment} vs ${target.environment}"
        }
        
        val mergedConfiguration = Configuration(
            id = target.id,
            namespace = target.namespace,
            environment = target.environment
        )
        
        // 先添加目标配置的所有项
        target.getItems().values.forEach { item ->
            mergedConfiguration.addItem(item)
        }
        
        // 然后添加或更新源配置的项
        source.getItems().forEach { (key, item) ->
            if (mergedConfiguration.hasItem(key)) {
                mergedConfiguration.updateItem(key, item.getValue())
            } else {
                mergedConfiguration.addItem(item)
            }
        }
        
        return mergedConfiguration
    }
    
    /**
     * 比较两个配置的差异
     */
    fun compareConfigurations(
        source: Configuration,
        target: Configuration
    ): ConfigurationDiff {
        val added = mutableListOf<ConfigItem>()
        val modified = mutableListOf<Pair<ConfigItem, ConfigItem>>()
        val removed = mutableListOf<ConfigItem>()
        
        val sourceItems = source.getItems()
        val targetItems = target.getItems()
        
        // 查找新增和修改的项
        sourceItems.forEach { (key, sourceItem) ->
            val targetItem = targetItems[key]
            if (targetItem == null) {
                added.add(sourceItem)
            } else if (sourceItem.getValue() != targetItem.getValue()) {
                modified.add(Pair(targetItem, sourceItem))
            }
        }
        
        // 查找删除的项
        targetItems.forEach { (key, targetItem) ->
            if (!sourceItems.containsKey(key)) {
                removed.add(targetItem)
            }
        }
        
        return ConfigurationDiff(
            added = added,
            modified = modified,
            removed = removed
        )
    }
    
    /**
     * 验证配置完整性
     */
    fun validateConfiguration(configuration: Configuration): List<String> {
        val errors = mutableListOf<String>()
        
        configuration.getItems().values.forEach { item ->
            try {
                // 验证值类型
                if (!item.type.validateValue(item.getValue())) {
                    errors.add("配置项 ${item.key} 的值与类型 ${item.type} 不匹配")
                }
                
                // 验证命名空间
                if (!item.key.belongsToNamespace(configuration.namespace)) {
                    errors.add("配置项 ${item.key} 不属于命名空间 ${configuration.namespace}")
                }
                
                // 验证环境
                if (item.environment != configuration.environment) {
                    errors.add("配置项 ${item.key} 的环境与配置环境不匹配")
                }
            } catch (e: Exception) {
                errors.add("配置项 ${item.key} 验证失败: ${e.message}")
            }
        }
        
        return errors
    }
    
    /**
     * 检查配置是否过期
     */
    fun isConfigurationStale(
        configuration: Configuration,
        staleDuration: Duration = Duration.ofHours(24)
    ): Boolean {
        return configuration.getUpdatedAt().isBefore(LocalDateTime.now().minus(staleDuration))
    }
    
    /**
     * 获取过期的配置项
     */
    fun getStaleItems(
        configuration: Configuration,
        staleDuration: Duration = Duration.ofHours(24)
    ): List<ConfigItem> {
        return configuration.getItems().values.filter { item ->
            item.isStale(staleDuration)
        }
    }
    
    /**
     * 生成配置摘要
     */
    fun generateConfigurationSummary(configuration: Configuration): ConfigurationSummary {
        val items = configuration.getItems().values
        
        return ConfigurationSummary(
            totalItems = items.size,
            itemsByType = items.groupBy { it.type }.mapValues { it.value.size },
            sensitiveItems = items.count { it.isSensitive() },
            encryptedItems = items.count { it.isEncrypted() },
            lastUpdated = configuration.getUpdatedAt()
        )
    }
    
    /**
     * 创建配置模板
     */
    fun createConfigurationTemplate(
        namespace: String,
        environment: Environment,
        templateItems: List<Pair<String, ConfigType>>
    ): Configuration {
        val configuration = Configuration.create(namespace, environment)
        
        templateItems.forEach { (keyStr, type) ->
            val key = ConfigKey.of("$namespace.$keyStr")
            val value = type.getDefaultValue()
            val item = ConfigItem(
                key = key,
                value = value,
                type = type,
                environment = environment,
                description = "默认配置项"
            )
            configuration.addItem(item)
        }
        
        return configuration
    }
}

/**
 * 配置差异
 */
data class ConfigurationDiff(
    val added: List<ConfigItem>,
    val modified: List<Pair<ConfigItem, ConfigItem>>, // (old, new)
    val removed: List<ConfigItem>
) {
    fun hasChanges(): Boolean {
        return added.isNotEmpty() || modified.isNotEmpty() || removed.isNotEmpty()
    }
}

/**
 * 配置摘要
 */
data class ConfigurationSummary(
    val totalItems: Int,
    val itemsByType: Map<ConfigType, Int>,
    val sensitiveItems: Int,
    val encryptedItems: Int,
    val lastUpdated: LocalDateTime
)