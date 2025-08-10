package com.lifee.config.domain.aggregates

import com.lifee.common.domain.AggregateRoot
import com.lifee.config.domain.entities.ConfigItem
import com.lifee.config.domain.events.*
import com.lifee.config.domain.exceptions.*
import com.lifee.config.domain.valueobjects.*
import java.time.LocalDateTime

/**
 * 配置聚合根
 */
data class Configuration(
    val id: ConfigId,
    val namespace: String,
    val environment: Environment,
    private val items: MutableMap<ConfigKey, ConfigItem> = mutableMapOf(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    private var updatedAt: LocalDateTime = LocalDateTime.now()
) : AggregateRoot() {
    
    init {
        require(namespace.isNotBlank()) { "命名空间不能为空" }
        require(namespace.length <= 100) { "命名空间长度不能超过100个字符" }
    }
    
    companion object {
        /**
         * 创建新的配置
         */
        fun create(
            namespace: String,
            environment: Environment
        ): Configuration {
            val configuration = Configuration(
                id = ConfigId.generate(),
                namespace = namespace,
                environment = environment
            )
            
            configuration.addDomainEvent(
                ConfigurationCreatedEvent(
                    configurationId = configuration.id,
                    namespace = namespace,
                    environment = environment
                )
            )
            
            return configuration
        }
    }
    
    /**
     * 获取更新时间
     */
    fun getUpdatedAt(): LocalDateTime {
        return updatedAt
    }
    
    /**
     * 获取所有配置项
     */
    fun getItems(): Map<ConfigKey, ConfigItem> {
        return items.toMap()
    }
    
    /**
     * 获取配置项数量
     */
    fun getItemCount(): Int {
        return items.size
    }
    
    /**
     * 添加配置项
     */
    fun addItem(item: ConfigItem) {
        // 验证配置项是否属于当前环境
        require(item.environment == environment) {
            "配置项环境 ${item.environment} 与当前配置环境 $environment 不匹配"
        }
        
        // 验证配置键是否属于当前命名空间
        require(item.key.belongsToNamespace(namespace)) {
            "配置键 ${item.key} 不属于命名空间 $namespace"
        }
        
        // 检查配置项是否已存在
        if (items.containsKey(item.key)) {
            throw ConfigItemAlreadyExistsException(item.key, environment)
        }
        
        items[item.key] = item
        updatedAt = LocalDateTime.now()
        
        addDomainEvent(
            ConfigItemAddedEvent(
                configurationId = id,
                key = item.key,
                value = item.getValue(),
                type = item.type,
                environment = environment
            )
        )
    }
    
    /**
     * 更新配置项
     */
    fun updateItem(key: ConfigKey, newValue: ConfigValue) {
        val item = items[key] ?: throw ConfigItemNotFoundException(key, environment)
        
        val oldValue = item.getValue()
        item.updateValue(newValue)
        updatedAt = LocalDateTime.now()
        
        addDomainEvent(
            ConfigItemUpdatedEvent(
                configurationId = id,
                key = key,
                oldValue = oldValue,
                newValue = newValue,
                environment = environment
            )
        )
    }
    
    /**
     * 移除配置项
     */
    fun removeItem(key: ConfigKey) {
        val item = items.remove(key) ?: throw ConfigItemNotFoundException(key, environment)
        updatedAt = LocalDateTime.now()
        
        addDomainEvent(
            ConfigItemRemovedEvent(
                configurationId = id,
                key = key,
                environment = environment
            )
        )
    }
    
    /**
     * 获取配置项
     */
    fun getItem(key: ConfigKey): ConfigItem? {
        return items[key]
    }
    
    /**
     * 检查配置项是否存在
     */
    fun hasItem(key: ConfigKey): Boolean {
        return items.containsKey(key)
    }
    
    /**
     * 获取配置值
     */
    fun getValue(key: ConfigKey): ConfigValue? {
        return items[key]?.getValue()
    }
    
    /**
     * 获取指定类型的配置项
     */
    fun getItemsByType(type: ConfigType): List<ConfigItem> {
        return items.values.filter { it.type == type }
    }
    
    /**
     * 获取敏感配置项
     */
    fun getSensitiveItems(): List<ConfigItem> {
        return items.values.filter { it.isSensitive() }
    }
    
    /**
     * 批量更新配置项
     */
    fun batchUpdate(updates: Map<ConfigKey, ConfigValue>) {
        val validUpdates = mutableMapOf<ConfigKey, Pair<ConfigValue, ConfigValue>>()
        
        // 验证所有更新
        updates.forEach { (key, newValue) ->
            val item = items[key] ?: throw ConfigItemNotFoundException(key, environment)
            require(item.type.validateValue(newValue)) { "配置值与类型不匹配: $key" }
            validUpdates[key] = Pair(item.getValue(), newValue)
        }
        
        // 执行更新
        validUpdates.forEach { (key, values) ->
            val (oldValue, newValue) = values
            items[key]?.updateValue(newValue)
            
            addDomainEvent(
                ConfigItemUpdatedEvent(
                    configurationId = id,
                    key = key,
                    oldValue = oldValue,
                    newValue = newValue,
                    environment = environment
                )
            )
        }
        
        if (validUpdates.isNotEmpty()) {
            updatedAt = LocalDateTime.now()
        }
    }
    
    /**
     * 清空所有配置项
     */
    fun clear() {
        if (items.isNotEmpty()) {
            items.clear()
            updatedAt = LocalDateTime.now()
            
            addDomainEvent(
                ConfigurationClearedEvent(
                    configurationId = id,
                    namespace = namespace,
                    environment = environment
                )
            )
        }
    }
    
    /**
     * 复制配置到另一个环境
     */
    fun copyToEnvironment(targetEnvironment: Environment): Configuration {
        val newConfiguration = Configuration(
            id = ConfigId.generate(),
            namespace = namespace,
            environment = targetEnvironment
        )
        
        items.values.forEach { item ->
            val newItem = item.copy(
                newEnvironment = targetEnvironment
            )
            newConfiguration.items[item.key] = newItem
        }
        
        newConfiguration.addDomainEvent(
            ConfigurationCopiedEvent(
                sourceConfigurationId = id,
                targetConfigurationId = newConfiguration.id,
                sourceEnvironment = environment,
                targetEnvironment = targetEnvironment,
                namespace = namespace
            )
        )
        
        return newConfiguration
    }
    
    override fun toString(): String {
        return "Configuration(id=$id, namespace='$namespace', environment=$environment, itemCount=${items.size})"
    }
}