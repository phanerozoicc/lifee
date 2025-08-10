package com.lifee.recommendation.app.application.commands.handlers

import com.lifee.common.cqrs.commands.CommandHandler
import com.lifee.recommendation.app.application.commands.GenerateRecommendationCommand
import com.lifee.recommendation.domain.aggregates.Recommendation
import com.lifee.recommendation.domain.entities.RecommendationItem
import com.lifee.recommendation.domain.repositories.RecommendationRepository
import com.lifee.recommendation.domain.services.RecommendationDomainService
import com.lifee.recommendation.domain.services.RecommendationValidationService
import com.lifee.recommendation.domain.valueobjects.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 生成推荐命令处理器
 */
@Component
class GenerateRecommendationCommandHandler(
    private val recommendationRepository: RecommendationRepository,
    private val recommendationDomainService: RecommendationDomainService,
    private val recommendationValidationService: RecommendationValidationService
) : CommandHandler<GenerateRecommendationCommand> {
    
    private val logger = LoggerFactory.getLogger(GenerateRecommendationCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: GenerateRecommendationCommand) {
        logger.debug("开始处理生成推荐命令: userId={}, type={}, limit={}", 
            command.userId, command.type, command.limit)
        
        // 验证生成参数
        recommendationValidationService.validateGenerationParameters(
            userId = command.userId,
            type = command.type,
            limit = command.limit
        )
        
        // 生成推荐ID
        val recommendationId = RecommendationId.generate()
        
        // 创建推荐聚合
        val recommendation = Recommendation.create(
            recommendationId = recommendationId,
            userId = command.userId
        )
        
        // 生成推荐项
        val recommendationItems = generateRecommendationItems(
            userId = command.userId,
            type = command.type,
            limit = command.limit
        )
        
        // 验证批量操作
        if (recommendationItems.isNotEmpty()) {
            recommendationValidationService.validateBatchOperation(
                items = recommendationItems,
                operationType = RecommendationValidationService.BatchOperationType.ADD
            )
        }
        
        // 添加推荐项到推荐中
        recommendationItems.forEach { item ->
            // 验证每个推荐项
            recommendationValidationService.validateRecommendationItem(recommendation, item)
            
            // 添加推荐项
            if (recommendationDomainService.canAddItem(recommendation, item)) {
                recommendation.addItem(item)
            }
        }
        
        // 保存推荐
        recommendationRepository.save(recommendation)
        
        logger.info("成功生成推荐: recommendationId={}, userId={}, type={}, itemCount={}", 
            recommendationId, command.userId, command.type, recommendation.getItemCount())
    }
    
    /**
     * 生成推荐项
     * 这里是一个简化的实现，实际应该根据用户偏好和内容特征来生成
     */
    private suspend fun generateRecommendationItems(
        userId: UserId,
        type: RecommendationType,
        limit: Int
    ): List<RecommendationItem> {
        val items = mutableListOf<RecommendationItem>()
        
        // 模拟用户偏好数据
        val userPreferences = getUserPreferences(userId)
        
        // 模拟内容特征数据
        val availableContent = getAvailableContent(type, limit)
        
        availableContent.forEach { (contentId, contentFeatures) ->
            try {
                // 计算推荐分数
                val score = recommendationDomainService.calculateScore(
                    userPreferences = userPreferences,
                    contentFeatures = contentFeatures,
                    type = type
                )
                
                // 生成推荐原因
                val reason = generateRecommendationReason(type, contentFeatures, score.value)
                
                // 创建推荐项
                val item = RecommendationItem(
                    contentId = contentId,
                    score = score,
                    type = type,
                    reason = reason
                )
                
                items.add(item)
            } catch (e: Exception) {
                logger.warn("生成推荐项失败: contentId={}, error={}", contentId, e.message)
            }
        }
        
        // 过滤低质量推荐项并排序
        return recommendationDomainService.filterLowQualityItems(items)
            .take(limit)
    }
    
    /**
     * 获取用户偏好数据
     * 实际实现应该从用户行为数据中分析得出
     */
    private suspend fun getUserPreferences(userId: UserId): Map<String, Double> {
        // 模拟数据，实际应该从数据库或缓存中获取
        return mapOf(
            "technology" to 0.8,
            "business" to 0.6,
            "entertainment" to 0.4,
            "sports" to 0.3,
            "engagement" to 0.7
        )
    }
    
    /**
     * 获取可用内容数据
     * 实际实现应该从内容服务中获取
     */
    private suspend fun getAvailableContent(
        type: RecommendationType,
        limit: Int
    ): Map<ContentId, Map<String, Double>> {
        val content = mutableMapOf<ContentId, Map<String, Double>>()
        
        // 模拟生成内容数据
        repeat(limit * 2) { index -> // 生成更多内容以便筛选
            val contentId = ContentId.from("content_${type.name.lowercase()}_$index")
            val features = when (type) {
                RecommendationType.TRENDING -> mapOf(
                    "trending_score" to (0.5 + Math.random() * 0.5),
                    "popularity" to (0.3 + Math.random() * 0.7)
                )
                RecommendationType.PERSONALIZED -> mapOf(
                    "personalized_score" to (0.4 + Math.random() * 0.6),
                    "user_similarity" to (0.3 + Math.random() * 0.7)
                )
                RecommendationType.CONTENT_BASED -> mapOf(
                    "technology" to Math.random(),
                    "business" to Math.random(),
                    "entertainment" to Math.random()
                )
                RecommendationType.COLLABORATIVE_FILTERING -> mapOf(
                    "user_similarity" to (0.2 + Math.random() * 0.8),
                    "popularity" to (0.1 + Math.random() * 0.9)
                )
                RecommendationType.HYBRID -> mapOf(
                    "technology" to Math.random(),
                    "user_similarity" to Math.random(),
                    "popularity" to Math.random()
                )
            }
            content[contentId] = features
        }
        
        return content
    }
    
    /**
     * 生成推荐原因
     */
    private fun generateRecommendationReason(
        type: RecommendationType,
        contentFeatures: Map<String, Double>,
        score: Double
    ): String {
        return when (type) {
            RecommendationType.TRENDING -> {
                val trendingScore = contentFeatures["trending_score"] ?: 0.5
                "基于热门趋势推荐，热度评分: ${String.format("%.2f", trendingScore)}"
            }
            RecommendationType.PERSONALIZED -> {
                "基于个人偏好定制推荐，匹配度: ${String.format("%.2f", score)}"
            }
            RecommendationType.CONTENT_BASED -> {
                "基于内容相似性推荐，相似度评分: ${String.format("%.2f", score)}"
            }
            RecommendationType.COLLABORATIVE_FILTERING -> {
                val similarity = contentFeatures["user_similarity"] ?: 0.5
                "基于用户协同过滤推荐，用户相似度: ${String.format("%.2f", similarity)}"
            }
            RecommendationType.HYBRID -> {
                "基于混合算法推荐，综合评分: ${String.format("%.2f", score)}"
            }
        }
    }
}