package com.lifee.recommendation.app.services

import com.lifee.common.cqrs.events.EventBus
import com.lifee.recommendation.domain.events.UserBehaviorCollectedEvent
import com.lifee.user.domain.UserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant

/**
 * 用户行为收集服务
 * 负责收集和记录用户的各种行为数据
 */
@Service
class UserBehaviorCollectionService(
    private val eventBus: EventBus
) {
    
    private val logger = LoggerFactory.getLogger(UserBehaviorCollectionService::class.java)
    
    /**
     * 收集用户行为
     */
    suspend fun collectBehavior(
        userId: UserId,
        behaviorType: UserBehaviorType,
        targetId: String,
        targetType: String,
        metadata: Map<String, Any> = emptyMap()
    ) {
        logger.debug("收集用户行为: userId={}, behaviorType={}, targetType={}", 
            userId.value, behaviorType, targetType)
        
        try {
            // 1. 创建行为记录
            val behavior = UserBehavior(
                userId = userId,
                behaviorType = behaviorType,
                targetId = targetId,
                targetType = targetType,
                metadata = metadata,
                timestamp = Instant.now()
            )
            
            // 2. 存储行为数据
            storeBehavior(behavior)
            
            // 3. 发布行为收集事件
            val event = UserBehaviorCollectedEvent(
                userId = userId,
                behaviorType = behaviorType.name,
                targetId = targetId,
                targetType = targetType,
                metadata = metadata,
                timestamp = behavior.timestamp
            )
            eventBus.publish(event)
            
            logger.info("用户行为收集完成: userId={}, behaviorType={}", 
                userId.value, behaviorType)
                
        } catch (e: Exception) {
            logger.error("用户行为收集失败: userId={}, behaviorType={}", 
                userId.value, behaviorType, e)
            throw UserBehaviorCollectionException("行为收集失败: ${e.message}", e)
        }
    }
    
    /**
     * 批量收集用户行为
     */
    suspend fun collectBehaviors(behaviors: List<UserBehavior>) {
        logger.debug("批量收集用户行为: count={}", behaviors.size)
        
        try {
            behaviors.forEach { behavior ->
                collectBehavior(
                    userId = behavior.userId,
                    behaviorType = behavior.behaviorType,
                    targetId = behavior.targetId,
                    targetType = behavior.targetType,
                    metadata = behavior.metadata
                )
            }
            
            logger.info("批量用户行为收集完成: count={}", behaviors.size)
            
        } catch (e: Exception) {
            logger.error("批量用户行为收集失败: count={}", behaviors.size, e)
            throw UserBehaviorCollectionException("批量行为收集失败: ${e.message}", e)
        }
    }
    
    /**
     * 获取用户行为历史
     */
    suspend fun getUserBehaviorHistory(
        userId: UserId,
        behaviorTypes: List<UserBehaviorType> = emptyList(),
        limit: Int = 100
    ): List<UserBehavior> {
        logger.debug("获取用户行为历史: userId={}, limit={}", userId.value, limit)
        
        try {
            // 实现从数据库查询用户行为历史
            // 这里使用模拟数据，实际项目中应该从时序数据库或NoSQL数据库查询
            val mockBehaviors = generateMockBehaviorHistory(userId, behaviorTypes, limit)
            
            logger.debug("获取用户行为历史成功: userId={}, count={}", userId.value, mockBehaviors.size)
            return mockBehaviors
            
        } catch (e: Exception) {
            logger.error("获取用户行为历史失败: userId={}", userId.value, e)
            throw UserBehaviorCollectionException("获取行为历史失败: ${e.message}", e)
        }
    }
    
    /**
     * 生成模拟行为历史数据
     * 实际项目中应该替换为真实的数据库查询逻辑
     */
    private fun generateMockBehaviorHistory(
        userId: UserId,
        behaviorTypes: List<UserBehaviorType>,
        limit: Int
    ): List<UserBehavior> {
        val targetTypes = listOf("document", "knowledge_base", "conversation", "recommendation")
        val behaviors = mutableListOf<UserBehavior>()
        
        val typesToGenerate = if (behaviorTypes.isNotEmpty()) behaviorTypes else UserBehaviorType.values().toList()
        
        repeat(minOf(limit, 50)) { index ->
            val behaviorType = typesToGenerate.random()
            val targetType = targetTypes.random()
            val targetId = "${targetType}_${(1000..9999).random()}"
            
            val metadata = when (behaviorType) {
                UserBehaviorType.VIEW -> mapOf(
                    "duration_seconds" to (10..300).random(),
                    "scroll_percentage" to (20..100).random()
                )
                UserBehaviorType.SEARCH -> mapOf(
                    "query" to "sample query $index",
                    "results_count" to (1..20).random()
                )
                UserBehaviorType.RATE -> mapOf(
                    "rating" to (1..5).random(),
                    "review" to "Sample review $index"
                )
                UserBehaviorType.STAY_TIME -> mapOf(
                    "stay_duration_seconds" to (30..1800).random()
                )
                else -> mapOf(
                    "source" to "web",
                    "session_id" to "session_${(100..999).random()}"
                )
            }
            
            behaviors.add(
                UserBehavior(
                    userId = userId,
                    behaviorType = behaviorType,
                    targetId = targetId,
                    targetType = targetType,
                    metadata = metadata,
                    timestamp = Instant.now().minusSeconds((index * 3600).toLong())
                )
            )
        }
        
        return behaviors.sortedByDescending { it.timestamp }
    }
    
    /**
     * 存储行为数据
     */
    private suspend fun storeBehavior(behavior: UserBehavior) {
        // 实现行为数据存储逻辑
        // 在实际项目中，这里应该存储到时序数据库（如InfluxDB）或NoSQL数据库（如MongoDB）
        
        try {
            // 模拟存储逻辑 - 实际项目中应该替换为真实的数据库操作
            val behaviorRecord = BehaviorRecord(
                id = generateBehaviorId(),
                userId = behavior.userId.value,
                behaviorType = behavior.behaviorType.name,
                targetId = behavior.targetId,
                targetType = behavior.targetType,
                metadata = behavior.metadata,
                timestamp = behavior.timestamp,
                createdAt = Instant.now()
            )
            
            // 这里应该调用实际的数据库存储服务
            // 例如：behaviorRepository.save(behaviorRecord)
            // 或者：influxDbClient.writePoint(behaviorRecord.toInfluxPoint())
            
            logger.debug("用户行为存储成功: id={}, userId={}, behaviorType={}", 
                behaviorRecord.id, behavior.userId.value, behavior.behaviorType)
                
            // 可选：异步更新用户行为统计
            updateUserBehaviorStatistics(behavior)
            
        } catch (e: Exception) {
            logger.error("存储用户行为失败: userId={}, behaviorType={}", 
                behavior.userId.value, behavior.behaviorType, e)
            throw UserBehaviorCollectionException("行为数据存储失败: ${e.message}", e)
        }
    }
    
    /**
     * 生成行为记录ID
     */
    private fun generateBehaviorId(): String {
        return "behavior_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }
    
    /**
     * 更新用户行为统计
     */
    private suspend fun updateUserBehaviorStatistics(behavior: UserBehavior) {
        try {
            // 这里可以更新用户行为统计信息，用于推荐算法
            // 例如：更新用户偏好、活跃度、兴趣标签等
            logger.debug("更新用户行为统计: userId={}, behaviorType={}", 
                behavior.userId.value, behavior.behaviorType)
                
            // 实际实现中可以调用统计服务
            // statisticsService.updateUserBehaviorStats(behavior)
            
        } catch (e: Exception) {
            logger.warn("更新用户行为统计失败: userId={}", behavior.userId.value, e)
            // 统计更新失败不应该影响主流程
        }
    }
}

/**
 * 用户行为类型
 */
enum class UserBehaviorType {
    VIEW,           // 浏览
    CLICK,          // 点击
    LIKE,           // 点赞
    SHARE,          // 分享
    COMMENT,        // 评论
    SEARCH,         // 搜索
    DOWNLOAD,       // 下载
    BOOKMARK,       // 收藏
    PURCHASE,       // 购买
    RATE,           // 评分
    FOLLOW,         // 关注
    UNFOLLOW,       // 取消关注
    STAY_TIME       // 停留时间
}

/**
 * 用户行为数据
 */
data class UserBehavior(
    val userId: UserId,
    val behaviorType: UserBehaviorType,
    val targetId: String,
    val targetType: String,
    val metadata: Map<String, Any> = emptyMap(),
    val timestamp: Instant
)

/**
 * 行为记录数据类
 * 用于数据库存储的行为记录结构
 */
data class BehaviorRecord(
    val id: String,
    val userId: String,
    val behaviorType: String,
    val targetId: String,
    val targetType: String,
    val metadata: Map<String, Any>,
    val timestamp: Instant,
    val createdAt: Instant
)

/**
 * 用户行为收集异常
 */
class UserBehaviorCollectionException(message: String, cause: Throwable? = null) : 
    RuntimeException(message, cause)