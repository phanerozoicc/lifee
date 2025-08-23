package com.lifee.knowledge.app.application.commands

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.orchestration.BusinessFlowOrchestrator
import com.lifee.common.orchestration.BusinessFlowType
import com.lifee.knowledge.domain.aggregates.KnowledgeBase
import com.lifee.knowledge.domain.entities.Document
import com.lifee.knowledge.domain.valueobjects.*
import com.lifee.knowledge.domain.repositories.KnowledgeBaseRepository
import com.lifee.knowledge.app.services.DocumentProcessingService
// import com.lifee.knowledge.domain.services.VectorSearchService // 暂时注释掉，待实现
import com.lifee.common.domain.valueobjects.UserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.time.Instant

/**
 * 创建知识库命令
 */
data class CreateKnowledgeBaseCommand(
    val userId: String,
    val name: String,
    val description: String,
    val embeddingModel: String = "text-embedding-ada-002",
    val rerankModel: String = "bge-reranker-large"
) : com.lifee.common.cqrs.commands.Command

/**
 * 上传文档命令
 */
data class UploadDocumentCommand(
    val knowledgeBaseId: String,
    val userId: String,
    val files: List<MultipartFile>,
    val overwrite: Boolean = false
) : com.lifee.common.cqrs.commands.Command

/**
 * 搜索知识库命令
 */
data class SearchKnowledgeBaseCommand(
    val knowledgeBaseId: String,
    val userId: String,
    val query: String,
    val limit: Int = 10,
    val threshold: Double = 0.7
) : com.lifee.common.cqrs.commands.Command

/**
 * 知识库管理命令处理器
 * 协调完整的知识库管理流程
 */
@Component
class KnowledgeBaseManagementCommandHandler(
    private val knowledgeBaseRepository: KnowledgeBaseRepository,
    private val documentProcessingService: DocumentProcessingService,
    // private val vectorSearchService: VectorSearchService, // 暂时注释掉，待实现
    private val businessFlowOrchestrator: BusinessFlowOrchestrator
) : AsyncCommandHandler<CreateKnowledgeBaseCommand, KnowledgeBaseResult> {
    
    private val logger = LoggerFactory.getLogger(KnowledgeBaseManagementCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: CreateKnowledgeBaseCommand): KnowledgeBaseResult {
        logger.info("Creating knowledge base for user: {}", command.userId)
        
        try {
            // 1. 验证输入
            validateCreateKnowledgeBaseInput(command)
            
            // 2. 创建知识库
            val knowledgeBaseId = KnowledgeBaseId.generate()
            val userId = UserId.fromString(command.userId)
            val name = KnowledgeBaseName(command.name)
            val description = KnowledgeBaseDescription(command.description)
            
            val knowledgeBase = KnowledgeBase.create(
                id = knowledgeBaseId,
                name = name,
                description = description,
                ownerId = userId
            )
            
            // 3. 保存知识库
            knowledgeBaseRepository.save(knowledgeBase)
            logger.info("Knowledge base created successfully with ID: {}", knowledgeBaseId)
            
            // 4. 启动知识库创建业务流程
            val flowId = businessFlowOrchestrator.startFlow(
                flowType = BusinessFlowType.KNOWLEDGE_BASE_CREATION,
                initiatorId = command.userId,
                flowData = mapOf(
                    "knowledgeBaseId" to knowledgeBaseId.toString(),
                    "userId" to command.userId,
                    "name" to command.name,
                    "description" to command.description,
                    "embeddingModel" to command.embeddingModel,
                    "rerankModel" to command.rerankModel
                )
            )
            
            return KnowledgeBaseResult.success(
                knowledgeBaseId = knowledgeBaseId.toString(),
                flowId = flowId,
                message = "知识库创建成功"
            )
            
        } catch (e: Exception) {
            logger.error("Error creating knowledge base: {}", e.message, e)
            return KnowledgeBaseResult.failure("知识库创建失败: ${e.message}")
        }
    }
    
    /**
     * 处理文档上传
     */
    suspend fun handleDocumentUpload(command: UploadDocumentCommand): DocumentUploadResult {
        logger.info("Uploading {} documents to knowledge base: {}", command.files.size, command.knowledgeBaseId)
        
        try {
            // 1. 验证输入
            validateUploadDocumentInput(command)
            
            // 2. 加载知识库
            val knowledgeBaseId = KnowledgeBaseId.fromString(command.knowledgeBaseId)
            val knowledgeBase = knowledgeBaseRepository.findById(knowledgeBaseId)
                ?: return DocumentUploadResult.failure("知识库不存在")
            
            // 3. 验证用户权限
            val userId = UserId.fromString(command.userId)
            if (knowledgeBase.getOwnerId() != userId) {
                return DocumentUploadResult.failure("无权限访问此知识库")
            }
            
            val uploadedDocuments = mutableListOf<String>()
            val failedDocuments = mutableListOf<String>()
            
            // 4. 处理每个文件
            command.files.forEach { file ->
                try {
                    val documentId = DocumentId.generate()
                    val content = String(file.bytes)
                    
                    // 创建文档
                    val document = Document.create(
                        id = documentId,
                        title = DocumentTitle(file.originalFilename ?: "unknown"),
                        content = DocumentContent(content),
                        type = DocumentType.fromString(getDocumentTypeFromMimeType(file.contentType ?: "text/plain"))
                    )
                    
                    // 添加到知识库
                    knowledgeBase.addDocument(
                        documentId = documentId,
                        title = DocumentTitle(file.originalFilename ?: "unknown"),
                        content = DocumentContent(content),
                        type = DocumentType.fromString(getDocumentTypeFromMimeType(file.contentType ?: "text/plain"))
                    )
                    
                    // 异步处理文档（分段、向量化）
                    processDocumentAsync(knowledgeBase.getKnowledgeBaseId(), document)
                    
                    uploadedDocuments.add(documentId.toString())
                    logger.info("Document uploaded successfully: {}", file.originalFilename ?: "unknown")
                    
                } catch (e: Exception) {
                    logger.error("Failed to upload document {}: {}", file.originalFilename, e.message, e)
                    failedDocuments.add(file.originalFilename ?: "unknown")
                }
            }
            
            // 5. 保存知识库
            knowledgeBaseRepository.save(knowledgeBase)
            
            return DocumentUploadResult.success(
                uploadedDocuments = uploadedDocuments,
                failedDocuments = failedDocuments,
                message = "文档上传完成"
            )
            
        } catch (e: Exception) {
            logger.error("Error uploading documents: {}", e.message, e)
            return DocumentUploadResult.failure("文档上传失败: ${e.message}")
        }
    }
    
    /**
     * 处理知识库搜索
     */
    suspend fun handleKnowledgeBaseSearch(command: SearchKnowledgeBaseCommand): SearchResult {
        logger.info("Searching knowledge base {} with query: {}", command.knowledgeBaseId, command.query)
        
        try {
            // 1. 验证输入
            validateSearchInput(command)
            
            // 2. 验证知识库存在性和权限
            val knowledgeBaseId = KnowledgeBaseId.fromString(command.knowledgeBaseId)
            val knowledgeBase = knowledgeBaseRepository.findById(knowledgeBaseId)
                ?: return SearchResult.failure("知识库不存在")
            
            val userId = UserId.fromString(command.userId)
            if (knowledgeBase.getOwnerId() != userId) {
                return SearchResult.failure("无权限访问此知识库")
            }
            
            // 3. 执行向量搜索
            // TODO: 实现向量搜索服务
            val searchResults = emptyList<DocumentSearchResult>()
            /*
            val searchResults = vectorSearchService.search(
                knowledgeBaseId = knowledgeBaseId,
                query = command.query,
                limit = command.limit,
                threshold = command.threshold
            )
            */
            
            logger.info("Found {} search results for query: {}", searchResults.size, command.query)
            
            return SearchResult.success(
                results = searchResults,
                query = command.query,
                totalCount = searchResults.size
            )
            
        } catch (e: Exception) {
            logger.error("Error searching knowledge base: {}", e.message, e)
            return SearchResult.failure("搜索失败: ${e.message}")
        }
    }
    
    /**
     * 异步处理文档
     */
    private suspend fun processDocumentAsync(knowledgeBaseId: KnowledgeBaseId, document: Document) {
        try {
            // 使用DocumentProcessingService进行完整的文档处理
            documentProcessingService.processDocument(
                knowledgeBaseId = knowledgeBaseId,
                documentId = document.getId(),
                userId = UserId.fromString("system"), // 临时使用系统用户ID
                title = document.getTitle().value,
                content = document.getContent().value,
                type = document.getType().value
            )
            
            logger.info("Document processing completed for: {}", document.getTitle().value)
            
        } catch (e: Exception) {
            logger.error("Error processing document {}: {}", document.getTitle().value, e.message, e)
        }
    }
    
    /**
     * 验证创建知识库输入
     */
    private fun validateCreateKnowledgeBaseInput(command: CreateKnowledgeBaseCommand) {
        require(command.userId.isNotBlank()) { "用户ID不能为空" }
        require(command.name.isNotBlank()) { "知识库名称不能为空" }
        require(command.name.length <= 100) { "知识库名称长度不能超过100个字符" }
        require(command.description.length <= 500) { "知识库描述长度不能超过500个字符" }
    }
    
    /**
     * 验证上传文档输入
     */
    private fun validateUploadDocumentInput(command: UploadDocumentCommand) {
        require(command.knowledgeBaseId.isNotBlank()) { "知识库ID不能为空" }
        require(command.userId.isNotBlank()) { "用户ID不能为空" }
        require(command.files.isNotEmpty()) { "至少需要上传一个文件" }
        
        // 验证文件大小和类型
        command.files.forEach { file ->
            require(file.size <= 10 * 1024 * 1024) { "文件大小不能超过10MB: ${file.originalFilename}" }
            require(!file.isEmpty) { "文件不能为空: ${file.originalFilename}" }
        }
    }
    
    /**
     * 验证搜索输入
     */
    private fun validateSearchInput(command: SearchKnowledgeBaseCommand) {
        require(command.knowledgeBaseId.isNotBlank()) { "知识库ID不能为空" }
        require(command.userId.isNotBlank()) { "用户ID不能为空" }
        require(command.query.isNotBlank()) { "搜索查询不能为空" }
        require(command.limit > 0) { "搜索结果数量必须大于0" }
        require(command.threshold in 0.0..1.0) { "相似度阈值必须在0.0到1.0之间" }
    }
    
    /**
     * 根据MIME类型获取文档类型字符串
     */
    private fun getDocumentTypeFromMimeType(mimeType: String): String {
        return when {
            mimeType.startsWith("text/") -> "TEXT"
            mimeType == "application/pdf" -> "PDF"
            mimeType.startsWith("application/vnd.openxmlformats-officedocument.wordprocessingml") -> "DOCX"
            mimeType == "application/msword" -> "DOC"
            else -> "TEXT"
        }
    }
}

/**
 * 知识库操作结果
 */
data class KnowledgeBaseResult(
    val success: Boolean,
    val knowledgeBaseId: String? = null,
    val flowId: String? = null,
    val message: String,
    val error: String? = null
) {
    companion object {
        fun success(knowledgeBaseId: String, flowId: String, message: String): KnowledgeBaseResult {
            return KnowledgeBaseResult(
                success = true,
                knowledgeBaseId = knowledgeBaseId,
                flowId = flowId,
                message = message
            )
        }
        
        fun failure(error: String): KnowledgeBaseResult {
            return KnowledgeBaseResult(
                success = false,
                message = "操作失败",
                error = error
            )
        }
    }
}

/**
 * 文档上传结果
 */
data class DocumentUploadResult(
    val success: Boolean,
    val uploadedDocuments: List<String> = emptyList(),
    val failedDocuments: List<String> = emptyList(),
    val message: String,
    val error: String? = null
) {
    companion object {
        fun success(uploadedDocuments: List<String>, failedDocuments: List<String>, message: String): DocumentUploadResult {
            return DocumentUploadResult(
                success = true,
                uploadedDocuments = uploadedDocuments,
                failedDocuments = failedDocuments,
                message = message
            )
        }
        
        fun failure(error: String): DocumentUploadResult {
            return DocumentUploadResult(
                success = false,
                message = "上传失败",
                error = error
            )
        }
    }
}

/**
 * 搜索结果
 */
data class SearchResult(
    val success: Boolean,
    val results: List<DocumentSearchResult> = emptyList(),
    val query: String? = null,
    val totalCount: Int = 0,
    val message: String,
    val error: String? = null
) {
    companion object {
        fun success(results: List<DocumentSearchResult>, query: String, totalCount: Int): SearchResult {
            return SearchResult(
                success = true,
                results = results,
                query = query,
                totalCount = totalCount,
                message = "搜索完成"
            )
        }
        
        fun failure(error: String): SearchResult {
            return SearchResult(
                success = false,
                message = "搜索失败",
                error = error
            )
        }
    }
}

/**
 * 文档搜索结果
 */
data class DocumentSearchResult(
    val documentId: String,
    val title: String,
    val content: String,
    val relevanceScore: Double,
    val chunkIndex: Int = 0
)