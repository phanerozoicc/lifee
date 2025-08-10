package com.lifee.chat.domain.valueobjects

import java.util.*

/**
 * 用户ID值对象
 */
data class UserId(val value: UUID) {
    
    companion object {
        /**
         * 生成新的用户ID
         */
        fun generate(): UserId {
            return UserId(UUID.randomUUID())
        }
        
        /**
         * 从字符串创建用户ID
         */
        fun fromString(id: String): UserId {
            return UserId(UUID.fromString(id))
        }
    }
    
    override fun toString(): String {
        return value.toString()
    }
}