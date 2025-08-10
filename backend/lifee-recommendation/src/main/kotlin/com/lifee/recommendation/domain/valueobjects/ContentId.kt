package com.lifee.recommendation.domain.valueobjects

import com.lifee.common.domain.ValueObject
import java.util.*

/**
 * 内容ID值对象
 */
data class ContentId(
    val value: UUID
) : ValueObject {
    
    companion object {
        fun generate(): ContentId = ContentId(UUID.randomUUID())
        
        fun from(value: String): ContentId = ContentId(UUID.fromString(value))
    }
    
    override fun toString(): String = value.toString()
}