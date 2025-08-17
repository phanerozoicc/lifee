package com.lifee.recommendation.domain.aggregates

import com.lifee.common.domain.EventSourcedAggregateRoot
import com.lifee.recommendation.domain.entities.RecommendationItem
import com.lifee.recommendation.domain.events.*
import com.lifee.recommendation.domain.valueobjects.*
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
) : EventSourcedAggregateRoot<RecommendationId>(id) {
    
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
    
    /**
     * 序列化聚合根状态
     */
    override fun serializeState(): Map<String, Any> {
        return mapOf(
            "id" to id.toString(),
            "userId" to userId.toString(),
            "createdAt" to createdAt.toString(),
            "updatedAt" to updatedAt.toString(),
            "items" to items.map { item ->
                mapOf(
                    "contentId" to item.getContentId().toString(),
                    "score" to item.getScore().value,
                    "type" to item.getType().toString(),
                    "createdAt" to item.getCreatedAt().toString(),
                    "updatedAt" to item.getUpdatedAt().toString()
                )
            }
        )
    }
    
    /**
     * 反序列化聚合根状态
     */
    override fun deserializeState(stateData: Map<String, Any>) {
        try {
            // 清空当前推荐项
            items.clear()
            
            // 恢复时间戳
            updatedAt = Instant.parse(stateData["updatedAt"] as String)
            
            // 恢复推荐项
            @Suppress("UNCHECKED_CAST")
            val itemsData = stateData["items"] as? List<Map<String, Any>> ?: emptyList()
            
            itemsData.forEach { itemData ->
                try {
                    val contentId = ContentId.of(itemData["contentId"] as String)
                    val score = RecommendationScore(itemData["score"] as Double)
                    val type = RecommendationType.valueOf(itemData["type"] as String)
                    val createdAt = Instant.parse(itemData["createdAt"] as String)
                    val updatedAt = Instant.parse(itemData["updatedAt"] as String)
                    
                    val item = RecommendationItem.create(
                        contentId = contentId,
                        score = score,
                        type = type,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                    
                    items.add(item)
                } catch (e: Exception) {
                    // 记录错误但继续处理其他推荐项
                    // 在实际应用中可能需要更严格的错误处理
                }
            }
            
        } catch (e: Exception) {
            // 在实际应用中需要更严格的错误处理
            throw IllegalStateException("Failed to deserialize Recommendation state", e)
        }
    }
    
    /**
     * 应用领域事件到聚合根
     */
    override fun applyEvent(event: com.lifee.common.domain.DomainEvent) {
        when (event) {
            is RecommendationCreatedEvent -> {
                // 推荐创建事件已在构造函数中处理
            }
            is RecommendationItemAddedEvent -> {
                // 推荐项添加事件已在addItem方法中处理
            }
            is RecommendationItemRemovedEvent -> {
                // 推荐项移除事件已在removeItem方法中处理
            }
            is RecommendationScoreUpdatedEvent -> {
                // 推荐分数更新事件已在updateItemScore方法中处理
            }
            is RecommendationClearedEvent -> {
                // 推荐清空事件已在clearItems方法中处理
            }
            // 可以根据需要添加更多事件处理
        }
    }
}