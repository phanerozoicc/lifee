package com.lifee.config.domain.valueobjects

import com.lifee.common.domain.ValueObject

/**
 * 配置类型枚举
 */
enum class ConfigType {
    /**
     * 字符串类型
     */
    STRING,
    
    /**
     * 整数类型
     */
    INTEGER,
    
    /**
     * 长整数类型
     */
    LONG,
    
    /**
     * 双精度浮点数类型
     */
    DOUBLE,
    
    /**
     * 布尔类型
     */
    BOOLEAN,
    
    /**
     * JSON类型
     */
    JSON,
    
    /**
     * 列表类型（逗号分隔）
     */
    LIST,
    
    /**
     * 密码类型（敏感信息）
     */
    PASSWORD;
    
    /**
     * 检查是否为敏感类型
     */
    fun isSensitive(): Boolean {
        return this == ConfigType.PASSWORD
    }
    
    /**
     * 检查是否为数字类型
     */
    fun isNumeric(): Boolean {
        return this in setOf(ConfigType.INTEGER, ConfigType.LONG, ConfigType.DOUBLE)
    }
    
    /**
     * 验证值是否符合类型
     */
    fun validateValue(value: ConfigValue): Boolean {
        return try {
            when (this) {
                ConfigType.STRING -> true
                ConfigType.INTEGER -> {
                    value.toInt()
                    true
                }
                ConfigType.LONG -> {
                    value.toLong()
                    true
                }
                ConfigType.DOUBLE -> {
                    value.toDouble()
                    true
                }
                ConfigType.BOOLEAN -> {
                    value.toBoolean()
                    true
                }
                ConfigType.JSON -> {
                    // 简单的JSON格式检查
                    val trimmed = value.value.trim()
                    (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                    (trimmed.startsWith("[") && trimmed.endsWith("]"))
                }
                ConfigType.LIST -> true // 列表类型总是有效的
                ConfigType.PASSWORD -> true // 密码类型总是有效的
            }
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 获取类型的默认值
     */
    fun getDefaultValue(): ConfigValue {
        return when (this) {
            ConfigType.STRING -> ConfigValue.of("")
            ConfigType.INTEGER -> ConfigValue.of(0)
            ConfigType.LONG -> ConfigValue.of(0L)
            ConfigType.DOUBLE -> ConfigValue.of(0.0)
            ConfigType.BOOLEAN -> ConfigValue.of(false)
            ConfigType.JSON -> ConfigValue.of("{}")
            ConfigType.LIST -> ConfigValue.of("")
            ConfigType.PASSWORD -> ConfigValue.of("")
        }
    }
}