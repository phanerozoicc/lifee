package com.lifee.config.domain.valueobjects

import com.lifee.common.domain.ValueObject

/**
 * 环境值对象
 */
data class Environment(
    val value: String
) : ValueObject {
    
    init {
        require(value.isNotBlank()) { "环境名称不能为空" }
        require(value.length <= 50) { "环境名称长度不能超过50个字符" }
        require(isValidEnvironment(value)) { "环境名称格式不正确，只能包含字母、数字和连字符" }
    }
    
    companion object {
        private val ENVIRONMENT_PATTERN = Regex("^[a-zA-Z0-9-]+$")
        
        // 预定义环境
        val DEVELOPMENT = Environment("development")
        val TESTING = Environment("testing")
        val STAGING = Environment("staging")
        val PRODUCTION = Environment("production")
        
        /**
         * 验证环境名称格式
         */
        private fun isValidEnvironment(env: String): Boolean {
            return ENVIRONMENT_PATTERN.matches(env)
        }
        
        /**
         * 创建环境
         */
        fun of(value: String): Environment {
            return Environment(value.lowercase())
        }
        
        /**
         * 获取所有预定义环境
         */
        fun getPredefinedEnvironments(): List<Environment> {
            return listOf(DEVELOPMENT, TESTING, STAGING, PRODUCTION)
        }
    }
    
    /**
     * 检查是否为开发环境
     */
    fun isDevelopment(): Boolean {
        return this == DEVELOPMENT
    }
    
    /**
     * 检查是否为测试环境
     */
    fun isTesting(): Boolean {
        return this == TESTING
    }
    
    /**
     * 检查是否为预发布环境
     */
    fun isStaging(): Boolean {
        return this == STAGING
    }
    
    /**
     * 检查是否为生产环境
     */
    fun isProduction(): Boolean {
        return this == PRODUCTION
    }
    
    /**
     * 检查是否为预定义环境
     */
    fun isPredefined(): Boolean {
        return this in getPredefinedEnvironments()
    }
    
    override fun toString(): String {
        return value
    }
}