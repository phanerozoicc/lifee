package com.lifee.recommendation.app.services

import com.lifee.common.cqrs.events.EventBus
import com.lifee.recommendation.domain.events.RecommendationComputedEvent
import com.lifee.user.domain.UserId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 推荐计算服务
 * 负责执行推荐算法，生成推荐结果
 */
@Service
class RecommendationComputationService(
    private val featureExtractionService: FeatureExtractionService,
    private val eventBus: EventBus
) {
    
    private val logger = LoggerFactory.getLogger(RecommendationComputationService::class.java)
    
    /**
     * 计算用户推荐
     */
    suspend fun computeRecommendations(
        userId: UserId,
        candidateItems: List<CandidateItem>,
        algorithm: RecommendationAlgorithm = RecommendationAlgorithm.HYBRID,
        topK: Int = 20
    ): List<RecommendationResult> {
        logger.debug("开始计算推荐: userId={}, candidateCount={}, algorithm={}, topK={}", 
            userId.value, candidateItems.size, algorithm, topK)
        
        val startTime = System.currentTimeMillis()
        
        try {
            // 1. 提取用户特征
            val userFeatures = featureExtractionService.extractUserFeatures(userId)
            
            // 2. 并行提取候选物品特征
            val itemFeaturesMap = withContext(Dispatchers.Default) {
                candidateItems.map { item ->
                    async {
                        item.itemId to featureExtractionService.extractItemFeatures(
                            itemId = item.itemId,
                            itemType = item.itemType,
                            metadata = item.metadata
                        )
                    }
                }.awaitAll().toMap()
            }
            
            // 3. 根据算法计算推荐分数
            val recommendations = when (algorithm) {
                RecommendationAlgorithm.COLLABORATIVE_FILTERING -> 
                    computeCollaborativeFiltering(userId, candidateItems, userFeatures, itemFeaturesMap)
                RecommendationAlgorithm.CONTENT_BASED -> 
                    computeContentBased(userId, candidateItems, userFeatures, itemFeaturesMap)
                RecommendationAlgorithm.MATRIX_FACTORIZATION -> 
                    computeMatrixFactorization(userId, candidateItems, userFeatures, itemFeaturesMap)
                RecommendationAlgorithm.DEEP_LEARNING -> 
                    computeDeepLearning(userId, candidateItems, userFeatures, itemFeaturesMap)
                RecommendationAlgorithm.HYBRID -> 
                    computeHybridRecommendation(userId, candidateItems, userFeatures, itemFeaturesMap)
            }
            
            // 4. 排序并取TopK
            val topRecommendations = recommendations
                .sortedByDescending { it.score }
                .take(topK)
            
            val computationTime = System.currentTimeMillis() - startTime
            
            // 5. 发布推荐计算事件
            val event = RecommendationComputedEvent(
                userId = userId,
                algorithm = algorithm.name,
                candidateCount = candidateItems.size,
                recommendationCount = topRecommendations.size,
                computationTimeMs = computationTime
            )
            eventBus.publish(event)
            
            logger.info("推荐计算完成: userId={}, recommendationCount={}, computationTime={}ms", 
                userId.value, topRecommendations.size, computationTime)
            
            return topRecommendations
            
        } catch (e: Exception) {
            logger.error("推荐计算失败: userId={}", userId.value, e)
            throw RecommendationComputationException("推荐计算失败: ${e.message}", e)
        }
    }
    
    /**
     * 协同过滤推荐
     */
    private suspend fun computeCollaborativeFiltering(
        userId: UserId,
        candidateItems: List<CandidateItem>,
        userFeatures: UserFeatures,
        itemFeaturesMap: Map<String, ItemFeatures>
    ): List<RecommendationResult> {
        return withContext(Dispatchers.Default) {
            candidateItems.map { item ->
                val itemFeatures = itemFeaturesMap[item.itemId]!!
                
                // 基于用户-物品交互的协同过滤分数
                val collaborativeScore = calculateCollaborativeScore(
                    userFeatures, itemFeatures
                )
                
                RecommendationResult(
                    itemId = item.itemId,
                    itemType = item.itemType,
                    score = collaborativeScore,
                    algorithm = "COLLABORATIVE_FILTERING",
                    explanation = "基于相似用户的偏好推荐"
                )
            }
        }
    }
    
    /**
     * 基于内容的推荐
     */
    private suspend fun computeContentBased(
        userId: UserId,
        candidateItems: List<CandidateItem>,
        userFeatures: UserFeatures,
        itemFeaturesMap: Map<String, ItemFeatures>
    ): List<RecommendationResult> {
        return withContext(Dispatchers.Default) {
            candidateItems.map { item ->
                val itemFeatures = itemFeaturesMap[item.itemId]!!
                
                // 基于内容相似度的推荐分数
                val contentScore = calculateContentSimilarity(
                    userFeatures, itemFeatures
                )
                
                RecommendationResult(
                    itemId = item.itemId,
                    itemType = item.itemType,
                    score = contentScore,
                    algorithm = "CONTENT_BASED",
                    explanation = "基于内容相似度推荐"
                )
            }
        }
    }
    
    /**
     * 矩阵分解推荐
     */
    private suspend fun computeMatrixFactorization(
        userId: UserId,
        candidateItems: List<CandidateItem>,
        userFeatures: UserFeatures,
        itemFeaturesMap: Map<String, ItemFeatures>
    ): List<RecommendationResult> {
        return withContext(Dispatchers.Default) {
            candidateItems.map { item ->
                val itemFeatures = itemFeaturesMap[item.itemId]!!
                
                // 基于矩阵分解的推荐分数
                val mfScore = calculateMatrixFactorizationScore(
                    userFeatures, itemFeatures
                )
                
                RecommendationResult(
                    itemId = item.itemId,
                    itemType = item.itemType,
                    score = mfScore,
                    algorithm = "MATRIX_FACTORIZATION",
                    explanation = "基于矩阵分解模型推荐"
                )
            }
        }
    }
    
    /**
     * 深度学习推荐
     */
    private suspend fun computeDeepLearning(
        userId: UserId,
        candidateItems: List<CandidateItem>,
        userFeatures: UserFeatures,
        itemFeaturesMap: Map<String, ItemFeatures>
    ): List<RecommendationResult> {
        return withContext(Dispatchers.Default) {
            candidateItems.map { item ->
                val itemFeatures = itemFeaturesMap[item.itemId]!!
                
                // 基于深度学习模型的推荐分数
                val dlScore = calculateDeepLearningScore(
                    userFeatures, itemFeatures
                )
                
                RecommendationResult(
                    itemId = item.itemId,
                    itemType = item.itemType,
                    score = dlScore,
                    algorithm = "DEEP_LEARNING",
                    explanation = "基于深度学习模型推荐"
                )
            }
        }
    }
    
    /**
     * 混合推荐
     */
    private suspend fun computeHybridRecommendation(
        userId: UserId,
        candidateItems: List<CandidateItem>,
        userFeatures: UserFeatures,
        itemFeaturesMap: Map<String, ItemFeatures>
    ): List<RecommendationResult> {
        return withContext(Dispatchers.Default) {
            candidateItems.map { item ->
                val itemFeatures = itemFeaturesMap[item.itemId]!!
                
                // 计算各种算法的分数
                val collaborativeScore = calculateCollaborativeScore(userFeatures, itemFeatures)
                val contentScore = calculateContentSimilarity(userFeatures, itemFeatures)
                val mfScore = calculateMatrixFactorizationScore(userFeatures, itemFeatures)
                val dlScore = calculateDeepLearningScore(userFeatures, itemFeatures)
                
                // 加权融合
                val hybridScore = 0.3 * collaborativeScore + 
                                 0.3 * contentScore + 
                                 0.2 * mfScore + 
                                 0.2 * dlScore
                
                RecommendationResult(
                    itemId = item.itemId,
                    itemType = item.itemType,
                    score = hybridScore,
                    algorithm = "HYBRID",
                    explanation = "基于多算法融合推荐"
                )
            }
        }
    }
    
    /**
     * 计算协同过滤分数
     */
    private fun calculateCollaborativeScore(
        userFeatures: UserFeatures,
        itemFeatures: ItemFeatures
    ): Double {
        // TODO: 实现真实的协同过滤算法
        // 这里使用简化的计算方式
        val userEngagement = userFeatures.basicFeatures["weighted_engagement_score"] ?: 0.0
        val itemPopularity = itemFeatures.statisticalFeatures["popularity_score"] ?: 0.5
        
        return (userEngagement * itemPopularity) / 100.0
    }
    
    /**
     * 计算内容相似度分数
     */
    private fun calculateContentSimilarity(
        userFeatures: UserFeatures,
        itemFeatures: ItemFeatures
    ): Double {
        // 计算用户偏好和物品特征的余弦相似度
        val userVector = userFeatures.getAllFeatures().values.toDoubleArray()
        val itemVector = itemFeatures.getAllFeatures().values.toDoubleArray()
        
        return cosineSimilarity(userVector, itemVector)
    }
    
    /**
     * 计算矩阵分解分数
     */
    private fun calculateMatrixFactorizationScore(
        userFeatures: UserFeatures,
        itemFeatures: ItemFeatures
    ): Double {
        // TODO: 实现真实的矩阵分解算法
        // 这里使用简化的计算方式
        val userLatentFactors = extractLatentFactors(userFeatures.getAllFeatures())
        val itemLatentFactors = extractLatentFactors(itemFeatures.getAllFeatures())
        
        return dotProduct(userLatentFactors, itemLatentFactors)
    }
    
    /**
     * 计算深度学习分数
     */
    private fun calculateDeepLearningScore(
        userFeatures: UserFeatures,
        itemFeatures: ItemFeatures
    ): Double {
        // TODO: 实现真实的深度学习模型推理
        // 这里使用简化的神经网络模拟
        val input = combineFeatures(userFeatures.getAllFeatures(), itemFeatures.getAllFeatures())
        return neuralNetworkPredict(input)
    }
    
    /**
     * 余弦相似度计算
     */
    private fun cosineSimilarity(vector1: DoubleArray, vector2: DoubleArray): Double {
        if (vector1.size != vector2.size) return 0.0
        
        val dotProduct = vector1.zip(vector2).sumOf { it.first * it.second }
        val norm1 = sqrt(vector1.sumOf { it * it })
        val norm2 = sqrt(vector2.sumOf { it * it })
        
        return if (norm1 > 0 && norm2 > 0) dotProduct / (norm1 * norm2) else 0.0
    }
    
    /**
     * 提取潜在因子
     */
    private fun extractLatentFactors(features: Map<String, Double>): DoubleArray {
        // 简化的潜在因子提取
        return features.values.take(10).toDoubleArray()
    }
    
    /**
     * 向量点积
     */
    private fun dotProduct(vector1: DoubleArray, vector2: DoubleArray): Double {
        return vector1.zip(vector2).sumOf { it.first * it.second }
    }
    
    /**
     * 特征组合
     */
    private fun combineFeatures(
        userFeatures: Map<String, Double>,
        itemFeatures: Map<String, Double>
    ): DoubleArray {
        val combined = mutableListOf<Double>()
        combined.addAll(userFeatures.values)
        combined.addAll(itemFeatures.values)
        return combined.toDoubleArray()
    }
    
    /**
     * 简化的神经网络预测
     */
    private fun neuralNetworkPredict(input: DoubleArray): Double {
        // 简化的单层神经网络
        val weights = DoubleArray(input.size) { 0.1 }
        val weightedSum = input.zip(weights).sumOf { it.first * it.second }
        
        // Sigmoid激活函数
        return 1.0 / (1.0 + exp(-weightedSum))
    }
}

/**
 * 候选物品
 */
data class CandidateItem(
    val itemId: String,
    val itemType: String,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * 推荐结果
 */
data class RecommendationResult(
    val itemId: String,
    val itemType: String,
    val score: Double,
    val algorithm: String,
    val explanation: String
)

/**
 * 推荐算法类型
 */
enum class RecommendationAlgorithm {
    COLLABORATIVE_FILTERING,
    CONTENT_BASED,
    MATRIX_FACTORIZATION,
    DEEP_LEARNING,
    HYBRID
}

/**
 * 推荐计算异常
 */
class RecommendationComputationException(message: String, cause: Throwable? = null) : 
    RuntimeException(message, cause)