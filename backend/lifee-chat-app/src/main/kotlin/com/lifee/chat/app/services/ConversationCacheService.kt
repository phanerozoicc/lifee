package com.lifee.chat.app.services

import com.lifee.common.cqrs.events.EventBus
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.chat.domain.valueobjects.MessageId
import com.lifee.chat.app.application.dtos.ConversationDto
import com.lifee.chat.app.application.dtos.MessageDto
// import com.lifee.chat.domain.events.ConversationCachedEvent
import com.lifee.common.domain.valueobjects.UserId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.concurrent.TimeUnit

/**
 * 对话缓存服务
 * 负责对话相关数据的缓存管理
 */
@Service
class ConversationCacheService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val eventBus: EventBus
) {
    
    private val logger = LoggerFactory.getLogger(ConversationCacheService::class.java)
    
    companion object {
        private const val CONVERSATION_PREFIX = "chat:conversation:"
        private const val CONVERSATION_HISTORY_PREFIX = "chat:history:"
        private const val CONVERSATION_CONTEXT_PREFIX = "chat:context:"
        private const val USER_CONVERSATIONS_PREFIX = "chat:user_conversations:"
        private const val CONVERSATION_STATS_PREFIX = "chat:stats:"
        private const val MESSAGE_PREFIX = "chat:message:"
        private const val RECENT_MESSAGES_PREFIX = "chat:recent:"
        
        // 缓存过期时间
        private val CONVERSATION_TTL = Duration.ofHours(2)
        private val HISTORY_TTL = Duration.ofHours(1)
        private val CONTEXT_TTL = Duration.ofMinutes(30)
        private val USER_CONVERSATIONS_TTL = Duration.ofMinutes(30)
        private val STATS_TTL = Duration.ofMinutes(15)
        private val MESSAGE_TTL = Duration.ofHours(1)
        private val RECENT_MESSAGES_TTL = Duration.ofMinutes(30)
    }
    
    /**
     * 缓存对话信息
     */
    suspend fun cacheConversation(
        conversationId: ConversationId,
        conversationDto: ConversationDto,
        ttl: Duration = CONVERSATION_TTL
    ) {
        logger.debug("缓存对话信息: conversationId={}", conversationId.value)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${CONVERSATION_PREFIX}${conversationId.value}"
                
                val cachedConversation = CachedConversation(
                    conversationId = conversationId.value.toString(),
                    title = conversationDto.title,
                    userId = conversationDto.userId.toString(),
                    knowledgeBaseId = null,
                    modelName = "gpt-3.5-turbo",
                    modelConfig = emptyMap(),
                    messageCount = conversationDto.messageCount,
                    createdAt = conversationDto.createdAt,
                    updatedAt = conversationDto.updatedAt,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedConversation,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
                
                logger.debug("对话信息缓存成功: conversationId={}, ttl={}秒", conversationId.value, ttl.seconds)
            }
            
            // 发布缓存事件
            // eventBus.publish(ConversationCachedEvent(
            //     conversationId = conversationId,
            //     cacheType = "conversation",
            //     cachedAt = Instant.now()
            // ))
            
        } catch (e: Exception) {
            logger.error("缓存对话信息失败: conversationId={}", conversationId.value, e)
        }
    }
    
    /**
     * 获取对话信息缓存
     */
    suspend fun getConversation(conversationId: ConversationId): CachedConversation? {
        logger.debug("获取对话信息缓存: conversationId={}", conversationId.value)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${CONVERSATION_PREFIX}${conversationId.value}"
                redisTemplate.opsForValue().get(cacheKey) as? CachedConversation
            }
        } catch (e: Exception) {
            logger.error("获取对话信息缓存失败: conversationId={}", conversationId.value, e)
            null
        }
    }
    
    /**
     * 缓存对话历史
     */
    suspend fun cacheConversationHistory(
        conversationId: ConversationId,
        messages: List<MessageDto>,
        ttl: Duration = HISTORY_TTL
    ) {
        logger.debug("缓存对话历史: conversationId={}, messageCount={}", conversationId.value, messages.size)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${CONVERSATION_HISTORY_PREFIX}${conversationId.value}"
                
                val cachedHistory = CachedConversationHistory(
                    conversationId = conversationId.value.toString(),
                    messages = messages.map { msg ->
                        CachedMessage(
                            messageId = msg.id.toString(),
                            content = msg.content,
                            role = msg.type,
                            type = msg.type,
                            userId = msg.userId.toString(),
                            metadata = emptyMap(),
                            createdAt = msg.createdAt
                        )
                    },
                    totalMessages = messages.size,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedHistory,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        } catch (e: Exception) {
            logger.error("缓存对话历史失败: conversationId={}", conversationId.value, e)
        }
    }
    
    /**
     * 获取对话历史缓存
     */
    suspend fun getConversationHistory(conversationId: ConversationId): List<CachedMessage>? {
        logger.debug("获取对话历史缓存: conversationId={}", conversationId.value)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${CONVERSATION_HISTORY_PREFIX}${conversationId.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedConversationHistory
                cached?.messages
            }
        } catch (e: Exception) {
            logger.error("获取对话历史缓存失败: conversationId={}", conversationId.value, e)
            null
        }
    }
    
    /**
     * 添加消息到对话历史缓存
     */
    suspend fun addMessageToHistory(
        conversationId: ConversationId,
        message: MessageDto
    ) {
        logger.debug("添加消息到对话历史缓存: conversationId={}, messageId={}", 
            conversationId.value, message.id)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${CONVERSATION_HISTORY_PREFIX}${conversationId.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedConversationHistory
                
                if (cached != null) {
                    val newMessage = CachedMessage(
                        messageId = message.id.toString(),
                        content = message.content,
                        role = message.type,
                        type = message.type,
                        userId = message.userId.toString(),
                        metadata = emptyMap(),
                        createdAt = message.createdAt
                    )
                    
                    val updatedMessages = cached.messages + newMessage
                    val maxMessages = 100 // 限制历史消息数量
                    val finalMessages = if (updatedMessages.size > maxMessages) {
                        updatedMessages.takeLast(maxMessages)
                    } else {
                        updatedMessages
                    }
                    
                    val updatedHistory = cached.copy(
                        messages = finalMessages,
                        totalMessages = finalMessages.size,
                        cachedAt = Instant.now()
                    )
                    
                    redisTemplate.opsForValue().set(
                        cacheKey,
                        updatedHistory,
                        HISTORY_TTL.toMillis(),
                        TimeUnit.MILLISECONDS
                    )
                }
            }
        } catch (e: Exception) {
            logger.error("添加消息到对话历史缓存失败: conversationId={}", conversationId.value, e)
        }
    }
    
    /**
     * 缓存对话上下文
     */
    suspend fun cacheConversationContext(
        conversationId: ConversationId,
        context: ConversationContext,
        ttl: Duration = CONTEXT_TTL
    ) {
        logger.debug("缓存对话上下文: conversationId={}", conversationId.value)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${CONVERSATION_CONTEXT_PREFIX}${conversationId.value}"
                
                val cachedContext = CachedConversationContext(
                    conversationId = conversationId.value.toString(),
                    context = context,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedContext,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        } catch (e: Exception) {
            logger.error("缓存对话上下文失败: conversationId={}", conversationId.value, e)
        }
    }
    
    /**
     * 获取对话上下文缓存
     */
    suspend fun getConversationContext(conversationId: ConversationId): ConversationContext? {
        logger.debug("获取对话上下文缓存: conversationId={}", conversationId.value)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${CONVERSATION_CONTEXT_PREFIX}${conversationId.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedConversationContext
                cached?.context
            }
        } catch (e: Exception) {
            logger.error("获取对话上下文缓存失败: conversationId={}", conversationId.value, e)
            null
        }
    }
    
    /**
     * 缓存用户对话列表
     */
    suspend fun cacheUserConversations(
        userId: UserId,
        conversations: List<ConversationDto>,
        ttl: Duration = USER_CONVERSATIONS_TTL
    ) {
        logger.debug("缓存用户对话列表: userId={}, count={}", userId.value, conversations.size)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_CONVERSATIONS_PREFIX}${userId.value}"
                
                val cachedList = CachedUserConversations(
                    userId = userId.value,
                    conversations = conversations.map { conv ->
                        CachedConversationSummary(
                            conversationId = conv.id.toString(),
                            title = conv.title,
                            messageCount = conv.messageCount,
                            lastMessageAt = conv.updatedAt,
                            createdAt = conv.createdAt
                        )
                    },
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedList,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        } catch (e: Exception) {
            logger.error("缓存用户对话列表失败: userId={}", userId.value, e)
        }
    }
    
    /**
     * 获取用户对话列表缓存
     */
    suspend fun getUserConversations(userId: UserId): List<CachedConversationSummary>? {
        logger.debug("获取用户对话列表缓存: userId={}", userId.value)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${USER_CONVERSATIONS_PREFIX}${userId.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedUserConversations
                cached?.conversations
            }
        } catch (e: Exception) {
            logger.error("获取用户对话列表缓存失败: userId={}", userId.value, e)
            null
        }
    }
    
    /**
     * 缓存最近消息
     */
    suspend fun cacheRecentMessages(
        userId: UserId,
        messages: List<MessageDto>,
        ttl: Duration = RECENT_MESSAGES_TTL
    ) {
        logger.debug("缓存最近消息: userId={}, count={}", userId.value, messages.size)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${RECENT_MESSAGES_PREFIX}${userId.value}"
                
                val cachedMessages = CachedRecentMessages(
                    userId = userId.value,
                    messages = messages.map { msg ->
                        CachedMessage(
                            messageId = msg.id.toString(),
                            content = msg.content,
                            role = msg.type,
                            type = msg.type,
                            userId = msg.userId.toString(),
                            metadata = emptyMap(),
                            createdAt = msg.createdAt
                        )
                    },
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedMessages,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        } catch (e: Exception) {
            logger.error("缓存最近消息失败: userId={}", userId.value, e)
        }
    }
    
    /**
     * 获取最近消息缓存
     */
    suspend fun getRecentMessages(userId: UserId): List<CachedMessage>? {
        logger.debug("获取最近消息缓存: userId={}", userId.value)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${RECENT_MESSAGES_PREFIX}${userId.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedRecentMessages
                cached?.messages
            }
        } catch (e: Exception) {
            logger.error("获取最近消息缓存失败: userId={}", userId.value, e)
            null
        }
    }
    
    /**
     * 失效对话相关缓存
     */
    suspend fun invalidateConversationCache(conversationId: ConversationId) {
        logger.debug("失效对话缓存: conversationId={}", conversationId.value)
        
        try {
            withContext(Dispatchers.IO) {
                val patterns = listOf(
                    "${CONVERSATION_PREFIX}${conversationId.value}",
                    "${CONVERSATION_HISTORY_PREFIX}${conversationId.value}",
                    "${CONVERSATION_CONTEXT_PREFIX}${conversationId.value}",
                    "${CONVERSATION_STATS_PREFIX}${conversationId.value}"
                )
                
                patterns.forEach { pattern ->
                    redisTemplate.delete(pattern)
                }
                
                logger.debug("对话缓存失效完成: conversationId={}", conversationId.value)
            }
        } catch (e: Exception) {
            logger.error("失效对话缓存失败: conversationId={}", conversationId.value, e)
        }
    }
    
    /**
     * 失效用户对话列表缓存
     */
    suspend fun invalidateUserConversationsCache(userId: UserId) {
        logger.debug("失效用户对话列表缓存: userId={}", userId.value)
        
        try {
            withContext(Dispatchers.IO) {
                val patterns = listOf(
                    "${USER_CONVERSATIONS_PREFIX}${userId.value}",
                    "${RECENT_MESSAGES_PREFIX}${userId.value}"
                )
                
                patterns.forEach { pattern ->
                    redisTemplate.delete(pattern)
                }
            }
        } catch (e: Exception) {
            logger.error("失效用户对话列表缓存失败: userId={}", userId.value, e)
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    suspend fun getCacheStatistics(): ConversationCacheStatistics {
        return try {
            withContext(Dispatchers.IO) {
                val conversationKeys = redisTemplate.keys("${CONVERSATION_PREFIX}*")
                val historyKeys = redisTemplate.keys("${CONVERSATION_HISTORY_PREFIX}*")
                val contextKeys = redisTemplate.keys("${CONVERSATION_CONTEXT_PREFIX}*")
                val userConversationKeys = redisTemplate.keys("${USER_CONVERSATIONS_PREFIX}*")
                val recentMessageKeys = redisTemplate.keys("${RECENT_MESSAGES_PREFIX}*")
                
                ConversationCacheStatistics(
                    conversationCacheCount = conversationKeys.size,
                    historyCacheCount = historyKeys.size,
                    contextCacheCount = contextKeys.size,
                    userConversationCacheCount = userConversationKeys.size,
                    recentMessageCacheCount = recentMessageKeys.size,
                    totalCacheCount = conversationKeys.size + historyKeys.size + contextKeys.size + userConversationKeys.size + recentMessageKeys.size
                )
            }
        } catch (e: Exception) {
            logger.error("获取缓存统计失败", e)
            ConversationCacheStatistics(0, 0, 0, 0, 0, 0)
        }
    }
}

/**
 * 缓存的对话信息
 */
data class CachedConversation(
    val conversationId: String,
    val title: String,
    val userId: String,
    val knowledgeBaseId: String?,
    val modelName: String,
    val modelConfig: Map<String, Any>,
    val messageCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 缓存的对话历史
 */
data class CachedConversationHistory(
    val conversationId: String,
    val messages: List<CachedMessage>,
    val totalMessages: Int,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 缓存的消息
 */
data class CachedMessage(
    val messageId: String,
    val content: String,
    val role: String,
    val type: String,
    val userId: String,
    val metadata: Map<String, Any>,
    val createdAt: Instant
)

/**
 * 缓存的对话上下文
 */
data class CachedConversationContext(
    val conversationId: String,
    val context: ConversationContext,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 对话上下文
 */
data class ConversationContext(
    val currentTopic: String?,
    val keywords: List<String>,
    val entities: Map<String, String>,
    val sentiment: String?,
    val lastKnowledgeBaseUsed: String?,
    val contextWindow: Int = 10
)

/**
 * 缓存的用户对话列表
 */
data class CachedUserConversations(
    val userId: String,
    val conversations: List<CachedConversationSummary>,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 缓存的对话摘要
 */
data class CachedConversationSummary(
    val conversationId: String,
    val title: String,
    val messageCount: Int,
    val lastMessageAt: Instant,
    val createdAt: Instant
)

/**
 * 缓存的最近消息
 */
data class CachedRecentMessages(
    val userId: String,
    val messages: List<CachedMessage>,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 对话缓存统计信息
 */
data class ConversationCacheStatistics(
    val conversationCacheCount: Int,
    val historyCacheCount: Int,
    val contextCacheCount: Int,
    val userConversationCacheCount: Int,
    val recentMessageCacheCount: Int,
    val totalCacheCount: Int
)