package com.lifee.config.domain.valueobjects

import com.lifee.common.domain.ValueObject

/**
 * 配置值对象
 */
data class ConfigValue(
    val value: String
) : ValueObject {
    
    init {
        require(value.length <= 5000) { "配置值长度不能超过5000个字符" }
    }
    
    companion object {
        /**
         * 创建空配置值
         */
        fun empty(): ConfigValue {
            return ConfigValue("")
        }
        
        /**
         * 创建配置值
         */
        fun of(value: String): ConfigValue {
            return ConfigValue(value)
        }
        
        /**
         * 从布尔值创建配置值
         */
        fun of(value: Boolean): ConfigValue {
            return ConfigValue(value.toString())
        }
        
        /**
         * 从数字创建配置值
         */
        fun of(value: Number): ConfigValue {
            return ConfigValue(value.toString())
        }
    }
    
    /**
     * 检查是否为空
     */
    fun isEmpty(): Boolean {
        return value.isEmpty()
    }
    
    /**
     * 检查是否不为空
     */
    fun isNotEmpty(): Boolean {
        return value.isNotEmpty()
    }
    
    /**
     * 转换为布尔值
     */
    fun toBoolean(): Boolean {
        return when (value.lowercase()) {
            "true", "1", "yes", "on", "enabled" -> true
            "false", "0", "no", "off", "disabled" -> false
            else -> throw IllegalArgumentException("无法将配置值 '$value' 转换为布尔值")
        }
    }
    
    /**
     * 转换为整数
     */
    fun toInt(): Int {
        return try {
            value.toInt()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("无法将配置值 '$value' 转换为整数", e)
        }
    }
    
    /**
     * 转换为长整数
     */
    fun toLong(): Long {
        return try {
            value.toLong()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("无法将配置值 '$value' 转换为长整数", e)
        }
    }
    
    /**
     * 转换为双精度浮点数
     */
    fun toDouble(): Double {
        return try {
            value.toDouble()
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException("无法将配置值 '$value' 转换为双精度浮点数", e)
        }
    }
    
    /**
     * 转换为字符串列表（以逗号分隔）
     */
    fun toList(): List<String> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        }
    }
    
    /**
     * 检查是否为数字
     */
    fun isNumeric(): Boolean {
        return try {
            value.toDouble()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * 检查是否为布尔值
     */
    fun isBoolean(): Boolean {
        return value.lowercase() in setOf("true", "false", "1", "0", "yes", "no", "on", "off", "enabled", "disabled")
    }
    
    override fun toString(): String {
        return value
    }
}