package com.lifee.recommendation.app.services

import com.lifee.common.cqrs.events.EventBus
import com.lifee.recommendation.domain.events.FeatureExtractedEvent
import com.lifee.user.domain.UserId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.log
import kotlin.math.sqrt

/**
 * 特征提取服务
 * 负责从用户行为数据中提取推荐算法所需的特征
 */
@Service
class FeatureExtractionService(
    private val userBehaviorCollectionService: UserBehaviorCollectionService,
    private val eventBus: EventBus
) {
    
    private val logger = LoggerFactory.getLogger(FeatureExtractionService::class.java)
    
    /**
     * 提取用户特征
     */
    suspend fun extractUserFeatures(
        userId: UserId,
        timeWindowDays: Int = 30
    ): UserFeatures {
        logger.debug("开始提取用户特征: userId={}, timeWindow={}天", userId.value, timeWindowDays)
        
        try {
            // 1. 获取用户行为历史
            val behaviors = userBehaviorCollectionService.getUserBehaviorHistory(
                userId = userId,
                limit = 1000
            )
            
            // 2. 提取基础特征
            val basicFeatures = withContext(Dispatchers.Default) {
                extractBasicFeatures(behaviors)
            }
            
            // 3. 提取行为偏好特征
            val preferenceFeatures = withContext(Dispatchers.Default) {
                extractPreferenceFeatures(behaviors)
            }
            
            // 4. 提取时间模式特征
            val temporalFeatures = withContext(Dispatchers.Default) {
                extractTemporalFeatures(behaviors)
            }
            
            // 5. 提取内容偏好特征
            val contentFeatures = withContext(Dispatchers.Default) {
                extractContentFeatures(behaviors)
            }
            
            // 6. 组合所有特征
            val userFeatures = UserFeatures(
                userId = userId,
                basicFeatures = basicFeatures,
                preferenceFeatures = preferenceFeatures,
                temporalFeatures = temporalFeatures,
                contentFeatures = contentFeatures
            )
            
            // 7. 发布特征提取事件
            val event = FeatureExtractedEvent(
                userId = userId,
                featureType = "USER_FEATURES",
                featureCount = userFeatures.getTotalFeatureCount(),
                extractionTimeMs = 0L // 这里可以记录实际提取时间
            )
            eventBus.publish(event)
            
            logger.info("用户特征提取完成: userId={}, featureCount={}", 
                userId.value, userFeatures.getTotalFeatureCount())
            
            return userFeatures
            
        } catch (e: Exception) {
            logger.error("用户特征提取失败: userId={}", userId.value, e)
            throw FeatureExtractionException("特征提取失败: ${e.message}", e)
        }
    }
    
    /**
     * 提取物品特征
     */
    suspend fun extractItemFeatures(
        itemId: String,
        itemType: String,
        metadata: Map<String, Any> = emptyMap()
    ): ItemFeatures {
        logger.debug("开始提取物品特征: itemId={}, itemType={}", itemId, itemType)
        
        try {
            // 1. 提取内容特征
            val contentFeatures = extractItemContentFeatures(itemId, itemType, metadata)
            
            // 2. 提取统计特征
            val statisticalFeatures = extractItemStatisticalFeatures(itemId, itemType)
            
            // 3. 提取协同特征
            val collaborativeFeatures = extractItemCollaborativeFeatures(itemId, itemType)
            
            val itemFeatures = ItemFeatures(
                itemId = itemId,
                itemType = itemType,
                contentFeatures = contentFeatures,
                statisticalFeatures = statisticalFeatures,
                collaborativeFeatures = collaborativeFeatures
            )
            
            logger.info("物品特征提取完成: itemId={}, featureCount={}", 
                itemId, itemFeatures.getTotalFeatureCount())
            
            return itemFeatures
            
        } catch (e: Exception) {
            logger.error("物品特征提取失败: itemId={}", itemId, e)
            throw FeatureExtractionException("物品特征提取失败: ${e.message}", e)
        }
    }
    
    /**
     * 提取基础特征
     */
    private fun extractBasicFeatures(behaviors: List<UserBehavior>): Map<String, Double> {
        val features = mutableMapOf<String, Double>()
        
        // 总行为次数
        features["total_behaviors"] = behaviors.size.toDouble()
        
        // 各类型行为次数
        UserBehaviorType.values().forEach { type ->
            val count = behaviors.count { it.behaviorType == type }
            features["${type.name.lowercase()}_count"] = count.toDouble()
        }
        
        // 活跃度指标
        val uniqueDays = behaviors.map { 
            it.timestamp.toString().substring(0, 10) 
        }.distinct().size
        features["active_days"] = uniqueDays.toDouble()
        
        if (uniqueDays > 0) {
            features["avg_behaviors_per_day"] = behaviors.size.toDouble() / uniqueDays
        }
        
        return features
    }
    
    /**
     * 提取偏好特征
     */
    private fun extractPreferenceFeatures(behaviors: List<UserBehavior>): Map<String, Double> {
        val features = mutableMapOf<String, Double>()
        
        // 内容类型偏好
        val contentTypePreferences = behaviors.groupBy { it.targetType }
            .mapValues { it.value.size.toDouble() }
        
        contentTypePreferences.forEach { (type, count) ->
            features["preference_${type.lowercase()}"] = count / behaviors.size
        }
        
        // 行为强度偏好（不同行为的权重）
        val behaviorWeights = mapOf(
            UserBehaviorType.VIEW to 1.0,
            UserBehaviorType.CLICK to 2.0,
            UserBehaviorType.LIKE to 3.0,
            UserBehaviorType.SHARE to 4.0,
            UserBehaviorType.COMMENT to 5.0,
            UserBehaviorType.BOOKMARK to 6.0
        )
        
        val weightedScore = behaviors.sumOf { behavior ->
            behaviorWeights[behavior.behaviorType] ?: 1.0
        }
        features["weighted_engagement_score"] = weightedScore
        
        return features
    }
    
    /**
     * 提取时间模式特征
     */
    private fun extractTemporalFeatures(behaviors: List<UserBehavior>): Map<String, Double> {
        val features = mutableMapOf<String, Double>()
        
        if (behaviors.isEmpty()) return features
        
        // 活跃时段分析
        val hourlyActivity = IntArray(24)
        behaviors.forEach { behavior ->
            val hour = behavior.timestamp.toString().substring(11, 13).toInt()
            hourlyActivity[hour]++
        }
        
        // 找出最活跃的时段
        val maxActivityHour = hourlyActivity.indices.maxByOrNull { hourlyActivity[it] } ?: 0
        features["most_active_hour"] = maxActivityHour.toDouble()
        
        // 活跃时段分布的方差（衡量活跃时间的集中程度）
        val avgActivity = hourlyActivity.average()
        val variance = hourlyActivity.map { (it - avgActivity) * (it - avgActivity) }.average()
        features["activity_time_variance"] = variance
        
        return features
    }
    
    /**
     * 提取内容偏好特征
     */
    private fun extractContentFeatures(behaviors: List<UserBehavior>): Map<String, Double> {
        val features = mutableMapOf<String, Double>()
        
        // 内容多样性（香农熵）
        val contentCounts = behaviors.groupBy { it.targetId }.mapValues { it.value.size }
        val totalBehaviors = behaviors.size.toDouble()
        
        val entropy = contentCounts.values.sumOf { count ->
            val probability = count / totalBehaviors
            if (probability > 0) -probability * log(probability, 2.0) else 0.0
        }
        features["content_diversity_entropy"] = entropy
        
        // 重复访问率
        val repeatVisits = contentCounts.values.count { it > 1 }
        features["repeat_visit_rate"] = repeatVisits.toDouble() / contentCounts.size
        
        return features
    }
    
    /**
     * 提取物品内容特征
     */
    private fun extractItemContentFeatures(
        itemId: String,
        itemType: String,
        metadata: Map<String, Any>
    ): Map<String, Double> {
        val features = mutableMapOf<String, Double>()
        
        // TODO: 根据物品类型和元数据提取内容特征
        // 例如：文本长度、关键词、分类、标签等
        
        return features
    }
    
    /**
     * 提取物品统计特征
     */
    private fun extractItemStatisticalFeatures(
        itemId: String,
        itemType: String
    ): Map<String, Double> {
        val features = mutableMapOf<String, Double>()
        
        // TODO: 提取物品的统计特征
        // 例如：浏览量、点赞数、评论数、分享数等
        
        return features
    }
    
    /**
     * 提取物品协同特征
     */
    private fun extractItemCollaborativeFeatures(
        itemId: String,
        itemType: String
    ): Map<String, Double> {
        val features = mutableMapOf<String, Double>()
        
        // TODO: 提取物品的协同过滤特征
        // 例如：相似物品、用户-物品交互模式等
        
        return features
    }
}

/**
 * 用户特征
 */
data class UserFeatures(
    val userId: UserId,
    val basicFeatures: Map<String, Double>,
    val preferenceFeatures: Map<String, Double>,
    val temporalFeatures: Map<String, Double>,
    val contentFeatures: Map<String, Double>
) {
    fun getTotalFeatureCount(): Int {
        return basicFeatures.size + preferenceFeatures.size + 
               temporalFeatures.size + contentFeatures.size
    }
    
    fun getAllFeatures(): Map<String, Double> {
        return basicFeatures + preferenceFeatures + temporalFeatures + contentFeatures
    }
}

/**
 * 物品特征
 */
data class ItemFeatures(
    val itemId: String,
    val itemType: String,
    val contentFeatures: Map<String, Double>,
    val statisticalFeatures: Map<String, Double>,
    val collaborativeFeatures: Map<String, Double>
) {
    fun getTotalFeatureCount(): Int {
        return contentFeatures.size + statisticalFeatures.size + collaborativeFeatures.size
    }
    
    fun getAllFeatures(): Map<String, Double> {
        return contentFeatures + statisticalFeatures + collaborativeFeatures
    }
}

/**
 * 特征提取异常
 */
class FeatureExtractionException(message: String, cause: Throwable? = null) : 
    RuntimeException(message, cause)