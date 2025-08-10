package com.lifee.config.domain.valueobjects

import com.lifee.common.domain.ValueObject

/**
 * 配置键值对象
 */
data class ConfigKey(
    val value: String
) : ValueObject {
    
    init {
        require(value.isNotBlank()) { "配置键不能为空" }
        require(value.length <= 200) { "配置键长度不能超过200个字符" }
        require(isValidKey(value)) { "配置键格式不正确，只能包含字母、数字、点号、下划线和连字符" }
    }
    
    companion object {
        private val KEY_PATTERN = Regex("^[a-zA-Z0-9._-]+$")
        
        /**
         * 验证配置键格式
         */
        private fun isValidKey(key: String): Boolean {
            return KEY_PATTERN.matches(key)
        }
        
        /**
         * 创建配置键
         */
        fun of(value: String): ConfigKey {
            return ConfigKey(value)
        }
    }
    
    /**
     * 获取配置键的命名空间
     */
    fun getNamespace(): String? {
        val parts = value.split(".")
        return if (parts.size > 1) parts.first() else null
    }
    
    /**
     * 获取配置键的名称部分
     */
    fun getName(): String {
        val parts = value.split(".")
        return parts.last()
    }
    
    /**
     * 检查是否属于指定命名空间
     */
    fun belongsToNamespace(namespace: String): Boolean {
        return value.startsWith("$namespace.")
    }
    
    override fun toString(): String {
        return value
    }
}