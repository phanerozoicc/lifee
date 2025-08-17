package com.lifee.config.domain.aggregates

import com.lifee.common.domain.EventSourcedAggregateRoot
import com.lifee.config.domain.entities.ConfigItem
import com.lifee.config.domain.events.*
import com.lifee.config.domain.exceptions.*
import com.lifee.config.domain.valueobjects.*
import java.time.LocalDateTime

/**
 * 配置聚合根
 */
class Configuration(
    val id: ConfigId,
    val namespace: String,
    val environment: Environment,
    private val items: MutableMap<ConfigKey, ConfigItem> = mutableMapOf(),
    val createdAt: LocalDateTime = LocalDateTime.now(),
    private var updatedAt: LocalDateTime = LocalDateTime.now()
) : EventSourcedAggregateRoot<ConfigId>(id) {
    
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
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Configuration) return false
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
    
    override fun toString(): String {
        return "Configuration(id=$id, namespace='$namespace', environment=$environment, itemCount=${items.size})"
    }
    
    /**
     * 序列化聚合根状态
     */
    override fun serializeState(): Map<String, Any> {
        return mapOf(
            "id" to id.toString(),
            "namespace" to namespace,
            "environment" to environment.toString(),
            "items" to items.mapKeys { it.key.toString() }.mapValues { entry ->
                val item = entry.value
                mapOf(
                    "key" to item.key.toString(),
                    "value" to item.getValue().toString(),
                    "type" to item.type.name,
                    "environment" to item.environment.toString(),
                    "description" to item.getDescription(),
                    "isEncrypted" to item.isEncrypted(),
                    "createdAt" to item.createdAt.toString(),
                    "updatedAt" to item.getUpdatedAt().toString()
                )
            },
            "createdAt" to createdAt.toString(),
            "updatedAt" to updatedAt.toString()
        )
    }
    
    /**
     * 反序列化聚合根状态
     */
    override fun deserializeState(stateData: Map<String, Any>) {
        // 清空当前状态
        items.clear()
        
        // 恢复配置项
        @Suppress("UNCHECKED_CAST")
        val itemsData = stateData["items"] as? Map<String, Map<String, Any>> ?: emptyMap()
        
        itemsData.forEach { (_, itemData) ->
            try {
                val key = ConfigKey.of(itemData["key"] as String)
                val value = ConfigValue.of(itemData["value"] as String)
                val type = ConfigType.valueOf(itemData["type"] as String)
                val environment = Environment.of(itemData["environment"] as String)
                val description = itemData["description"] as? String ?: ""
                val isEncrypted = itemData["isEncrypted"] as? Boolean ?: false
                val createdAt = java.time.LocalDateTime.parse(itemData["createdAt"] as String)
                val updatedAt = java.time.LocalDateTime.parse(itemData["updatedAt"] as String)
                
                val configItem = ConfigItem(
                    key = key,
                    value = value,
                    type = type,
                    environment = environment,
                    description = description,
                    isEncrypted = isEncrypted,
                    createdAt = createdAt,
                    updatedAt = updatedAt
                )
                
                items[key] = configItem
            } catch (e: Exception) {
                // 记录错误但继续处理其他项
                // 在实际应用中可能需要更严格的错误处理
            }
        }
        
        // 恢复时间戳
        val updatedAtStr = stateData["updatedAt"] as? String
        if (updatedAtStr != null) {
            try {
                updatedAt = java.time.LocalDateTime.parse(updatedAtStr)
            } catch (e: Exception) {
                // 使用当前时间作为默认值
                updatedAt = java.time.LocalDateTime.now()
            }
        }
    }
    
    /**
     * 应用领域事件到聚合根
     */
    override fun applyEvent(event: com.lifee.common.domain.DomainEvent) {
        when (event) {
            is ConfigurationCreatedEvent -> {
                // 配置创建事件已在构造函数中处理
            }
            is ConfigItemAddedEvent -> {
                // 配置项添加事件已在addItem方法中处理
            }
            is ConfigItemUpdatedEvent -> {
                // 配置项更新事件已在updateItem方法中处理
            }
            is ConfigItemRemovedEvent -> {
                // 配置项删除事件已在removeItem方法中处理
            }
            is ConfigurationClearedEvent -> {
                // 配置清空事件已在clear方法中处理
            }
            is ConfigurationCopiedEvent -> {
                // 配置复制事件已在copyToEnvironment方法中处理
            }
            // 可以根据需要添加更多事件处理
        }
    }
}