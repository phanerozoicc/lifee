package com.lifee.config.domain.valueobjects

import com.lifee.common.domain.ValueObject
import java.util.*

/**
 * 配置ID值对象
 */
data class ConfigId(
    val value: UUID
) : ValueObject() {
    
    override protected fun getEqualityComponents(): List<Any?> {
        return listOf(value)
    }
    
    companion object {
        /**
         * 生成新的配置ID
         */
        fun generate(): ConfigId {
            return ConfigId(UUID.randomUUID())
        }
        
        /**
         * 从字符串创建配置ID
         */
        fun from(value: String): ConfigId {
            return ConfigId(UUID.fromString(value))
        }
    }
    
    override fun toString(): String {
        return value.toString()
    }
}