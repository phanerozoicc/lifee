package com.lifee.time.domain

import com.lifee.common.domain.ValueObject
import java.util.*

/**
 * 团队ID值对象
 */
data class TeamId(val value: String) : ValueObject {
    
    init {
        require(isValid(value)) { "无效的团队ID格式: $value" }
    }
    
    companion object {
        private val TEAM_ID_PATTERN = Regex("^[a-zA-Z0-9]{8,32}$")
        
        /**
         * 生成新的团队ID
         */
        fun generate(): TeamId {
            return TeamId(UUID.randomUUID().toString().replace("-", "").substring(0, 16))
        }
        
        /**
         * 从字符串创建团队ID
         */
        fun of(value: String): TeamId {
            return TeamId(value)
        }
        
        /**
         * 验证团队ID格式
         */
        fun isValid(value: String): Boolean {
            return value.matches(TEAM_ID_PATTERN)
        }
    }
    
    override fun toString(): String = value
}