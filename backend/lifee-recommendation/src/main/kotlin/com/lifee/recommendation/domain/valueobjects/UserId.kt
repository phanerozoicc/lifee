package com.lifee.recommendation.domain.valueobjects

import com.lifee.common.domain.ValueObject
import java.util.*

/**
 * 用户ID值对象
 */
data class UserId(
    val value: UUID
) : ValueObject {
    
    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID())
        
        fun from(value: String): UserId = UserId(UUID.fromString(value))
    }
    
    override fun toString(): String = value.toString()
}