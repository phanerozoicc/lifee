package com.lifee.recommendation.domain.valueobjects

import com.lifee.common.domain.ValueObject
import java.util.*

/**
 * 推荐ID值对象
 */
data class RecommendationId(
    val value: UUID
) : ValueObject {
    
    companion object {
        fun generate(): RecommendationId = RecommendationId(UUID.randomUUID())
        
        fun from(value: String): RecommendationId = RecommendationId(UUID.fromString(value))
    }
    
    override fun toString(): String = value.toString()
}