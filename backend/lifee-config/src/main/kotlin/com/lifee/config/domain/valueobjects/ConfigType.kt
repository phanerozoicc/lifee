package com.lifee.config.domain.valueobjects

import com.lifee.common.domain.ValueObject

/**
 * 配置类型枚举
 */
enum class ConfigType : ValueObject {
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
        return this == PASSWORD
    }
    
    /**
     * 检查是否为数字类型
     */
    fun isNumeric(): Boolean {
        return this in setOf(INTEGER, LONG, DOUBLE)
    }
    
    /**
     * 验证值是否符合类型
     */
    fun validateValue(value: ConfigValue): Boolean {
        return try {
            when (this) {
                STRING -> true
                INTEGER -> {
                    value.toInt()
                    true
                }
                LONG -> {
                    value.toLong()
                    true
                }
                DOUBLE -> {
                    value.toDouble()
                    true
                }
                BOOLEAN -> {
                    value.toBoolean()
                    true
                }
                JSON -> {
                    // 简单的JSON格式检查
                    val trimmed = value.value.trim()
                    (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
                    (trimmed.startsWith("[") && trimmed.endsWith("]"))
                }
                LIST -> true // 列表类型总是有效的
                PASSWORD -> true // 密码类型总是有效的
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
            STRING -> ConfigValue.of("")
            INTEGER -> ConfigValue.of(0)
            LONG -> ConfigValue.of(0L)
            DOUBLE -> ConfigValue.of(0.0)
            BOOLEAN -> ConfigValue.of(false)
            JSON -> ConfigValue.of("{}")
            LIST -> ConfigValue.of("")
            PASSWORD -> ConfigValue.of("")
        }
    }
}