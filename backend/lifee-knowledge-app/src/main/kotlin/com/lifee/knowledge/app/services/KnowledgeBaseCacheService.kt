package com.lifee.knowledge.app.services

import com.lifee.common.cqrs.events.EventBus
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.knowledge.domain.valueobjects.DocumentId
import com.lifee.knowledge.application.dto.KnowledgeBaseDto
import com.lifee.knowledge.application.dto.DocumentDto
import com.lifee.knowledge.app.events.KnowledgeBaseCachedEvent
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
 * 知识库缓存服务
 * 负责知识库相关数据的缓存管理
 */
@Service
class KnowledgeBaseCacheService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val eventBus: EventBus
) {
    
    private val logger = LoggerFactory.getLogger(KnowledgeBaseCacheService::class.java)
    
    companion object {
        private const val KB_METADATA_PREFIX = "kb:metadata:"
        private const val KB_DOCUMENTS_PREFIX = "kb:documents:"
        private const val KB_SEARCH_PREFIX = "kb:search:"
        private const val KB_STATS_PREFIX = "kb:stats:"
        private const val KB_USER_LIST_PREFIX = "kb:user_list:"
        private const val DOCUMENT_CONTENT_PREFIX = "doc:content:"
        private const val DOCUMENT_CHUNKS_PREFIX = "doc:chunks:"
        
        // 缓存过期时间
        private val METADATA_TTL = Duration.ofHours(2)
        private val DOCUMENTS_TTL = Duration.ofHours(1)
        private val SEARCH_RESULTS_TTL = Duration.ofMinutes(30)
        private val STATS_TTL = Duration.ofMinutes(15)
        private val USER_LIST_TTL = Duration.ofMinutes(30)
        private val CONTENT_TTL = Duration.ofMinutes(45)
        private val CHUNKS_TTL = Duration.ofHours(1)
    }
    
    /**
     * 缓存知识库元数据
     */
    suspend fun cacheKnowledgeBaseMetadata(
        kbId: KnowledgeBaseId,
        kbDto: KnowledgeBaseDto,
        ttl: Duration = METADATA_TTL
    ) {
        logger.debug("缓存知识库元数据: kbId={}", kbId.value.toString())
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${KB_METADATA_PREFIX}${kbId.value}"
                
                val cachedMetadata = CachedKnowledgeBaseMetadata(
                    kbId = kbId.value.toString(),
                    name = kbDto.name,
                    description = kbDto.description,
                    ownerId = kbDto.ownerId,
                    documentCount = kbDto.documentCount,
                    totalSize = kbDto.totalSize,
                    embeddingModel = kbDto.embeddingModel,
                    rerankModel = kbDto.rerankModel,
                    createdAt = kbDto.createdAt,
                    updatedAt = kbDto.updatedAt,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedMetadata,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
                
                logger.debug("知识库元数据缓存成功: kbId={}, ttl={}秒", kbId.value.toString(), ttl.seconds)
            }
            
            // 发布缓存事件
            eventBus.publish(KnowledgeBaseCachedEvent(
                kbId = kbId.value.toString(),
                cacheType = "metadata",
                cachedAt = Instant.now()
            ))
            
        } catch (e: Exception) {
            logger.error("缓存知识库元数据失败: kbId={}", kbId.value.toString(), e)
        }
    }
    
    /**
     * 获取知识库元数据缓存
     */
    suspend fun getKnowledgeBaseMetadata(kbId: KnowledgeBaseId): CachedKnowledgeBaseMetadata? {
        logger.debug("获取知识库元数据缓存: kbId={}", kbId.value.toString())
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${KB_METADATA_PREFIX}${kbId.value}"
                redisTemplate.opsForValue().get(cacheKey) as? CachedKnowledgeBaseMetadata
            }
        } catch (e: Exception) {
            logger.error("获取知识库元数据缓存失败: kbId={}", kbId.value.toString(), e)
            null
        }
    }
    
    /**
     * 缓存知识库文档列表
     */
    suspend fun cacheKnowledgeBaseDocuments(
        kbId: KnowledgeBaseId,
        documents: List<DocumentDto>,
        ttl: Duration = DOCUMENTS_TTL
    ) {
        logger.debug("缓存知识库文档列表: kbId={}, count={}", kbId.value.toString(), documents.size)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${KB_DOCUMENTS_PREFIX}${kbId.value}"
                
                val cachedDocuments = CachedKnowledgeBaseDocuments(
                    kbId = kbId.value.toString(),
                    documents = documents.map { doc ->
                        CachedDocumentSummary(
                        documentId = doc.id,
                        title = doc.title,
                        type = doc.type,
                        size = doc.size.toLong(),
                        uploadedAt = doc.createdAt,
                        updatedAt = doc.updatedAt
                    )
                    },
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedDocuments,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        } catch (e: Exception) {
            logger.error("缓存知识库文档列表失败: kbId={}", kbId.value.toString(), e)
        }
    }
    
    /**
     * 获取知识库文档列表缓存
     */
    suspend fun getKnowledgeBaseDocuments(kbId: KnowledgeBaseId): List<CachedDocumentSummary>? {
        logger.debug("获取知识库文档列表缓存: kbId={}", kbId.value.toString())
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${KB_DOCUMENTS_PREFIX}${kbId.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedKnowledgeBaseDocuments
                cached?.documents
            }
        } catch (e: Exception) {
            logger.error("获取知识库文档列表缓存失败: kbId={}", kbId.value.toString(), e)
            null
        }
    }
    
    /**
     * 缓存搜索结果
     */
    suspend fun cacheSearchResults(
        kbId: KnowledgeBaseId,
        query: String,
        results: List<SearchResult>,
        ttl: Duration = SEARCH_RESULTS_TTL
    ) {
        logger.debug("缓存搜索结果: kbId={}, query={}, count={}", kbId.value.toString(), query, results.size)
        
        try {
            withContext(Dispatchers.IO) {
                val queryHash = query.hashCode().toString()
                val cacheKey = "${KB_SEARCH_PREFIX}${kbId.value}:$queryHash"
                
                val cachedResults = CachedSearchResults(
                    kbId = kbId.value.toString(),
                    query = query,
                    queryHash = queryHash,
                    results = results,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedResults,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        } catch (e: Exception) {
            logger.error("缓存搜索结果失败: kbId={}, query={}", kbId.value.toString(), query, e)
        }
    }
    
    /**
     * 获取搜索结果缓存
     */
    suspend fun getSearchResults(kbId: KnowledgeBaseId, query: String): List<SearchResult>? {
        logger.debug("获取搜索结果缓存: kbId={}, query={}", kbId.value.toString(), query)
        
        return try {
            withContext(Dispatchers.IO) {
                val queryHash = query.hashCode().toString()
                val cacheKey = "${KB_SEARCH_PREFIX}${kbId.value}:$queryHash"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedSearchResults
                cached?.results
            }
        } catch (e: Exception) {
            logger.error("获取搜索结果缓存失败: kbId={}, query={}", kbId.value.toString(), query, e)
            null
        }
    }
    
    /**
     * 缓存文档内容
     */
    suspend fun cacheDocumentContent(
        documentId: DocumentId,
        content: String,
        ttl: Duration = CONTENT_TTL
    ) {
        logger.debug("缓存文档内容: documentId={}, size={}", documentId.value, content.length)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${DOCUMENT_CONTENT_PREFIX}${documentId.value}"
                
                val cachedContent = CachedDocumentContent(
                    documentId = documentId.value.toString(),
                    content = content,
                    contentSize = content.length,
                    cachedAt = Instant.now(),
                    expiresAt = Instant.now().plus(ttl)
                )
                
                redisTemplate.opsForValue().set(
                    cacheKey,
                    cachedContent,
                    ttl.toMillis(),
                    TimeUnit.MILLISECONDS
                )
            }
        } catch (e: Exception) {
            logger.error("缓存文档内容失败: documentId={}", documentId.value, e)
        }
    }
    
    /**
     * 获取文档内容缓存
     */
    suspend fun getDocumentContent(documentId: DocumentId): String? {
        logger.debug("获取文档内容缓存: documentId={}", documentId.value)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${DOCUMENT_CONTENT_PREFIX}${documentId.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedDocumentContent
                cached?.content
            }
        } catch (e: Exception) {
            logger.error("获取文档内容缓存失败: documentId={}", documentId.value, e)
            null
        }
    }
    
    /**
     * 缓存用户知识库列表
     */
    suspend fun cacheUserKnowledgeBases(
        userId: UserId,
        knowledgeBases: List<KnowledgeBaseDto>,
        ttl: Duration = USER_LIST_TTL
    ) {
        logger.debug("缓存用户知识库列表: userId={}, count={}", userId.value, knowledgeBases.size)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${KB_USER_LIST_PREFIX}${userId.value}"
                
                val cachedList = CachedUserKnowledgeBases(
                    userId = userId.value,
                    knowledgeBases = knowledgeBases.map { kb ->
                        CachedKnowledgeBaseSummary(
                            kbId = kb.id,
                            name = kb.name,
                            description = kb.description,
                            documentCount = kb.documentCount,
                            totalSize = kb.totalSize,
                            createdAt = kb.createdAt,
                            updatedAt = kb.updatedAt
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
            logger.error("缓存用户知识库列表失败: userId={}", userId.value, e)
        }
    }
    
    /**
     * 获取用户知识库列表缓存
     */
    suspend fun getUserKnowledgeBases(userId: UserId): List<CachedKnowledgeBaseSummary>? {
        logger.debug("获取用户知识库列表缓存: userId={}", userId.value)
        
        return try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${KB_USER_LIST_PREFIX}${userId.value}"
                val cached = redisTemplate.opsForValue().get(cacheKey) as? CachedUserKnowledgeBases
                cached?.knowledgeBases
            }
        } catch (e: Exception) {
            logger.error("获取用户知识库列表缓存失败: userId={}", userId.value, e)
            null
        }
    }
    
    /**
     * 失效知识库相关缓存
     */
    suspend fun invalidateKnowledgeBaseCache(kbId: KnowledgeBaseId) {
        logger.debug("失效知识库缓存: kbId={}", kbId.value.toString())
        
        try {
            withContext(Dispatchers.IO) {
                val patterns = listOf(
                    "${KB_METADATA_PREFIX}${kbId.value}",
                "${KB_DOCUMENTS_PREFIX}${kbId.value}",
                "${KB_STATS_PREFIX}${kbId.value}"
                )
                
                patterns.forEach { pattern ->
                    redisTemplate.delete(pattern)
                }
                
                // 失效搜索结果缓存
                val searchPattern = "${KB_SEARCH_PREFIX}${kbId.value}:*"
                val searchKeys = redisTemplate.keys(searchPattern)
                if (searchKeys.isNotEmpty()) {
                    redisTemplate.delete(searchKeys)
                }
                
                logger.debug("知识库缓存失效完成: kbId={}", kbId.value.toString())
            }
        } catch (e: Exception) {
            logger.error("失效知识库缓存失败: kbId={}", kbId.value.toString(), e)
        }
    }
    
    /**
     * 失效用户知识库列表缓存
     */
    suspend fun invalidateUserKnowledgeBasesCache(userId: UserId) {
        logger.debug("失效用户知识库列表缓存: userId={}", userId.value)
        
        try {
            withContext(Dispatchers.IO) {
                val cacheKey = "${KB_USER_LIST_PREFIX}${userId.value}"
                redisTemplate.delete(cacheKey)
            }
        } catch (e: Exception) {
            logger.error("失效用户知识库列表缓存失败: userId={}", userId.value, e)
        }
    }
    
    /**
     * 失效文档相关缓存
     */
    suspend fun invalidateDocumentCache(documentId: DocumentId) {
        logger.debug("失效文档缓存: documentId={}", documentId.value)
        
        try {
            withContext(Dispatchers.IO) {
                val patterns = listOf(
                    "${DOCUMENT_CONTENT_PREFIX}${documentId.value}",
                    "${DOCUMENT_CHUNKS_PREFIX}${documentId.value}"
                )
                
                patterns.forEach { pattern ->
                    redisTemplate.delete(pattern)
                }
            }
        } catch (e: Exception) {
            logger.error("失效文档缓存失败: documentId={}", documentId.value, e)
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    suspend fun getCacheStatistics(): KnowledgeBaseCacheStatistics {
        return try {
            withContext(Dispatchers.IO) {
                val metadataKeys = redisTemplate.keys("${KB_METADATA_PREFIX}*")
                val documentsKeys = redisTemplate.keys("${KB_DOCUMENTS_PREFIX}*")
                val searchKeys = redisTemplate.keys("${KB_SEARCH_PREFIX}*")
                val contentKeys = redisTemplate.keys("${DOCUMENT_CONTENT_PREFIX}*")
                val userListKeys = redisTemplate.keys("${KB_USER_LIST_PREFIX}*")
                
                KnowledgeBaseCacheStatistics(
                    metadataCacheCount = metadataKeys.size,
                    documentsCacheCount = documentsKeys.size,
                    searchCacheCount = searchKeys.size,
                    contentCacheCount = contentKeys.size,
                    userListCacheCount = userListKeys.size,
                    totalCacheCount = metadataKeys.size + documentsKeys.size + searchKeys.size + contentKeys.size + userListKeys.size
                )
            }
        } catch (e: Exception) {
            logger.error("获取缓存统计失败", e)
            KnowledgeBaseCacheStatistics(0, 0, 0, 0, 0, 0)
        }
    }
}

/**
 * 缓存的知识库元数据
 */
data class CachedKnowledgeBaseMetadata(
    val kbId: String,
    val name: String,
    val description: String?,
    val ownerId: String,
    val documentCount: Int,
    val totalSize: Long,
    val embeddingModel: String,
    val rerankModel: String,
    val createdAt: Instant,
    val updatedAt: Instant,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 缓存的知识库文档列表
 */
data class CachedKnowledgeBaseDocuments(
    val kbId: String,
    val documents: List<CachedDocumentSummary>,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 缓存的文档摘要
 */
data class CachedDocumentSummary(
    val documentId: String,
    val title: String,
    val type: String,
    val size: Long,
    val uploadedAt: Instant,
    val updatedAt: Instant
)

/**
 * 缓存的搜索结果
 */
data class CachedSearchResults(
    val kbId: String,
    val query: String,
    val queryHash: String,
    val results: List<SearchResult>,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 搜索结果项
 */
data class SearchResult(
    val documentId: String,
    val title: String,
    val content: String,
    val score: Double,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * 缓存的文档内容
 */
data class CachedDocumentContent(
    val documentId: String,
    val content: String,
    val contentSize: Int,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 缓存的用户知识库列表
 */
data class CachedUserKnowledgeBases(
    val userId: String,
    val knowledgeBases: List<CachedKnowledgeBaseSummary>,
    val cachedAt: Instant,
    val expiresAt: Instant
)

/**
 * 缓存的知识库摘要
 */
data class CachedKnowledgeBaseSummary(
    val kbId: String,
    val name: String,
    val description: String?,
    val documentCount: Int,
    val totalSize: Long,
    val createdAt: Instant,
    val updatedAt: Instant
)

/**
 * 知识库缓存统计信息
 */
data class KnowledgeBaseCacheStatistics(
    val metadataCacheCount: Int,
    val documentsCacheCount: Int,
    val searchCacheCount: Int,
    val contentCacheCount: Int,
    val userListCacheCount: Int,
    val totalCacheCount: Int
)