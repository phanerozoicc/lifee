package com.lifee.recommendation.domain.entities

import com.lifee.common.domain.Entity
import com.lifee.recommendation.domain.valueobjects.*
import java.time.Instant

/**
 * 推荐项实体
 */
class RecommendationItem(
    private val contentId: ContentId,
    private var score: RecommendationScore,
    private val type: RecommendationType,
    private val reason: String,
    private val createdAt: Instant = Instant.now()
) : Entity {
    
    /**
     * 获取内容ID
     */
    fun getContentId(): ContentId = contentId
    
    /**
     * 获取推荐分数
     */
    fun getScore(): RecommendationScore = score
    
    /**
     * 获取推荐类型
     */
    fun getType(): RecommendationType = type
    
    /**
     * 获取推荐原因
     */
    fun getReason(): String = reason
    
    /**
     * 获取创建时间
     */
    fun getCreatedAt(): Instant = createdAt
    
    /**
     * 更新推荐分数
     */
    fun updateScore(newScore: RecommendationScore) {
        this.score = newScore
    }
    
    /**
     * 是否为高质量推荐
     */
    fun isHighQuality(): Boolean = score.isHighScore()
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RecommendationItem) return false
        return contentId == other.contentId
    }
    
    override fun hashCode(): Int {
        return contentId.hashCode()
    }
}