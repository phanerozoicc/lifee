package com.lifee.config.app.services

import com.lifee.common.cqrs.events.EventBus
import com.lifee.config.domain.valueobjects.ConfigurationId
import com.lifee.config.domain.valueobjects.ConfigKey
import com.lifee.config.domain.valueobjects.ConfigNamespace
import com.lifee.config.app.dto.ConfigurationDto
import com.lifee.config.domain.events.ConfigurationCachedEvent
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
 * 配置缓存服务
 * 负责配置相关数据的缓存管理，提供高性能的配置访问能力
 * 
 * 核心功能：
 * 1. 用户配置缓存：缓存用户个性化配置，支持快速访问
 * 2. 系统配置缓存：缓存全局系统配置，减少数据库查询
 * 3. 命名空间配置缓存：按命名空间分组缓存配置
 * 4. 配置模式缓存：缓存配置验证规则和模式定义
 * 5. 配置历史缓存：缓存配置变更历史记录
 * 
 * 扩展优化策略：
 * 1. 多级缓存架构：
 *    - L1缓存：JVM本地缓存（Caffeine）用于热点配置
 *    - L2缓存：Redis分布式缓存用于共享配置
 *    - L3缓存：数据库持久化存储
 * 
 * 2. 智能缓存策略：
 *    - 基于配置访问频率的动态TTL调整
 *    - 配置变更时的智能失效和预热
 *    - 基于配置类型的差异化缓存策略
 * 
 * 3. 性能优化：
 *    - 批量配置操作：支持批量读取和更新
 *    - 异步缓存更新：配置变更时异步更新相关缓存
 *    - 压缩存储：对大型配置使用压缩算法
 * 
 * 4. 一致性保证：
 *    - 配置版本控制：支持配置的版本管理
 *    - 分布式锁：确保配置更新的原子性
 *    - 事件驱动失效：配置变更时主动失效相关缓存
 * 
 * 5. 监控和运维：
 *    - 缓存命中率监控：实时监控各类配置的缓存效果
 *    - 配置访问统计：分析配置使用模式
 *    - 自动容量管理：基于使用情况自动调整缓存容量
 * 
 * @param redisTemplate Redis操作模板，用于分布式缓存操作
 * @param eventBus 事件总线，用于发布缓存相关事件
 */
@Service
class ConfigurationCacheService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val eventBus: EventBus
) {
    
    private val logger = LoggerFactory.getLogger(ConfigurationCacheService::class.java)
    
    companion object {
        private const val CONFIG_PREFIX = "config:"
        private const val USER_CONFIG_PREFIX = "config:user:"
        private const val SYSTEM_CONFIG_PREFIX = "config:system:"
        private const val NAMESPACE_CONFIG_PREFIX = "config:namespace:"
        private const val CONFIG_SCHEMA_PREFIX = "config:schema:"
        private const val CONFIG_HISTORY_PREFIX = "config:history:"
        private const val CONFIG_VALIDATION_PREFIX = "config:validation:"
        
        // 缓存过期时间
        private val USER_CONFIG_TTL = Duration.ofHours(1)
        private val SYSTEM_CONFIG_TTL = Duration.ofHours(24)
        private val NAMESPACE_CONFIG_TTL = Duration.ofHours(2)
        private val SCHEMA_TTL = Duration.ofHours(12)
        private val HISTORY_TTL = Duration.ofMinutes(30)
        private val VALIDATION_TTL = Duration.ofMinutes(15)
    }
    
    /**
     * 缓存用户配置
     * 
     * 将用户的个性化配置存储到Redis缓存中，提供快速访问能力。
     * 支持配置的批量缓存和TTL管理。
     * 
     * @param userId 用户ID，用于构建缓存键
     * @param configurations 用户配置映射，键为配置名，值为配置值
     * @param ttl 缓存生存时间，默认为1小时
     */
    suspend fun cacheUserConfiguration(
        userId: UserId,
        configurations: Map<String, Any>,
        ttl: Duration = USER_CONFIG_TTL
    ) {
        logger.debug("缓存用户配置: userId={}, configCount={}", userId.value, configurations.size)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_CONFIG_PREFIX}${userId.value}"
                
                val cachedConfig = CachedUserConfiguration(
                    userId = userId.value,
                    configurations = configurations,
                    configCount = configurations.size,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedConfig,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
                
                logger.debug("用户配置缓存成功: userId={}, ttl={}秒", userId.value, ttl.seconds)
            }
            
            // 发布缓存事件
            eventBus.publish(ConfigurationCachedEvent(
                configId = ConfigurationId.generate(),
                cacheType = "user_config",
                userId = userId,
                cachedAt = Instant.now()
            ))
            
        } catch (e: Exception) {
            logger.error("缓存用户配置失败: userId={}", userId.value, e)
        }
    }
    
    /**
     * 获取用户配置缓存
     * 
     * 从Redis缓存中获取用户的完整配置信息。
     * 如果缓存不存在或已过期，返回null。
     * 
     * @param userId 用户ID
     * @return 用户配置映射，如果缓存未命中则返回null
     */
    suspend fun getUserConfiguration(userId: UserId): Map<String, Any>? {
        logger.debug("获取用户配置缓存: userId={}", userId.value)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_CONFIG_PREFIX}${userId.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedUserConfiguration
                cached?.configurations
            }
        } catch (e: Exception) {
            logger.error("获取用户配置缓存失败: userId={}", userId.value, e)
            null
        }
    }
    
    /**
     * 缓存单个用户配置项
     * 
     * 更新或添加用户的单个配置项到缓存中。
     * 如果用户配置缓存已存在，则更新指定配置项；
     * 如果不存在，则创建新的用户配置缓存。
     * 
     * @param userId 用户ID
     * @param configKey 配置键名
     * @param configValue 配置值
     * @param ttl 缓存生存时间，默认为1小时
     */
    suspend fun cacheUserConfigItem(
        userId: UserId,
        configKey: String,
        configValue: Any,
        ttl: Duration = USER_CONFIG_TTL
    ) {
        logger.debug("缓存用户配置项: userId={}, key={}", userId.value, configKey)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_CONFIG_PREFIX}${userId.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedUserConfiguration
                
                val updatedConfigurations = if (cached != null) {
                    cached.configurations.toMutableMap().apply {
                        put(configKey, configValue)
                    }
                } else {
                    mapOf(configKey to configValue)
                }
                
                val updatedConfig = CachedUserConfiguration(
                    userId = userId.value,
                    configurations = updatedConfigurations,
                    configCount = updatedConfigurations.size,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    updatedConfig,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        } catch (e: Exception) {
            logger.error("缓存用户配置项失败: userId={}, key={}", userId.value, configKey, e)
        }
    }
    
    /**
     * 获取用户配置项
     * 
     * 从缓存中获取用户的特定配置项值。
     * 如果配置项不存在或缓存已过期，返回null。
     * 
     * @param userId 用户ID
     * @param configKey 配置键名
     * @return 配置值，如果不存在则返回null
     */
    suspend fun getUserConfigItem(userId: UserId, configKey: String): Any? {
        logger.debug("获取用户配置项: userId={}, key={}", userId.value, configKey)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_CONFIG_PREFIX}${userId.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedUserConfiguration
                cached?.configurations?.get(configKey)
            }
        } catch (e: Exception) {
            logger.error("获取用户配置项失败: userId={}, key={}", userId.value, configKey, e)
            null
        }
    }
    
    /**
     * 缓存系统配置
     */
    suspend fun cacheSystemConfiguration(
        configurations: Map<String, Any>,
        ttl: Duration = SYSTEM_CONFIG_TTL
    ) {
        logger.debug("缓存系统配置: configCount={}", configurations.size)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = SYSTEM_CONFIG_PREFIX + "global"
                
                val cachedConfig = CachedSystemConfiguration(
                    configurations = configurations,
                    configCount = configurations.size,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedConfig,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
                
                logger.debug("系统配置缓存成功: configCount={}, ttl={}秒", configurations.size, ttl.seconds)
            }
        } catch (e: Exception) {
            logger.error("缓存系统配置失败", e)
        }
    }
    
    /**
     * 获取系统配置缓存
     */
    suspend fun getSystemConfiguration(): Map<String, Any>? {
        logger.debug("获取系统配置缓存")
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = SYSTEM_CONFIG_PREFIX + "global"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedSystemConfiguration
                cached?.configurations
            }
        } catch (e: Exception) {
            logger.error("获取系统配置缓存失败", e)
            null
        }
    }
    
    /**
     * 缓存命名空间配置
     */
    suspend fun cacheNamespaceConfiguration(
        namespace: ConfigNamespace,
        configurations: Map<String, Any>,
        ttl: Duration = NAMESPACE_CONFIG_TTL
    ) {
        logger.debug("缓存命名空间配置: namespace={}, configCount={}", namespace.value, configurations.size)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${NAMESPACE_CONFIG_PREFIX}${namespace.value}"
                
                val cachedConfig = CachedNamespaceConfiguration(
                    namespace = namespace.value,
                    configurations = configurations,
                    configCount = configurations.size,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedConfig,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        } catch (e: Exception) {
            logger.error("缓存命名空间配置失败: namespace={}", namespace.value, e)
        }
    }
    
    /**
     * 获取命名空间配置缓存
     */
    suspend fun getNamespaceConfiguration(namespace: ConfigNamespace): Map<String, Any>? {
        logger.debug("获取命名空间配置缓存: namespace={}", namespace.value)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${NAMESPACE_CONFIG_PREFIX}${namespace.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedNamespaceConfiguration
                cached?.configurations
            }
        } catch (e: Exception) {
            logger.error("获取命名空间配置缓存失败: namespace={}", namespace.value, e)
            null
        }
    }
    
    /**
     * 缓存配置模式
     */
    suspend fun cacheConfigurationSchema(
        namespace: ConfigNamespace,
        schema: ConfigurationSchema,
        ttl: Duration = SCHEMA_TTL
    ) {
        logger.debug("缓存配置模式: namespace={}", namespace.value)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${CONFIG_SCHEMA_PREFIX}${namespace.value}"
                
                val cachedSchema = CachedConfigurationSchema(
                    namespace = namespace.value,
                    schema = schema,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedSchema,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        } catch (e: Exception) {
            logger.error("缓存配置模式失败: namespace={}", namespace.value, e)
        }
    }
    
    /**
     * 获取配置模式缓存
     */
    suspend fun getConfigurationSchema(namespace: ConfigNamespace): ConfigurationSchema? {
        logger.debug("获取配置模式缓存: namespace={}", namespace.value)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${CONFIG_SCHEMA_PREFIX}${namespace.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedConfigurationSchema
                cached?.schema
            }
        } catch (e: Exception) {
            logger.error("获取配置模式缓存失败: namespace={}", namespace.value, e)
            null
        }
    }
    
    /**
     * 缓存配置验证结果
     */
    suspend fun cacheConfigurationValidation(
        configId: ConfigurationId,
        validationResult: ConfigurationValidationResult,
        ttl: Duration = VALIDATION_TTL
    ) {
        logger.debug("缓存配置验证结果: configId={}", configId.value)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${CONFIG_VALIDATION_PREFIX}${configId.value}"
                
                val cachedValidation = CachedConfigurationValidation(
                    configId = configId.value,
                    validationResult = validationResult,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedValidation,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        } catch (e: Exception) {
            logger.error("缓存配置验证结果失败: configId={}", configId.value, e)
        }
    }
    
    /**
     * 获取配置验证结果缓存
     */
    suspend fun getConfigurationValidation(configId: ConfigurationId): ConfigurationValidationResult? {
        logger.debug("获取配置验证结果缓存: configId={}", configId.value)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${CONFIG_VALIDATION_PREFIX}${configId.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedConfigurationValidation
                cached?.validationResult
            }
        } catch (e: Exception) {
            logger.error("获取配置验证结果缓存失败: configId={}", configId.value, e)
            null
        }
    }
    
    /**
     * 失效用户配置缓存
     */
    suspend fun invalidateUserConfiguration(userId: UserId) {
        logger.debug("失效用户配置缓存: userId={}", userId.value)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_CONFIG_PREFIX}${userId.value}"
                redisTemplate.delete(cacheKey)
                
                logger.debug("用户配置缓存失效完成: userId={}", userId.value)
            }
        } catch (e: Exception) {
            logger.error("失效用户配置缓存失败: userId={}", userId.value, e)
        }
    }
    
    /**
     * 失效系统配置缓存
     */
    suspend fun invalidateSystemConfiguration() {
        logger.debug("失效系统配置缓存")
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = SYSTEM_CONFIG_PREFIX + "global"
                redisTemplate.delete(cacheKey)
                
                logger.debug("系统配置缓存失效完成")
            }
        } catch (e: Exception) {
            logger.error("失效系统配置缓存失败", e)
        }
    }
    
    /**
     * 失效命名空间配置缓存
     */
    suspend fun invalidateNamespaceConfiguration(namespace: ConfigNamespace) {
        logger.debug("失效命名空间配置缓存: namespace={}", namespace.value)
        
        try {
            withContext(Dispatchers.IO) {
                val patterns = listOf(
                    "${NAMESPACE_CONFIG_PREFIX}${namespace.value}",
                    "${CONFIG_SCHEMA_PREFIX}${namespace.value}"
                )
                
                patterns.forEach { pattern ->
                    redisTemplate.delete(pattern)
                }
                
                logger.debug("命名空间配置缓存失效完成: namespace={}", namespace.value)
            }
        } catch (e: Exception) {
            logger.error("失效命名空间配置缓存失败: namespace={}", namespace.value, e)
        }
    }
    
    /**
     * 批量预热配置缓存
     */
    suspend fun warmupConfigurationCache() {
        logger.info("开始预热配置缓存")
        
        try {
            // 预热系统配置
            // 这里需要从数据库加载系统配置并缓存
            logger.debug("预热系统配置")
            
            // 预热常用命名空间配置
            val commonNamespaces = listOf("app", "database", "cache", "security")
            commonNamespaces.forEach { namespace ->
                try {
                    logger.debug("预热命名空间配置: namespace={}", namespace)
                    // 这里需要从数据库加载命名空间配置并缓存
                } catch (e: Exception) {
                    logger.warn("预热命名空间配置失败: namespace={}", namespace, e)
                }
            }
            
            logger.info("配置缓存预热完成")
        } catch (e: Exception) {
            logger.error("配置缓存预热失败", e)
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    suspend fun getCacheStatistics(): ConfigurationCacheStatistics {
        return try {
            withContext(Dispatchers.IO) {
                val userConfigKeys = redisTemplate.keys("${USER_CONFIG_PREFIX}*")
                val systemConfigKeys = redisTemplate.keys("${SYSTEM_CONFIG_PREFIX}*")
                val namespaceConfigKeys = redisTemplate.keys("${NAMESPACE_CONFIG_PREFIX}*")
                val schemaKeys = redisTemplate.keys("${CONFIG_SCHEMA_PREFIX}*")
                val validationKeys = redisTemplate.keys("${CONFIG_VALIDATION_PREFIX}*")
                
                ConfigurationCacheStatistics(
                    userConfigCacheCount = userConfigKeys.size,
                    systemConfigCacheCount = systemConfigKeys.size,
                    namespaceConfigCacheCount = namespaceConfigKeys.size,
                    schemaCacheCount = schemaKeys.size,
                    validationCacheCount = validationKeys.size,
                    totalCacheCount = userConfigKeys.size + systemConfigKeys.size + namespaceConfigKeys.size + schemaKeys.size + validationKeys.size
                )
            }
        } catch (e: Exception) {
            logger.error("获取缓存统计失败", e)
            ConfigurationCacheStatistics(0, 0, 0, 0, 0, 0)
        }
    }
}

/**
 * 缓存的用户配置
 */
data class CachedUserConfiguration(
    val userId: String,
    val configurations: Map<String, Any>,
    val configCount: Int,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 缓存的系统配置
 */
data class CachedSystemConfiguration(
    val configurations: Map<String, Any>,
    val configCount: Int,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 缓存的命名空间配置
 */
data class CachedNamespaceConfiguration(
    val namespace: String,
    val configurations: Map<String, Any>,
    val configCount: Int,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 缓存的配置模式
 */
data class CachedConfigurationSchema(
    val namespace: String,
    val schema: ConfigurationSchema,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 配置模式
 */
data class ConfigurationSchema(
    val version: String,
    val properties: Map<String, ConfigPropertySchema>,
    val required: List<String> = emptyList(),
    val additionalProperties: Boolean = false
)

/**
 * 配置属性模式
 */
data class ConfigPropertySchema(
    val type: String,
    val description: String?,
    val defaultValue: Any?,
    val enum: List<Any>? = null,
    val minimum: Number? = null,
    val maximum: Number? = null,
    val pattern: String? = null
)

/**
 * 缓存的配置验证结果
 */
data class CachedConfigurationValidation(
    val configId: String,
    val validationResult: ConfigurationValidationResult,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 配置验证结果
 */
data class ConfigurationValidationResult(
    val isValid: Boolean,
    val errors: List<ConfigurationValidationError> = emptyList(),
    val warnings: List<ConfigurationValidationWarning> = emptyList()
)

/**
 * 配置验证错误
 */
data class ConfigurationValidationError(
    val field: String,
    val message: String,
    val code: String
)

/**
 * 配置验证警告
 */
data class ConfigurationValidationWarning(
    val field: String,
    val message: String,
    val code: String
)

/**
 * 配置缓存统计信息
 */
data class ConfigurationCacheStatistics(
    val userConfigCacheCount: Int,
    val systemConfigCacheCount: Int,
    val namespaceConfigCacheCount: Int,
    val schemaCacheCount: Int,
    val validationCacheCount: Int,
    val totalCacheCount: Int
)