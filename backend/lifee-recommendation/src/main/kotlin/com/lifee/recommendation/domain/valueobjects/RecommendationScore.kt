package com.lifee.recommendation.domain.valueobjects

import com.lifee.common.domain.ValueObject

/**
 * 推荐分数值对象
 */
data class RecommendationScore(
    val value: Double
) : ValueObject {
    
    init {
        require(value in 0.0..1.0) { "推荐分数必须在0.0到1.0之间" }
    }
    
    companion object {
        fun zero(): RecommendationScore = RecommendationScore(0.0)
        
        fun max(): RecommendationScore = RecommendationScore(1.0)
        
        fun of(value: Double): RecommendationScore = RecommendationScore(value)
    }
    
    /**
     * 是否为高分推荐
     */
    fun isHighScore(): Boolean = value >= 0.7
    
    /**
     * 是否为中等分数推荐
     */
    fun isMediumScore(): Boolean = value >= 0.4 && value < 0.7
    
    /**
     * 是否为低分推荐
     */
    fun isLowScore(): Boolean = value < 0.4
}