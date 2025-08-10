package com.lifee.user.domain

import com.lifee.common.domain.ValueObject
import java.util.*

/**
 * 用户ID值对象
 */
data class UserId(
    val value: UUID
) : ValueObject() {
    
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
        fun fromString(value: String): UserId {
            return UserId(UUID.fromString(value))
        }
    }
    
    override fun getEqualityComponents(): List<Any> {
        return listOf(value)
    }
    
    override fun toString(): String {
        return value.toString()
    }
}