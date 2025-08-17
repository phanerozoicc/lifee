package com.lifee.recommendation.app.services

import com.lifee.common.cqrs.events.EventBus
import com.lifee.recommendation.domain.events.RecommendationCachedEvent
import com.lifee.user.domain.UserId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * 推荐缓存服务
 * 负责推荐结果的缓存、更新和失效管理
 * 
 * 扩展优化策略：
 * 1. 缓存分层优化：
 *    - L1缓存：本地内存缓存（Caffeine）用于热点数据，减少Redis网络开销
 *    - L2缓存：Redis分布式缓存用于共享数据
 *    - L3缓存：数据库缓存用于持久化存储
 * 
 * 2. 智能缓存策略：
 *    - 基于用户活跃度的动态TTL：活跃用户短TTL，非活跃用户长TTL
 *    - 预测性缓存预热：基于用户行为模式预加载推荐结果
 *    - 缓存穿透保护：使用布隆过滤器防止无效查询
 * 
 * 3. 性能优化：
 *    - 异步缓存更新：使用消息队列异步更新缓存，避免阻塞主流程
 *    - 批量操作：支持批量缓存和批量失效操作
 *    - 压缩存储：对大型推荐结果使用压缩算法减少内存占用
 * 
 * 4. 算法优化：
 *    - 增量更新：支持推荐结果的增量更新而非全量替换
 *    - 多版本缓存：支持A/B测试的多版本推荐算法缓存
 *    - 个性化TTL：基于推荐质量和用户反馈动态调整缓存时间
 * 
 * 5. 监控和告警：
 *    - 缓存命中率监控：实时监控各类缓存的命中率
 *    - 性能指标收集：响应时间、吞吐量、错误率统计
 *    - 自动扩容：基于负载自动调整缓存容量
 */
@Service
class RecommendationCacheService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val eventBus: EventBus
) {
    
    private val logger = LoggerFactory.getLogger(RecommendationCacheService::class.java)
    
    companion object {
        private const val CACHE_KEY_PREFIX = "recommendation:"
        private const val USER_CACHE_KEY_PREFIX = "user_rec:"
        private const val ITEM_CACHE_KEY_PREFIX = "item_rec:"
        private const val METADATA_KEY_PREFIX = "rec_meta:"
        
        // 缓存过期时间
        private val DEFAULT_TTL = Duration.ofHours(24)
        private val HOT_USER_TTL = Duration.ofHours(6)
        private val COLD_USER_TTL = Duration.ofDays(7)
        
        // 扩展优化配置
        // TODO: 扩展优化 - 可配置的缓存策略
        // 建议将这些配置移到配置文件中，支持动态调整：
        // - 基于用户活跃度的动态TTL策略
        // - 基于推荐算法类型的差异化缓存时间
        // - 基于业务场景的缓存优先级设置
        
        // TODO: 扩展优化 - 缓存容量管理
        // 建议添加缓存容量限制和LRU淘汰策略：
        // - 设置最大缓存条目数量
        // - 实现智能淘汰算法（LRU + 访问频率）
        // - 添加缓存预热和清理机制
    }
    
    /**
     * 缓存用户推荐结果
     */
    suspend fun cacheUserRecommendations(
        userId: UserId,
        recommendations: List<RecommendationResult>,
        algorithm: String,
        ttl: Duration = DEFAULT_TTL
    ) {
        logger.debug("开始缓存用户推荐: userId={}, count={}, algorithm={}", 
            userId.value, recommendations.size, algorithm)
        
        val startTime = System.currentTimeMillis()
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = getUserRecommendationKey(userId, algorithm)
                val metadataKey = getMetadataKey(userId, algorithm)
                
                // 缓存推荐结果
                val cacheData = CachedRecommendation(
                    userId = userId,
                    recommendations = recommendations,
                    algorithm = algorithm,
                    cachedAt = Instant.now(),
                    ttl = ttl
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey, 
                    cacheData, 
                    ttl.toMillis(), 
                    TimeUnit.MILLISECONDS
                )
                
                // 缓存元数据
                val metadata = RecommendationMetadata(
                    userId = userId,
                    algorithm = algorithm,
                    count = recommendations.size,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    metadataKey,
                    metadata,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
                
                // 更新用户推荐索引
                updateUserRecommendationIndex(userId, algorithm)
            }
            
            val cacheTime = System.currentTimeMillis() - startTime
            
            // 发布缓存事件
            val event = RecommendationCachedEvent(
                userId = userId,
                algorithm = algorithm,
                recommendationCount = recommendations.size,
                cacheTimeMs = cacheTime,
                ttlSeconds = ttl.seconds
            )
            eventBus.publish(event)
            
            logger.info("用户推荐缓存完成: userId={}, count={}, cacheTime={}ms", 
                userId.value, recommendations.size, cacheTime)
            
        } catch (e: Exception) {
            logger.error("用户推荐缓存失败: userId={}", userId.value, e)
            throw RecommendationCacheException("推荐缓存失败: ${e.message}", e)
        }
    }
    
    /**
     * 获取用户推荐缓存
     */
    suspend fun getUserRecommendations(
        userId: UserId,
        algorithm: String
    ): CachedRecommendation? {
        logger.debug("获取用户推荐缓存: userId={}, algorithm={}", userId.value, algorithm)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = getUserRecommendationKey(userId, algorithm)
                val cachedData = redisTemplate.opsForValue().get(cacheKey) as? CachedRecommendation
                
                if (cachedData != null) {
                    logger.debug("命中用户推荐缓存: userId={}, count={}", 
                        userId.value, cachedData.recommendations.size)
                } else {
                    logger.debug("未命中用户推荐缓存: userId={}", userId.value)
                }
                
                cachedData
            }
        } catch (e: Exception) {
            logger.error("获取用户推荐缓存失败: userId={}", userId.value, e)
            null
        }
    }
    
    /**
     * 缓存物品推荐结果
     */
    suspend fun cacheItemRecommendations(
        itemId: String,
        itemType: String,
        recommendations: List<RecommendationResult>,
        ttl: Duration = DEFAULT_TTL
    ) {
        logger.debug("开始缓存物品推荐: itemId={}, count={}", itemId, recommendations.size)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = getItemRecommendationKey(itemId, itemType)
                
                val cacheData = CachedItemRecommendation(
                    itemId = itemId,
                    itemType = itemType,
                    recommendations = recommendations,
                    cachedAt = Instant.now(),
                    ttl = ttl
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cacheData,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
            
            logger.info("物品推荐缓存完成: itemId={}, count={}", itemId, recommendations.size)
            
        } catch (e: Exception) {
            logger.error("物品推荐缓存失败: itemId={}", itemId, e)
            throw RecommendationCacheException("物品推荐缓存失败: ${e.message}", e)
        }
    }
    
    /**
     * 获取物品推荐缓存
     */
    suspend fun getItemRecommendations(
        itemId: String,
        itemType: String
    ): CachedItemRecommendation? {
        logger.debug("获取物品推荐缓存: itemId={}, itemType={}", itemId, itemType)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = getItemRecommendationKey(itemId, itemType)
                redisTemplate.opsForValue().get(cacheKey) as? CachedItemRecommendation
            }
        } catch (e: Exception) {
            logger.error("获取物品推荐缓存失败: itemId={}", itemId, e)
            null
        }
    }
    
    /**
     * 失效用户推荐缓存
     */
    suspend fun invalidateUserRecommendations(
        userId: UserId,
        algorithm: String? = null
    ) {
        logger.debug("失效用户推荐缓存: userId={}, algorithm={}", userId.value, algorithm)
        
        try {
            withContext(Dispatchers.IO) {
                if (algorithm != null) {
                    // 失效特定算法的缓存
                    val cacheKey = getUserRecommendationKey(userId, algorithm)
                    val metadataKey = getMetadataKey(userId, algorithm)
                    
                    redisTemplate.delete(cacheKey)
                    redisTemplate.delete(metadataKey)
                } else {
                    // 失效用户所有推荐缓存
                    val pattern = "${USER_CACHE_KEY_PREFIX}${userId.value}:*"
                    val keys = redisTemplate.keys(pattern)
                    if (keys.isNotEmpty()) {
                        redisTemplate.delete(keys)
                    }
                    
                    val metadataPattern = "${METADATA_KEY_PREFIX}${userId.value}:*"
                    val metadataKeys = redisTemplate.keys(metadataPattern)
                    if (metadataKeys.isNotEmpty()) {
                        redisTemplate.delete(metadataKeys)
                    }
                }
            }
            
            logger.info("用户推荐缓存失效完成: userId={}, algorithm={}", userId.value, algorithm)
            
        } catch (e: Exception) {
            logger.error("用户推荐缓存失效失败: userId={}", userId.value, e)
        }
    }
    
    /**
     * 失效物品推荐缓存
     */
    suspend fun invalidateItemRecommendations(
        itemId: String,
        itemType: String
    ) {
        logger.debug("失效物品推荐缓存: itemId={}, itemType={}", itemId, itemType)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = getItemRecommendationKey(itemId, itemType)
                redisTemplate.delete(cacheKey)
            }
            
            logger.info("物品推荐缓存失效完成: itemId={}", itemId)
            
        } catch (e: Exception) {
            logger.error("物品推荐缓存失效失败: itemId={}", itemId, e)
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    suspend fun getCacheStatistics(): CacheStatistics {
        return try {
            withContext(Dispatchers.IO) {
                val userCacheKeys = redisTemplate.keys("${USER_CACHE_KEY_PREFIX}*")
                val itemCacheKeys = redisTemplate.keys("${ITEM_CACHE_KEY_PREFIX}*")
                
                CacheStatistics(
                    userCacheCount = userCacheKeys.size,
                    itemCacheCount = itemCacheKeys.size,
                    totalCacheCount = userCacheKeys.size + itemCacheKeys.size
                )
            }
        } catch (e: Exception) {
            logger.error("获取缓存统计信息失败", e)
            CacheStatistics(0, 0, 0)
        }
    }
    
    /**
     * 清理过期缓存
     */
    suspend fun cleanupExpiredCache() {
        logger.debug("开始清理过期缓存")
        
        try {
            withContext(Dispatchers.IO) {
                // Redis会自动清理过期的key，这里主要是清理一些元数据
                val metadataPattern = "${METADATA_KEY_PREFIX}*"
                val metadataKeys = redisTemplate.keys(metadataPattern)
                
                var cleanedCount = 0
                metadataKeys.forEach { key ->
                    val metadata = redisTemplate.opsForValue().get(key) as? RecommendationMetadata
                    if (metadata != null && metadata.expiresAt.isBefore(Instant.now())) {
                        redisTemplate.delete(key)
                        cleanedCount++
                    }
                }
                
                logger.info("过期缓存清理完成: cleanedCount={}", cleanedCount)
            }
        } catch (e: Exception) {
            logger.error("清理过期缓存失败", e)
        }
    }
    
    /**
     * 更新用户推荐索引
     */
    private fun updateUserRecommendationIndex(userId: UserId, algorithm: String) {
        try {
            val indexKey = "user_rec_index:${userId.value}"
            redisTemplate.opsForSet().add(indexKey, algorithm)
            redisTemplate.expire(indexKey, DEFAULT_TTL.toMillis(), TimeUnit.MILLISECONDS)
        } catch (e: Exception) {
            logger.warn("更新用户推荐索引失败: userId={}", userId.value, e)
        }
    }
    
    /**
     * 生成用户推荐缓存键
     */
    private fun getUserRecommendationKey(userId: UserId, algorithm: String): String {
        return "${USER_CACHE_KEY_PREFIX}${userId.value}:${algorithm}"
    }
    
    /**
     * 生成物品推荐缓存键
     */
    private fun getItemRecommendationKey(itemId: String, itemType: String): String {
        return "${ITEM_CACHE_KEY_PREFIX}${itemType}:${itemId}"
    }
    
    /**
     * 生成元数据缓存键
     */
    private fun getMetadataKey(userId: UserId, algorithm: String): String {
        return "${METADATA_KEY_PREFIX}${userId.value}:${algorithm}"
    }
}

/**
 * 缓存的推荐结果
 */
data class CachedRecommendation(
    val userId: UserId,
    val recommendations: List<RecommendationResult>,
    val algorithm: String,
    val cachedAt: Instant,
    val ttl: Duration
)

/**
 * 缓存的物品推荐结果
 */
data class CachedItemRecommendation(
    val itemId: String,
    val itemType: String,
    val recommendations: List<RecommendationResult>,
    val cachedAt: Instant,
    val ttl: Duration
)

/**
 * 推荐元数据
 */
data class RecommendationMetadata(
    val userId: UserId,
    val algorithm: String,
    val count: Int,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 缓存统计信息
 */
data class CacheStatistics(
    val userCacheCount: Int,
    val itemCacheCount: Int,
    val totalCacheCount: Int
)

/**
 * 推荐缓存异常
 */
class RecommendationCacheException(message: String, cause: Throwable? = null) : 
    RuntimeException(message, cause)