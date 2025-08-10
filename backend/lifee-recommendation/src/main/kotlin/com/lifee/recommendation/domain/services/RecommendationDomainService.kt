package com.lifee.recommendation.domain.services

import com.lifee.recommendation.domain.aggregates.Recommendation
import com.lifee.recommendation.domain.entities.RecommendationItem
import com.lifee.recommendation.domain.valueobjects.*

/**
 * 推荐领域服务
 */
class RecommendationDomainService {
    
    /**
     * 计算推荐分数
     */
    fun calculateScore(
        userPreferences: Map<String, Double>,
        contentFeatures: Map<String, Double>,
        type: RecommendationType
    ): RecommendationScore {
        return when (type) {
            RecommendationType.CONTENT_BASED -> calculateContentBasedScore(userPreferences, contentFeatures)
            RecommendationType.COLLABORATIVE_FILTERING -> calculateCollaborativeScore(userPreferences, contentFeatures)
            RecommendationType.HYBRID -> calculateHybridScore(userPreferences, contentFeatures)
            RecommendationType.TRENDING -> calculateTrendingScore(contentFeatures)
            RecommendationType.PERSONALIZED -> calculatePersonalizedScore(userPreferences, contentFeatures)
        }
    }
    
    /**
     * 验证推荐项是否可以添加
     */
    fun canAddItem(recommendation: Recommendation, item: RecommendationItem): Boolean {
        // 检查是否已存在相同内容
        if (recommendation.getItems().any { it.getContentId() == item.getContentId() }) {
            return false
        }
        
        // 检查推荐数量限制
        if (recommendation.getItemCount() >= MAX_RECOMMENDATION_ITEMS) {
            return false
        }
        
        // 检查分数阈值
        if (item.getScore().value < MIN_SCORE_THRESHOLD) {
            return false
        }
        
        return true
    }
    
    /**
     * 过滤低质量推荐项
     */
    fun filterLowQualityItems(items: List<RecommendationItem>): List<RecommendationItem> {
        return items.filter { it.getScore().value >= MIN_SCORE_THRESHOLD }
            .sortedByDescending { it.getScore().value }
    }
    
    /**
     * 合并推荐列表
     */
    fun mergeRecommendations(
        primary: List<RecommendationItem>,
        secondary: List<RecommendationItem>
    ): List<RecommendationItem> {
        val merged = mutableListOf<RecommendationItem>()
        val contentIds = mutableSetOf<ContentId>()
        
        // 添加主要推荐
        primary.forEach { item ->
            if (contentIds.add(item.getContentId())) {
                merged.add(item)
            }
        }
        
        // 添加次要推荐（去重）
        secondary.forEach { item ->
            if (contentIds.add(item.getContentId()) && merged.size < MAX_RECOMMENDATION_ITEMS) {
                merged.add(item)
            }
        }
        
        return merged.sortedByDescending { it.getScore().value }
    }
    
    private fun calculateContentBasedScore(
        userPreferences: Map<String, Double>,
        contentFeatures: Map<String, Double>
    ): RecommendationScore {
        var score = 0.0
        var totalWeight = 0.0
        
        userPreferences.forEach { (feature, preference) ->
            contentFeatures[feature]?.let { contentValue ->
                score += preference * contentValue
                totalWeight += preference
            }
        }
        
        val normalizedScore = if (totalWeight > 0) score / totalWeight else 0.0
        return RecommendationScore.of(normalizedScore.coerceIn(0.0, 1.0))
    }
    
    private fun calculateCollaborativeScore(
        userPreferences: Map<String, Double>,
        contentFeatures: Map<String, Double>
    ): RecommendationScore {
        // 简化的协同过滤分数计算
        val similarity = contentFeatures["user_similarity"] ?: 0.5
        val popularity = contentFeatures["popularity"] ?: 0.3
        
        val score = (similarity * 0.7 + popularity * 0.3).coerceIn(0.0, 1.0)
        return RecommendationScore.of(score)
    }
    
    private fun calculateHybridScore(
        userPreferences: Map<String, Double>,
        contentFeatures: Map<String, Double>
    ): RecommendationScore {
        val contentScore = calculateContentBasedScore(userPreferences, contentFeatures)
        val collaborativeScore = calculateCollaborativeScore(userPreferences, contentFeatures)
        
        val hybridScore = (contentScore.value * 0.6 + collaborativeScore.value * 0.4)
        return RecommendationScore.of(hybridScore)
    }
    
    private fun calculateTrendingScore(contentFeatures: Map<String, Double>): RecommendationScore {
        val trendingScore = contentFeatures["trending_score"] ?: 0.5
        return RecommendationScore.of(trendingScore.coerceIn(0.0, 1.0))
    }
    
    private fun calculatePersonalizedScore(
        userPreferences: Map<String, Double>,
        contentFeatures: Map<String, Double>
    ): RecommendationScore {
        val personalizedScore = contentFeatures["personalized_score"] ?: 0.5
        val userEngagement = userPreferences["engagement"] ?: 0.5
        
        val score = (personalizedScore * 0.8 + userEngagement * 0.2).coerceIn(0.0, 1.0)
        return RecommendationScore.of(score)
    }
    
    companion object {
        private const val MAX_RECOMMENDATION_ITEMS = 50
        private const val MIN_SCORE_THRESHOLD = 0.1
    }
}