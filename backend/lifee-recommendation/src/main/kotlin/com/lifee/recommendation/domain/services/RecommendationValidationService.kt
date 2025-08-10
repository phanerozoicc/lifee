package com.lifee.recommendation.domain.services

import com.lifee.recommendation.domain.aggregates.Recommendation
import com.lifee.recommendation.domain.entities.RecommendationItem
import com.lifee.recommendation.domain.valueobjects.*
import org.springframework.stereotype.Service

/**
 * 推荐验证服务
 * 实现推荐模块的业务规则验证
 */
@Service
class RecommendationValidationService {
    
    /**
     * 验证推荐项是否可以添加
     */
    fun validateRecommendationItem(
        recommendation: Recommendation,
        item: RecommendationItem
    ) {
        // 验证推荐项数量限制
        validateItemCountLimit(recommendation)
        
        // 验证推荐项质量
        validateItemQuality(item)
        
        // 验证推荐项内容
        validateItemContent(item)
        
        // 验证推荐项唯一性
        validateItemUniqueness(recommendation, item)
        
        // 验证推荐项类型一致性
        validateItemTypeConsistency(recommendation, item)
    }
    
    /**
     * 验证推荐生成参数
     */
    fun validateGenerationParameters(
        userId: UserId,
        type: RecommendationType,
        limit: Int
    ) {
        // 验证推荐数量限制
        if (limit <= 0) {
            throw IllegalArgumentException("推荐数量必须大于0")
        }
        
        if (limit > MAX_GENERATION_LIMIT) {
            throw IllegalArgumentException("推荐数量不能超过${MAX_GENERATION_LIMIT}个")
        }
        
        // 验证推荐类型
        validateRecommendationType(type)
    }
    
    /**
     * 验证推荐分数更新
     */
    fun validateScoreUpdate(
        oldScore: Double,
        newScore: Double,
        reason: String?
    ) {
        // 验证分数范围
        if (newScore < MIN_SCORE || newScore > MAX_SCORE) {
            throw IllegalArgumentException("推荐分数必须在${MIN_SCORE}到${MAX_SCORE}之间")
        }
        
        // 验证分数变化幅度
        val scoreDifference = kotlin.math.abs(newScore - oldScore)
        if (scoreDifference > MAX_SCORE_CHANGE && reason.isNullOrBlank()) {
            throw IllegalArgumentException("分数变化幅度超过${MAX_SCORE_CHANGE}时必须提供原因")
        }
        
        // 验证分数变化原因
        if (!reason.isNullOrBlank() && reason.length > MAX_REASON_LENGTH) {
            throw IllegalArgumentException("分数变化原因不能超过${MAX_REASON_LENGTH}个字符")
        }
    }
    
    /**
     * 验证推荐项批量操作
     */
    fun validateBatchOperation(
        items: List<RecommendationItem>,
        operationType: BatchOperationType
    ) {
        if (items.isEmpty()) {
            throw IllegalArgumentException("批量操作的推荐项列表不能为空")
        }
        
        if (items.size > MAX_BATCH_SIZE) {
            throw IllegalArgumentException("批量操作的推荐项数量不能超过${MAX_BATCH_SIZE}个")
        }
        
        when (operationType) {
            BatchOperationType.ADD -> {
                items.forEach { validateItemQuality(it) }
            }
            BatchOperationType.UPDATE -> {
                items.forEach { validateItemContent(it) }
            }
            BatchOperationType.REMOVE -> {
                // 移除操作只需要验证数量
            }
        }
    }
    
    private fun validateItemCountLimit(recommendation: Recommendation) {
        if (recommendation.getItemCount() >= MAX_ITEMS_PER_RECOMMENDATION) {
            throw IllegalArgumentException("推荐项数量不能超过${MAX_ITEMS_PER_RECOMMENDATION}个")
        }
    }
    
    private fun validateItemQuality(item: RecommendationItem) {
        val score = item.getScore().value
        
        // 验证最低质量分数
        if (score < MIN_QUALITY_SCORE) {
            throw IllegalArgumentException("推荐项质量分数不能低于${MIN_QUALITY_SCORE}")
        }
        
        // 验证高质量推荐的特殊要求
        if (score >= HIGH_QUALITY_THRESHOLD) {
            validateHighQualityItem(item)
        }
    }
    
    private fun validateHighQualityItem(item: RecommendationItem) {
        val reason = item.getReason()
        
        // 高质量推荐必须有详细的推荐原因
        if (reason.length < MIN_HIGH_QUALITY_REASON_LENGTH) {
            throw IllegalArgumentException("高质量推荐项必须提供至少${MIN_HIGH_QUALITY_REASON_LENGTH}个字符的推荐原因")
        }
        
        // 验证推荐原因的质量
        if (!isValidReasonContent(reason)) {
            throw IllegalArgumentException("推荐原因包含无效内容")
        }
    }
    
    private fun validateItemContent(item: RecommendationItem) {
        val reason = item.getReason()
        
        // 验证推荐原因长度
        if (reason.isBlank()) {
            throw IllegalArgumentException("推荐原因不能为空")
        }
        
        if (reason.length > MAX_REASON_LENGTH) {
            throw IllegalArgumentException("推荐原因不能超过${MAX_REASON_LENGTH}个字符")
        }
        
        // 验证推荐原因内容
        if (!isValidReasonContent(reason)) {
            throw IllegalArgumentException("推荐原因包含无效内容")
        }
        
        // 验证内容ID格式
        validateContentId(item.getContentId())
    }
    
    private fun validateItemUniqueness(recommendation: Recommendation, item: RecommendationItem) {
        if (recommendation.getItems().any { it.getContentId() == item.getContentId() }) {
            throw IllegalArgumentException("推荐项已存在: ${item.getContentId()}")
        }
    }
    
    private fun validateItemTypeConsistency(recommendation: Recommendation, item: RecommendationItem) {
        val existingTypes = recommendation.getItems().map { it.getType() }.toSet()
        
        // 如果已有推荐项，检查类型一致性
        if (existingTypes.isNotEmpty() && existingTypes.size >= MAX_MIXED_TYPES) {
            if (!existingTypes.contains(item.getType())) {
                throw IllegalArgumentException("推荐中已包含${MAX_MIXED_TYPES}种不同类型，不能再添加新类型")
            }
        }
    }
    
    private fun validateRecommendationType(type: RecommendationType) {
        // 验证推荐类型是否支持
        when (type) {
            RecommendationType.CONTENT_BASED,
            RecommendationType.COLLABORATIVE_FILTERING,
            RecommendationType.HYBRID,
            RecommendationType.TRENDING,
            RecommendationType.PERSONALIZED -> {
                // 支持的类型
            }
        }
    }
    
    private fun validateContentId(contentId: ContentId) {
        val idValue = contentId.value
        
        // 验证内容ID格式
        if (!idValue.matches(CONTENT_ID_PATTERN)) {
            throw IllegalArgumentException("内容ID格式无效: $idValue")
        }
        
        // 验证内容ID长度
        if (idValue.length > MAX_CONTENT_ID_LENGTH) {
            throw IllegalArgumentException("内容ID长度不能超过${MAX_CONTENT_ID_LENGTH}个字符")
        }
    }
    
    private fun isValidReasonContent(reason: String): Boolean {
        // 检查是否包含敏感词
        if (SENSITIVE_WORDS.any { reason.contains(it, ignoreCase = true) }) {
            return false
        }
        
        // 检查是否包含过多重复字符
        if (hasExcessiveRepeatedChars(reason)) {
            return false
        }
        
        // 检查是否包含有效的推荐信息
        if (!hasValidRecommendationInfo(reason)) {
            return false
        }
        
        return true
    }
    
    private fun hasExcessiveRepeatedChars(text: String): Boolean {
        var consecutiveCount = 1
        var lastChar = text.firstOrNull() ?: return false
        
        for (i in 1 until text.length) {
            if (text[i] == lastChar) {
                consecutiveCount++
                if (consecutiveCount > MAX_CONSECUTIVE_CHARS) {
                    return true
                }
            } else {
                consecutiveCount = 1
                lastChar = text[i]
            }
        }
        
        return false
    }
    
    private fun hasValidRecommendationInfo(reason: String): Boolean {
        // 检查是否包含有效的推荐关键词
        return VALID_REASON_KEYWORDS.any { reason.contains(it, ignoreCase = true) }
    }
    
    enum class BatchOperationType {
        ADD, UPDATE, REMOVE
    }
    
    companion object {
        // 推荐项数量限制
        private const val MAX_ITEMS_PER_RECOMMENDATION = 100
        private const val MAX_GENERATION_LIMIT = 50
        private const val MAX_BATCH_SIZE = 20
        
        // 分数相关常量
        private const val MIN_SCORE = 0.0
        private const val MAX_SCORE = 1.0
        private const val MIN_QUALITY_SCORE = 0.1
        private const val HIGH_QUALITY_THRESHOLD = 0.8
        private const val MAX_SCORE_CHANGE = 0.3
        
        // 内容相关常量
        private const val MAX_REASON_LENGTH = 500
        private const val MIN_HIGH_QUALITY_REASON_LENGTH = 50
        private const val MAX_CONTENT_ID_LENGTH = 100
        private const val MAX_CONSECUTIVE_CHARS = 3
        private const val MAX_MIXED_TYPES = 3
        
        // 内容ID格式验证
        private val CONTENT_ID_PATTERN = Regex("^[a-zA-Z0-9_-]+$")
        
        // 敏感词列表
        private val SENSITIVE_WORDS = listOf(
            "垃圾", "废物", "欺骗", "诈骗", "病毒", "恶意",
            "spam", "fraud", "scam", "malware", "virus"
        )
        
        // 有效推荐原因关键词
        private val VALID_REASON_KEYWORDS = listOf(
            "相似", "喜欢", "推荐", "热门", "优质", "匹配", "适合",
            "similar", "like", "recommend", "popular", "quality", "match", "suitable"
        )
    }
}