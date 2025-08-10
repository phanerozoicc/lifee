package com.lifee.config.domain.entities

import com.lifee.common.domain.Entity
import com.lifee.config.domain.valueobjects.*
import java.time.LocalDateTime

/**
 * 配置项实体
 */
data class ConfigItem(
    val key: ConfigKey,
    private var value: ConfigValue,
    val type: ConfigType,
    val environment: Environment,
    private var description: String = "",
    private var isEncrypted: Boolean = false,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    private var updatedAt: LocalDateTime = LocalDateTime.now()
) : Entity {
    
    init {
        require(description.length <= 500) { "配置描述长度不能超过500个字符" }
        validateValueType()
    }
    
    /**
     * 获取配置值
     */
    fun getValue(): ConfigValue {
        return value
    }
    
    /**
     * 获取描述
     */
    fun getDescription(): String {
        return description
    }
    
    /**
     * 检查是否加密
     */
    fun isEncrypted(): Boolean {
        return isEncrypted
    }
    
    /**
     * 获取更新时间
     */
    fun getUpdatedAt(): LocalDateTime {
        return updatedAt
    }
    
    /**
     * 更新配置值
     */
    fun updateValue(newValue: ConfigValue) {
        require(type.validateValue(newValue)) { "配置值与类型不匹配" }
        
        if (this.value != newValue) {
            this.value = newValue
            this.updatedAt = LocalDateTime.now()
        }
    }
    
    /**
     * 更新描述
     */
    fun updateDescription(newDescription: String) {
        require(newDescription.length <= 500) { "配置描述长度不能超过500个字符" }
        
        if (this.description != newDescription) {
            this.description = newDescription
            this.updatedAt = LocalDateTime.now()
        }
    }
    
    /**
     * 设置加密状态
     */
    fun setEncrypted(encrypted: Boolean) {
        if (this.isEncrypted != encrypted) {
            this.isEncrypted = encrypted
            this.updatedAt = LocalDateTime.now()
        }
    }
    
    /**
     * 检查是否为敏感配置
     */
    fun isSensitive(): Boolean {
        return type.isSensitive() || isEncrypted
    }
    
    /**
     * 获取显示值（敏感信息会被掩码）
     */
    fun getDisplayValue(): String {
        return if (isSensitive()) {
            "***"
        } else {
            value.value
        }
    }
    
    /**
     * 检查配置是否已过期（基于更新时间）
     */
    fun isStale(staleDuration: java.time.Duration): Boolean {
        return updatedAt.isBefore(LocalDateTime.now().minus(staleDuration))
    }
    
    /**
     * 验证值类型匹配
     */
    private fun validateValueType() {
        require(type.validateValue(value)) { "配置值 '${value.value}' 与类型 $type 不匹配" }
    }
    
    /**
     * 创建配置项的副本
     */
    fun copy(
        newKey: ConfigKey = this.key,
        newValue: ConfigValue = this.value,
        newType: ConfigType = this.type,
        newEnvironment: Environment = this.environment,
        newDescription: String = this.description
    ): ConfigItem {
        return ConfigItem(
            key = newKey,
            value = newValue,
            type = newType,
            environment = newEnvironment,
            description = newDescription,
            isEncrypted = this.isEncrypted,
            createdAt = this.createdAt,
            updatedAt = LocalDateTime.now()
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ConfigItem) return false
        return key == other.key && environment == other.environment
    }
    
    override fun hashCode(): Int {
        return key.hashCode() * 31 + environment.hashCode()
    }
    
    override fun toString(): String {
        return "ConfigItem(key=$key, environment=$environment, type=$type, value=${getDisplayValue()})"
    }
}