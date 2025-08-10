package com.lifee.recommendation.domain.valueobjects

import com.lifee.common.domain.ValueObject

/**
 * 推荐类型枚举
 */
enum class RecommendationType : ValueObject {
    /**
     * 基于内容的推荐
     */
    CONTENT_BASED,
    
    /**
     * 协同过滤推荐
     */
    COLLABORATIVE_FILTERING,
    
    /**
     * 混合推荐
     */
    HYBRID,
    
    /**
     * 热门推荐
     */
    TRENDING,
    
    /**
     * 个性化推荐
     */
    PERSONALIZED
}