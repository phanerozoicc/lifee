package com.lifee.knowledge.domain.valueobjects

import java.util.*

/**
 * 用户ID值对象（知识库模块中的用户引用）
 */
data class UserId(
    val value: UUID
) {
    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID())
        
        fun fromString(value: String): UserId {
            return try {
                UserId(UUID.fromString(value))
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid UserId format: $value", e)
            }
        }
    }
    
    override fun toString(): String = value.toString()
}