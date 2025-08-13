package com.lifee.recommendation.domain.aggregates

import com.lifee.common.domain.AggregateRoot
import com.lifee.recommendation.domain.entities.RecommendationItem
import com.lifee.recommendation.domain.events.RecommendationCreatedEvent
import com.lifee.recommendation.domain.valueobjects.RecommendationId
import com.lifee.recommendation.domain.valueobjects.ContentId
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 推荐聚合根
 */
class Recommendation private constructor(
    id: RecommendationId,
    private val userId: UserId,
    private val items: MutableList<RecommendationItem> = mutableListOf(),
    private val createdAt: Instant = Instant.now(),
    private var updatedAt: Instant = Instant.now()
) : AggregateRoot<RecommendationId>(id) {
    
    companion object {
        /**
         * 创建新的推荐
         */
        fun create(
            recommendationId: RecommendationId,
            userId: UserId
        ): Recommendation {
            val recommendation = Recommendation(
                id = recommendationId,
                userId = userId
            )
            
            recommendation.addDomainEvent(
                RecommendationCreatedEvent.create(
                    recommendationId = recommendationId,
                    userId = userId
                )
            )
            
            return recommendation
        }
    }
    
    /**
     * 获取用户ID
     */
    fun getUserId(): UserId = userId
    
    /**
     * 获取推荐项列表
     */
    fun getItems(): List<RecommendationItem> = items.toList()
    
    /**
     * 获取创建时间
     */
    fun getCreatedAt(): Instant = createdAt
    
    /**
     * 获取更新时间
     */
    fun getUpdatedAt(): Instant = updatedAt
    
    /**
     * 添加推荐项
     */
    fun addItem(item: RecommendationItem) {
        // 检查是否已存在相同内容的推荐
        if (items.any { it.getContentId() == item.getContentId() }) {
            throw IllegalArgumentException("推荐项已存在: ${item.getContentId()}")
        }
        
        items.add(item)
        updatedAt = Instant.now()
        
        addDomainEvent(
            RecommendationItemAddedEvent.create(
                recommendationId = getId(),
                userId = userId,
                contentId = item.getContentId(),
                score = item.getScore(),
                type = item.getType()
            )
        )
    }
    
    /**
     * 移除推荐项
     */
    fun removeItem(contentId: ContentId) {
        val removed = items.removeIf { it.getContentId() == contentId }
        
        if (removed) {
            updatedAt = Instant.now()
            
            addDomainEvent(
                RecommendationItemRemovedEvent.create(
                    recommendationId = getId(),
                    userId = userId,
                    contentId = contentId
                )
            )
        }
    }
    
    /**
     * 更新推荐项分数
     */
    fun updateItemScore(contentId: ContentId, newScore: RecommendationScore) {
        val item = items.find { it.getContentId() == contentId }
            ?: throw IllegalArgumentException("推荐项不存在: $contentId")
        
        val oldScore = item.getScore()
        item.updateScore(newScore)
        updatedAt = Instant.now()
        
        addDomainEvent(
            RecommendationScoreUpdatedEvent.create(
                recommendationId = getId(),
                userId = userId,
                contentId = contentId,
                oldScore = oldScore,
                newScore = newScore
            )
        )
    }
    
    /**
     * 获取高质量推荐项
     */
    fun getHighQualityItems(): List<RecommendationItem> {
        return items.filter { it.isHighQuality() }
            .sortedByDescending { it.getScore().value }
    }
    
    /**
     * 按类型获取推荐项
     */
    fun getItemsByType(type: RecommendationType): List<RecommendationItem> {
        return items.filter { it.getType() == type }
            .sortedByDescending { it.getScore().value }
    }
    
    /**
     * 清空所有推荐项
     */
    fun clearItems() {
        if (items.isNotEmpty()) {
            items.clear()
            updatedAt = Instant.now()
            
            addDomainEvent(
                RecommendationClearedEvent.create(
                    recommendationId = getId(),
                    userId = userId
                )
            )
        }
    }
    
    /**
     * 获取推荐项数量
     */
    fun getItemCount(): Int = items.size
    
    /**
     * 是否为空推荐
     */
    fun isEmpty(): Boolean = items.isEmpty()
}