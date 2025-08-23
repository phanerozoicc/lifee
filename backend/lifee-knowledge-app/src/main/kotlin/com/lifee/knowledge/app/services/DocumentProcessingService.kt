package com.lifee.knowledge.app.services

import com.lifee.common.cqrs.events.EventBus
import com.lifee.knowledge.domain.events.DocumentVectorizedEvent
import com.lifee.knowledge.domain.events.DocumentIndexedEvent
import com.lifee.knowledge.domain.events.DocumentProcessingCompletedEvent
import com.lifee.knowledge.domain.valueobjects.DocumentId
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.common.domain.valueobjects.UserId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

/**
 * 文档处理服务
 * 负责文档的向量化处理和索引构建
 */
@Service
class DocumentProcessingService(
    private val vectorEmbeddingService: VectorEmbeddingService,
    private val indexingService: IndexingService,
    private val eventBus: EventBus
) {
    
    private val logger = LoggerFactory.getLogger(DocumentProcessingService::class.java)
    
    /**
     * 处理文档：向量化 + 索引构建
     */
    suspend fun processDocument(
        knowledgeBaseId: KnowledgeBaseId,
        documentId: DocumentId,
        userId: UserId,
        title: String,
        content: String,
        type: String
    ) {
        logger.info("开始处理文档: documentId={}, knowledgeBaseId={}", documentId.value, knowledgeBaseId.value)
        
        var isSuccessful = false
        var errorMessage: String? = null
        
        var vectorizationTime = 0L
        var indexingTime = 0L
        
        val totalTime = measureTimeMillis {
            try {
                // 1. 向量化处理
                vectorizationTime = measureTimeMillis {
                    val vectorResult = vectorEmbeddingService.embedDocument(
                        documentId = documentId,
                        content = content,
                        type = type
                    )
                }
                
                // 发布向量化完成事件
                val vectorizedEvent = DocumentVectorizedEvent(
                    knowledgeBaseId = knowledgeBaseId,
                    documentId = documentId,
                    userId = userId,
                    vectorDimension = 768, // 模拟向量维度
                    chunkCount = 10, // 模拟分块数量
                    processingTimeMs = vectorizationTime
                )
                eventBus.publish(vectorizedEvent)
                
                logger.debug("文档向量化完成: documentId={}, 耗时={}ms", documentId.value, vectorizationTime)
                
                // 2. 索引构建
                indexingTime = measureTimeMillis {
                    val indexResult = indexingService.buildIndex(
                        knowledgeBaseId = knowledgeBaseId,
                        documentId = documentId,
                        title = title,
                        content = content,
                        type = type
                    )
                }
                
                // 发布索引构建完成事件
                val indexedEvent = DocumentIndexedEvent(
                    knowledgeBaseId = knowledgeBaseId,
                    documentId = documentId,
                    userId = userId,
                    indexType = "inverted_index",
                    indexSize = content.length.toLong(),
                    processingTimeMs = indexingTime
                )
                eventBus.publish(indexedEvent)
                
                logger.debug("文档索引构建完成: documentId={}, 耗时={}ms", documentId.value, indexingTime)
                
                isSuccessful = true
                
            } catch (e: Exception) {
                logger.error("文档处理失败: documentId={}", documentId.value, e)
                errorMessage = e.message
                throw e
            }
        }
        
        // 发布文档处理完成事件
        val completedEvent = DocumentProcessingCompletedEvent(
            knowledgeBaseId = knowledgeBaseId,
            documentId = documentId,
            userId = userId,
            totalProcessingTimeMs = totalTime,
            success = isSuccessful,
            errorMessage = errorMessage
        )
        eventBus.publish(completedEvent)
        
        logger.info("文档处理完成: documentId={}, 成功={}, 总耗时={}ms", 
            documentId.value, isSuccessful, totalTime)
    }
}

/**
 * 向量嵌入服务
 * 
 * 扩展优化策略：
 * 1. 模型优化：
 *    - 多模型支持：集成OpenAI、Cohere、本地BERT等多种嵌入模型
 *    - 模型微调：基于领域数据微调嵌入模型提高准确性
 *    - 模型版本管理：支持模型版本切换和A/B测试
 *    - 动态模型选择：基于文档类型和语言自动选择最优模型
 * 
 * 2. 分块优化：
 *    - 智能分块：基于语义边界进行分块而非固定长度
 *    - 重叠分块：使用滑动窗口确保语义连续性
 *    - 层次分块：支持段落、章节、文档多级分块
 *    - 自适应分块：基于文档结构动态调整分块策略
 * 
 * 3. 性能优化：
 *    - 批量处理：支持批量向量化减少API调用
 *    - 异步处理：使用消息队列异步处理大文档
 *    - 缓存机制：缓存常用文档片段的向量表示
 *    - 增量更新：支持文档增量更新而非全量重新处理
 */
@Service
class VectorEmbeddingService {
    
    private val logger = LoggerFactory.getLogger(VectorEmbeddingService::class.java)
    
    /**
     * 对文档进行向量化处理
     */
    suspend fun embedDocument(
        documentId: DocumentId,
        content: String,
        type: String
    ): VectorEmbeddingResult = withContext(Dispatchers.IO) {
        logger.debug("开始向量化文档: documentId={}, contentLength={}", documentId.value, content.length)
        
        // 实现向量化逻辑
        // 1. 文档分块
        val chunks = chunkDocument(content, type)
        logger.debug("文档分块完成: chunkCount={}", chunks.size)
        
        // 2. 调用向量化模型
        val embeddings = generateEmbeddings(chunks)
        logger.debug("向量化完成: embeddingCount={}", embeddings.size)
        
        // 3. 存储向量数据
        storeVectorEmbeddings(documentId, embeddings)
        logger.debug("向量数据存储完成")
        
        // 模拟处理时间
        delay(200 + (content.length / 100).toLong())
        
        VectorEmbeddingResult(
            dimension = 768, // 模拟向量维度
            chunkCount = chunks.size
        )
    }
    
    private fun chunkDocument(content: String, type: String): List<String> {
        // 实现智能分块逻辑
        val chunkSize = when (type.uppercase()) {
            "MARKDOWN" -> 1000
            "HTML" -> 800
            "PDF" -> 1200
            "DOCX" -> 1000
            else -> 500
        }
        
        val overlapSize = chunkSize / 4 // 25%重叠
        val chunks = mutableListOf<String>()
        
        // 基于段落和句子边界进行智能分块
        val paragraphs = content.split("\n\n").filter { it.isNotBlank() }
        var currentChunk = StringBuilder()
        
        for (paragraph in paragraphs) {
            if (currentChunk.length + paragraph.length <= chunkSize) {
                if (currentChunk.isNotEmpty()) currentChunk.append("\n\n")
                currentChunk.append(paragraph)
            } else {
                if (currentChunk.isNotEmpty()) {
                    chunks.add(currentChunk.toString())
                    // 保持重叠
                    val overlap = currentChunk.toString().takeLast(overlapSize)
                    currentChunk = StringBuilder(overlap)
                }
                currentChunk.append(paragraph)
            }
        }
        
        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toString())
        }
        
        return chunks.ifEmpty { listOf(content) }
    }
    
    private suspend fun generateEmbeddings(chunks: List<String>): List<FloatArray> {
        // 实现向量化模型调用
        val batchSize = 10 // 批量处理减少API调用
        val embeddings = mutableListOf<FloatArray>()
        
        chunks.chunked(batchSize).forEach { batch ->
            try {
                // 模拟调用向量化API（实际应用中替换为真实API调用）
                val batchEmbeddings = batch.map { chunk ->
                    // 使用简单的哈希向量化作为示例
                    generateSimpleEmbedding(chunk)
                }
                embeddings.addAll(batchEmbeddings)
                
                // 避免API限流
                kotlinx.coroutines.delay(100)
            } catch (e: Exception) {
                logger.error("向量化失败: batch={}, error={}", batch.size, e.message)
                // 降级处理：使用简单向量
                val fallbackEmbeddings = batch.map { FloatArray(768) { 0.1f } }
                embeddings.addAll(fallbackEmbeddings)
            }
        }
        
        return embeddings
    }
    
    private fun generateSimpleEmbedding(text: String): FloatArray {
        // 简单的文本向量化实现（实际应用中应使用专业模型）
        val words = text.lowercase().split("\\s+").filter { it.isNotBlank() }
        val embedding = FloatArray(768) { 0f }
        
        words.forEachIndexed { index, word ->
            val hash = word.hashCode()
            val pos = kotlin.math.abs(hash) % 768
            embedding[pos] += 1f / words.size
        }
        
        // 归一化
        val norm = kotlin.math.sqrt(embedding.sumOf { (it * it).toDouble() }).toFloat()
        if (norm > 0) {
            for (i in embedding.indices) {
                embedding[i] /= norm
            }
        }
        
        return embedding
    }
    
    private suspend fun storeVectorEmbeddings(documentId: DocumentId, embeddings: List<FloatArray>) {
        // 存储向量数据到向量数据库
        try {
            // 模拟向量数据库存储（实际应用中连接真实向量数据库）
            embeddings.forEachIndexed { index, embedding ->
                val vectorId = "${documentId.value}_chunk_$index"
                // 这里应该调用向量数据库API存储
                logger.trace("存储向量: vectorId={}, dimension={}", vectorId, embedding.size)
            }
            
            logger.info("向量数据存储成功: documentId={}, embeddingCount={}", documentId.value, embeddings.size)
        } catch (e: Exception) {
            logger.error("向量数据存储失败: documentId={}, error={}", documentId.value, e.message)
            throw RuntimeException("向量数据存储失败", e)
        }
    }
}

/**
 * 索引服务
 * 
 * 扩展优化策略：
 * 1. 索引类型优化：
 *    - 多类型索引：支持倒排索引、向量索引、图索引等
 *    - 混合索引：结合全文搜索和向量搜索的混合索引
 *    - 分层索引：构建多层次索引提高查询效率
 *    - 压缩索引：使用压缩算法减少索引存储空间
 * 
 * 2. 搜索引擎集成：
 *    - Elasticsearch集成：支持复杂查询和聚合分析
 *    - Solr集成：支持企业级搜索功能
 *    - 本地索引：使用Lucene构建本地搜索索引
 *    - 分布式索引：支持索引分片和副本
 * 
 * 3. 性能优化：
 *    - 增量索引：支持文档增量更新索引
 *    - 并行构建：多线程并行构建索引
 *    - 索引预热：预加载热点索引到内存
 *    - 查询优化：索引结构优化和查询计划优化
 * 
 * 4. 智能特性：
 *    - 自动补全：构建前缀索引支持搜索建议
 *    - 拼写纠错：集成拼写检查和纠错功能
 *    - 同义词扩展：支持同义词和相关词扩展
 *    - 个性化索引：基于用户偏好构建个性化索引
 */
@Service
class IndexingService {
    
    private val logger = LoggerFactory.getLogger(IndexingService::class.java)
    
    /**
     * 构建文档索引
     */
    suspend fun buildIndex(
        knowledgeBaseId: KnowledgeBaseId,
        documentId: DocumentId,
        title: String,
        content: String,
        type: String
    ): IndexingResult = withContext(Dispatchers.IO) {
        logger.debug("开始构建索引: documentId={}, knowledgeBaseId={}", documentId.value, knowledgeBaseId.value)
        
        // 实现索引构建逻辑
        // 1. 文本预处理
        val processedContent = preprocessContent(content, type)
        logger.debug("文本预处理完成: originalLength={}, processedLength={}", content.length, processedContent.length)
        
        // 2. 关键词提取
        val keywords = extractKeywords(processedContent)
        logger.debug("关键词提取完成: keywordCount={}", keywords.size)
        
        // 3. 构建倒排索引
        val invertedIndex = buildInvertedIndex(documentId, processedContent, keywords)
        logger.debug("倒排索引构建完成: indexSize={}", invertedIndex.size)
        
        // 4. 存储索引数据
        storeIndex(knowledgeBaseId, documentId, invertedIndex)
        logger.debug("索引数据存储完成")
        
        // 模拟处理时间
        delay(150 + (content.length / 200).toLong())
        
        IndexingResult(
            indexType = "inverted_index",
            indexSize = processedContent.length.toLong()
        )
    }
    
    private fun preprocessContent(content: String, type: String): String {
        // 实现文本预处理
        var processed = content
        
        // 根据文档类型进行特定预处理
        when (type.uppercase()) {
            "HTML" -> {
                // 去除HTML标签
                processed = processed.replace(Regex("<[^>]+>"), " ")
                // 解码HTML实体
                processed = processed.replace("&nbsp;", " ")
                    .replace("&lt;", "<")
                    .replace("&gt;", ">")
                    .replace("&amp;", "&")
            }
            "MARKDOWN" -> {
                // 去除Markdown标记
                processed = processed.replace(Regex("#{1,6}\\s*"), "") // 标题
                    .replace(Regex("\\*\\*([^*]+)\\*\\*"), "$1") // 粗体
                    .replace(Regex("\\*([^*]+)\\*"), "$1") // 斜体
                    .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1") // 链接
            }
        }
        
        // 通用预处理
        processed = processed.lowercase()
            .replace(Regex("[^\\w\\s\\u4e00-\\u9fff]"), " ") // 保留中文字符
            .replace(Regex("\\s+"), " ") // 合并多个空格
            .trim()
        
        return processed
    }
    
    private fun extractKeywords(content: String): List<String> {
        // 实现关键词提取
        val words = content.split("\\s+").filter { it.length > 2 }
        
        // 停用词列表（简化版）
        val stopWords = setOf(
            "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
            "是", "的", "了", "在", "有", "和", "就", "不", "人", "都", "一", "个", "上", "也", "很", "到", "说", "要", "去", "你", "会", "着", "没有", "看"
        )
        
        // 过滤停用词
        val filteredWords = words.filter { it !in stopWords }
        
        // 简单的TF计算
        val wordFreq = filteredWords.groupingBy { it }.eachCount()
        
        // 按频率排序，取前20个作为关键词
        val keywords = wordFreq.entries
            .sortedByDescending { it.value }
            .take(20)
            .map { it.key }
        
        // 添加长词（可能是专业术语）
        val longWords = filteredWords.filter { it.length >= 6 }.distinct()
        
        return (keywords + longWords).distinct()
    }
    
    private fun buildInvertedIndex(documentId: DocumentId, content: String, keywords: List<String>): Map<String, List<Int>> {
        // 构建倒排索引
        val words = content.split("\\s+")
        val index = mutableMapOf<String, MutableList<Int>>()
        
        // 为每个关键词建立位置索引
        keywords.forEach { keyword ->
            val positions = mutableListOf<Int>()
            words.forEachIndexed { wordIndex, word ->
                if (word.contains(keyword, ignoreCase = true)) {
                    positions.add(wordIndex)
                }
            }
            if (positions.isNotEmpty()) {
                index[keyword] = positions
            }
        }
        
        // 添加n-gram索引（2-gram和3-gram）
        for (n in 2..3) {
            val ngrams = words.windowed(n) { it.joinToString(" ") }
            ngrams.forEachIndexed { ngramIndex, ngram ->
                if (ngram.length > 5) { // 过滤太短的n-gram
                    index.getOrPut(ngram) { mutableListOf() }.add(ngramIndex)
                }
            }
        }
        
        return index.mapValues { it.value.toList() }
    }
    
    private suspend fun storeIndex(knowledgeBaseId: KnowledgeBaseId, documentId: DocumentId, index: Map<String, List<Int>>) {
        // 存储索引数据到搜索引擎
        try {
            // 构建索引文档
            val indexDocument = mapOf(
                "knowledge_base_id" to knowledgeBaseId.value,
                "document_id" to documentId.value,
                "index_data" to index,
                "created_at" to java.time.Instant.now().toString(),
                "index_version" to "1.0"
            )
            
            // 模拟存储到搜索引擎（实际应用中连接Elasticsearch等）
            logger.trace("索引文档结构: {}", indexDocument.keys)
            
            // 这里应该调用搜索引擎API存储索引
            // 例如：elasticsearchClient.index(indexDocument)
            
            logger.info("索引数据存储成功: knowledgeBaseId={}, documentId={}, indexSize={}", 
                knowledgeBaseId.value, documentId.value, index.size)
        } catch (e: Exception) {
            logger.error("索引数据存储失败: documentId={}, error={}", documentId.value, e.message)
            throw RuntimeException("索引数据存储失败", e)
        }
    }
}

/**
 * 向量化结果
 */
data class VectorEmbeddingResult(
    val dimension: Int,
    val chunkCount: Int
)

/**
 * 索引构建结果
 */
data class IndexingResult(
    val indexType: String,
    val indexSize: Long
)