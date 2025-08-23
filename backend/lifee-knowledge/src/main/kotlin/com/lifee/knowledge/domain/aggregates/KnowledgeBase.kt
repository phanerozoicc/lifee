package com.lifee.knowledge.domain.aggregates

import com.lifee.common.domain.EventSourcedAggregateRoot
import com.lifee.knowledge.domain.entities.Document
import com.lifee.knowledge.domain.events.*
import com.lifee.knowledge.domain.valueobjects.*
import com.lifee.common.domain.valueobjects.UserId as CommonUserId
import java.time.Instant

/**
 * 知识库聚合根
 */
class KnowledgeBase private constructor(
    private val id: KnowledgeBaseId,
    private var name: KnowledgeBaseName,
    private var description: KnowledgeBaseDescription,
    private val ownerId: CommonUserId,
    private val createdAt: Instant,
    private var updatedAt: Instant,
    private val documents: MutableMap<DocumentId, Document> = mutableMapOf()
) : EventSourcedAggregateRoot<KnowledgeBaseId>(id) {
    
    companion object {
        /**
         * 创建新的知识库
         */
        fun create(
            id: KnowledgeBaseId,
            name: KnowledgeBaseName,
            description: KnowledgeBaseDescription,
            ownerId: CommonUserId
        ): KnowledgeBase {
            val now = Instant.now()
            val knowledgeBase = KnowledgeBase(
                id = id,
                name = name,
                description = description,
                ownerId = ownerId,
                createdAt = now,
                updatedAt = now
            )
            
            // 发布知识库创建事件
            knowledgeBase.addDomainEvent(
                KnowledgeBaseCreatedEvent(
                    knowledgeBaseId = id,
                    name = name,
                    description = description,
                    ownerId = ownerId
                )
            )
            
            return knowledgeBase
        }
        
        /**
         * 从持久化实体恢复知识库聚合根
         */
        fun fromEntity(
            id: KnowledgeBaseId,
            name: KnowledgeBaseName,
            description: KnowledgeBaseDescription,
            ownerId: CommonUserId,
            createdAt: Instant,
            updatedAt: Instant,
            documents: MutableMap<DocumentId, Document> = mutableMapOf()
        ): KnowledgeBase {
            return KnowledgeBase(
                id = id,
                name = name,
                description = description,
                ownerId = ownerId,
                createdAt = createdAt,
                updatedAt = updatedAt,
                documents = documents
            )
        }
    }
    
    // Getters
    fun getKnowledgeBaseId(): KnowledgeBaseId = id
    fun getName(): KnowledgeBaseName = name
    fun getDescription(): KnowledgeBaseDescription = description
    fun getOwnerId(): CommonUserId = ownerId
    fun getCreatedAt(): Instant = createdAt
    fun getUpdatedAt(): Instant = updatedAt
    fun getDocuments(): List<Document> = documents.values.toList()
    fun getDocumentCount(): Int = documents.size
    
    /**
     * 更新知识库信息
     */
    fun updateInfo(newName: KnowledgeBaseName, newDescription: KnowledgeBaseDescription) {
        this.name = newName
        this.description = newDescription
        this.updatedAt = Instant.now()
    }
    
    /**
     * 添加文档
     */
    fun addDocument(
        documentId: DocumentId,
        title: DocumentTitle,
        content: DocumentContent,
        type: DocumentType
    ) {
        require(!documents.containsKey(documentId)) {
            "Document with id ${documentId} already exists in knowledge base"
        }
        
        val document = Document.create(documentId, title, content, type)
        documents[documentId] = document
        this.updatedAt = Instant.now()
        
        // 发布文档添加事件
        addDomainEvent(
            DocumentAddedEvent(
                knowledgeBaseId = id,
                documentId = documentId,
                userId = com.lifee.common.domain.valueobjects.UserId(ownerId.value),
                title = title,
                content = content,
                type = type,
                contentLength = content.getLength(),
                aggregateId = id.value.toString(),
                version = getVersion() + 1
            )
        )
    }
    
    /**
     * 更新文档
     */
    fun updateDocument(
        documentId: DocumentId,
        newTitle: DocumentTitle,
        newContent: DocumentContent
    ) {
        val document = documents[documentId]
            ?: throw IllegalArgumentException("Document with id ${documentId} not found")
        
        document.updateContent(newTitle, newContent)
        this.updatedAt = Instant.now()
        
        // 发布文档更新事件
        addDomainEvent(
            DocumentUpdatedEvent(
                knowledgeBaseId = id,
                documentId = documentId,
                newTitle = newTitle,
                newContentLength = newContent.getLength(),
                aggregateId = id.value.toString(),
                version = getVersion() + 1
            )
        )
    }
    
    /**
     * 删除文档
     */
    fun removeDocument(documentId: DocumentId) {
        require(documents.containsKey(documentId)) {
            "Document with id ${documentId} not found in knowledge base"
        }
        
        documents.remove(documentId)
        this.updatedAt = Instant.now()
        
        // 发布文档删除事件
        addDomainEvent(
            DocumentRemovedEvent(
                knowledgeBaseId = id,
                documentId = documentId,
                aggregateId = id.value.toString(),
                version = getVersion() + 1
            )
        )
    }
    
    /**
     * 获取指定文档
     */
    fun getDocument(documentId: DocumentId): Document? {
        return documents[documentId]
    }
    
    /**
     * 检查是否包含指定文档
     */
    fun containsDocument(documentId: DocumentId): Boolean {
        return documents.containsKey(documentId)
    }
    
    /**
     * 获取知识库总大小（所有文档字符数之和）
     */
    fun getTotalSize(): Int {
        return documents.values.sumOf { it.getSize() }
    }
    
    /**
     * 检查知识库是否为空
     */
    fun isEmpty(): Boolean = documents.isEmpty()
    
    /**
     * 验证是否为所有者
     */
    fun isOwnedBy(userId: CommonUserId): Boolean {
        return this.ownerId == userId
    }
    
    /**
     * 内部方法：直接添加文档（用于从持久化层重建聚合）
     */
    internal fun addDocumentInternal(document: Document) {
        documents[document.getId()] = document
    }
    
    /**
     * 序列化聚合根状态
     */
    override fun serializeState(): Map<String, Any> {
        return mapOf(
            "id" to id.toString(),
            "name" to name.toString(),
            "description" to description.toString(),
            "ownerId" to ownerId.toString(),
            "createdAt" to createdAt.toString(),
            "updatedAt" to updatedAt.toString(),
            "documents" to documents.mapKeys { it.key.toString() }.mapValues { entry ->
                val doc = entry.value
                mapOf(
                    "id" to doc.getId().toString(),
                    "title" to doc.getTitle().toString(),
                    "content" to doc.getContent().toString(),
                    "type" to doc.getType().toString(),
                    "size" to doc.getSize(),
                    "createdAt" to doc.getCreatedAt().toString(),
                    "updatedAt" to doc.getUpdatedAt().toString()
                )
            }
        )
    }
    
    /**
     * 反序列化聚合根状态
     */
    override fun deserializeState(stateData: Map<String, Any>) {
        try {
            // 清空当前文档
            documents.clear()
            
            // 恢复基本信息
            name = KnowledgeBaseName(stateData["name"] as String)
            description = KnowledgeBaseDescription(stateData["description"] as String)
            
            // 恢复时间戳
            val updatedAtStr = stateData["updatedAt"] as? String
            if (updatedAtStr != null) {
                updatedAt = Instant.parse(updatedAtStr)
            }
            
            // 恢复文档
            @Suppress("UNCHECKED_CAST")
            val documentsData = stateData["documents"] as? Map<String, Map<String, Any>> ?: emptyMap()
            
            documentsData.forEach { (_, docData) ->
                try {
                    val docId = DocumentId(java.util.UUID.fromString(docData["id"] as String))
                    val title = DocumentTitle(docData["title"] as String)
                    val content = DocumentContent(docData["content"] as String)
                    val type = DocumentType.valueOf(docData["type"] as String)
                    val createdAt = Instant.parse(docData["createdAt"] as String)
                    val updatedAt = Instant.parse(docData["updatedAt"] as String)
                    
                    val document = Document(
                        id = docId,
                        title = title,
                        content = content,
                        type = type,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                    
                    documents[docId] = document
                } catch (e: Exception) {
                    // 记录错误但继续处理其他文档
                    // 在实际应用中可能需要更严格的错误处理
                }
            }
            
        } catch (e: Exception) {
            // 在实际应用中需要更严格的错误处理
            throw IllegalStateException("Failed to deserialize KnowledgeBase state", e)
        }
    }
    
    /**
     * 应用领域事件到聚合根
     */
    override fun applyEvent(event: com.lifee.common.domain.DomainEvent) {
        when (event) {
            is KnowledgeBaseCreatedEvent -> {
                // 知识库创建事件已在构造函数中处理
            }
            is DocumentAddedEvent -> {
                // 文档添加事件已在addDocument方法中处理
            }
            is DocumentUpdatedEvent -> {
                // 文档更新事件已在updateDocument方法中处理
            }
            is DocumentRemovedEvent -> {
                // 文档删除事件已在removeDocument方法中处理
            }
            // 可以根据需要添加更多事件处理
        }
    }
}