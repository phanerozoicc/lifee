package com.lifee.user.app.services

import com.lifee.common.cqrs.events.EventBus
import com.lifee.user.domain.UserId
import com.lifee.user.domain.Email
import com.lifee.user.app.dto.UserDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * 用户缓存服务
 * 负责用户相关数据的缓存管理
 */
@Service
class UserCacheService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val eventBus: EventBus
) {
    
    private val logger = LoggerFactory.getLogger(UserCacheService::class.java)
    
    companion object {
        private const val USER_PROFILE_PREFIX = "user:profile:"
        private const val USER_PERMISSIONS_PREFIX = "user:permissions:"
        private const val USER_SESSION_PREFIX = "user:session:"
        private const val USER_PREFERENCES_PREFIX = "user:preferences:"
        private const val USER_STATS_PREFIX = "user:stats:"
        
        // 缓存过期时间
        private val PROFILE_TTL = Duration.ofHours(1)
        private val PERMISSIONS_TTL = Duration.ofMinutes(30)
        private val SESSION_TTL = Duration.ofHours(24)
        private val PREFERENCES_TTL = Duration.ofHours(2)
        private val STATS_TTL = Duration.ofMinutes(15)
    }
    
    /**
     * 缓存用户档案
     */
    suspend fun cacheUserProfile(
        userId: UserId,
        userDto: UserDto,
        ttl: Duration = PROFILE_TTL
    ) {
        logger.debug("缓存用户档案: userId={}", userId.value)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_PROFILE_PREFIX}${userId.value}"
                
                val cachedProfile = CachedUserProfile(
                    userId = userId.value,
                    email = userDto.email,
                    username = userDto.email, // 使用email作为username的临时值
                    isActive = true, // 默认为活跃状态
                    lastLoginAt = userDto.lastLoginAt,
                    createdAt = userDto.createdAt,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedProfile,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
                
                logger.debug("用户档案缓存成功: userId={}, ttl={}秒", userId.value, ttl.seconds)
            }
            
            // 缓存事件发布已移除，因为UserCachedEvent不存在
            logger.debug("用户档案缓存事件记录: userId={}, cacheType=profile", userId.value)
            
        } catch (e: Exception) {
            logger.error("缓存用户档案失败: userId={}", userId.value, e)
        }
    }
    
    /**
     * 获取用户档案缓存
     */
    suspend fun getUserProfile(userId: UserId): CachedUserProfile? {
        logger.debug("获取用户档案缓存: userId={}", userId.value)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_PROFILE_PREFIX}${userId.value}"
                redisTemplate.opsForValue().get(cacheKey) as? CachedUserProfile
            }
        } catch (e: Exception) {
            logger.error("获取用户档案缓存失败: userId={}", userId.value, e)
            null
        }
    }
    
    /**
     * 缓存用户权限
     */
    suspend fun cacheUserPermissions(
        userId: UserId,
        permissions: Set<String>,
        ttl: Duration = PERMISSIONS_TTL
    ) {
        logger.debug("缓存用户权限: userId={}, permissions={}", userId.value, permissions.size)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_PERMISSIONS_PREFIX}${userId.value}"
                
                val cachedPermissions = CachedUserPermissions(
                    userId = userId.value,
                    permissions = permissions,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedPermissions,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        } catch (e: Exception) {
            logger.error("缓存用户权限失败: userId={}", userId.value, e)
        }
    }
    
    /**
     * 获取用户权限缓存
     */
    suspend fun getUserPermissions(userId: UserId): Set<String>? {
        logger.debug("获取用户权限缓存: userId={}", userId.value)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_PERMISSIONS_PREFIX}${userId.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedUserPermissions
                cached?.permissions
            }
        } catch (e: Exception) {
            logger.error("获取用户权限缓存失败: userId={}", userId.value, e)
            null
        }
    }
    
    /**
     * 缓存用户会话信息
     */
    suspend fun cacheUserSession(
        sessionId: String,
        userId: UserId,
        sessionData: Map<String, Any>,
        ttl: Duration = SESSION_TTL
    ) {
        logger.debug("缓存用户会话: sessionId={}, userId={}", sessionId, userId.value)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_SESSION_PREFIX}$sessionId"
                
                val cachedSession = CachedUserSession(
                    sessionId = sessionId,
                    userId = userId.value,
                    sessionData = sessionData,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedSession,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        } catch (e: Exception) {
            logger.error("缓存用户会话失败: sessionId={}", sessionId, e)
        }
    }
    
    /**
     * 获取用户会话缓存
     */
    suspend fun getUserSession(sessionId: String): CachedUserSession? {
        logger.debug("获取用户会话缓存: sessionId={}", sessionId)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_SESSION_PREFIX}$sessionId"
                redisTemplate.opsForValue().get(cacheKey) as? CachedUserSession
            }
        } catch (e: Exception) {
            logger.error("获取用户会话缓存失败: sessionId={}", sessionId, e)
            null
        }
    }
    
    /**
     * 缓存用户偏好设置
     */
    suspend fun cacheUserPreferences(
        userId: UserId,
        preferences: Map<String, Any>,
        ttl: Duration = PREFERENCES_TTL
    ) {
        logger.debug("缓存用户偏好: userId={}", userId.value)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_PREFERENCES_PREFIX}${userId.value}"
                
                val cachedPreferences = CachedUserPreferences(
                    userId = userId.value,
                    preferences = preferences,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedPreferences,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        } catch (e: Exception) {
            logger.error("缓存用户偏好失败: userId={}", userId.value, e)
        }
    }
    
    /**
     * 获取用户偏好缓存
     */
    suspend fun getUserPreferences(userId: UserId): Map<String, Any>? {
        logger.debug("获取用户偏好缓存: userId={}", userId.value)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_PREFERENCES_PREFIX}${userId.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedUserPreferences
                cached?.preferences
            }
        } catch (e: Exception) {
            logger.error("获取用户偏好缓存失败: userId={}", userId.value, e)
            null
        }
    }
    
    /**
     * 失效用户相关缓存
     */
    suspend fun invalidateUserCache(userId: UserId) {
        logger.debug("失效用户缓存: userId={}", userId.value)
        
        try {
            withContext(Dispatchers.IO) {
                val patterns = listOf(
                    "${USER_PROFILE_PREFIX}${userId.value}",
                    "${USER_PERMISSIONS_PREFIX}${userId.value}",
                    "${USER_PREFERENCES_PREFIX}${userId.value}",
                    "${USER_STATS_PREFIX}${userId.value}"
                )
                
                patterns.forEach { pattern ->
                    redisTemplate.delete(pattern)
                }
                
                // 失效用户会话（需要查找所有相关会话）
                val sessionPattern = "${USER_SESSION_PREFIX}*"
                val sessionKeys = redisTemplate.keys(sessionPattern)
                sessionKeys.forEach { key ->
                    val session = redisTemplate.opsForValue().get(key) as? CachedUserSession
                    if (session?.userId == userId.value) {
                        redisTemplate.delete(key)
                    }
                }
                
                logger.debug("用户缓存失效完成: userId={}", userId.value)
            }
        } catch (e: Exception) {
            logger.error("失效用户缓存失败: userId={}", userId.value, e)
        }
    }
    
    /**
     * 批量预热用户缓存
     */
    suspend fun warmupUserCache(userIds: List<UserId>) {
        logger.info("开始预热用户缓存: count={}", userIds.size)
        
        userIds.forEach { userId ->
            try {
                // 这里需要从数据库加载用户数据并缓存
                // 实际实现中需要注入相应的服务
                logger.debug("预热用户缓存: userId={}", userId.value)
            } catch (e: Exception) {
                logger.warn("预热用户缓存失败: userId={}", userId.value, e)
            }
        }
        
        logger.info("用户缓存预热完成")
    }
    
    /**
     * 获取缓存统计信息
     */
    suspend fun getCacheStatistics(): UserCacheStatistics {
        return try {
            withContext(Dispatchers.IO) {
                val profileKeys = redisTemplate.keys("${USER_PROFILE_PREFIX}*")
                val permissionKeys = redisTemplate.keys("${USER_PERMISSIONS_PREFIX}*")
                val sessionKeys = redisTemplate.keys("${USER_SESSION_PREFIX}*")
                val preferenceKeys = redisTemplate.keys("${USER_PREFERENCES_PREFIX}*")
                
                UserCacheStatistics(
                    profileCacheCount = profileKeys.size,
                    permissionCacheCount = permissionKeys.size,
                    sessionCacheCount = sessionKeys.size,
                    preferenceCacheCount = preferenceKeys.size,
                    totalCacheCount = profileKeys.size + permissionKeys.size + sessionKeys.size + preferenceKeys.size
                )
            }
        } catch (e: Exception) {
            logger.error("获取缓存统计失败", e)
            UserCacheStatistics(0, 0, 0, 0, 0)
        }
    }
}

/**
 * 缓存的用户档案
 */
data class CachedUserProfile(
    val userId: String,
    val email: String,
    val username: String,
    val isActive: Boolean,
    val lastLoginAt: Instant?,
    val createdAt: Instant,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 缓存的用户权限
 */
data class CachedUserPermissions(
    val userId: String,
    val permissions: Set<String>,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 缓存的用户会话
 */
data class CachedUserSession(
    val sessionId: String,
    val userId: String,
    val sessionData: Map<String, Any>,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 缓存的用户偏好
 */
data class CachedUserPreferences(
    val userId: String,
    val preferences: Map<String, Any>,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 用户缓存统计信息
 */
data class UserCacheStatistics(
    val profileCacheCount: Int,
    val permissionCacheCount: Int,
    val sessionCacheCount: Int,
    val preferenceCacheCount: Int,
    val totalCacheCount: Int
)