package com.lifee.knowledge.domain.aggregates

import com.lifee.common.domain.AggregateRoot
import com.lifee.knowledge.domain.entities.Document
import com.lifee.knowledge.domain.events.*
import com.lifee.knowledge.domain.valueobjects.*
import java.time.Instant

/**
 * 知识库聚合根
 */
class KnowledgeBase private constructor(
    private val id: KnowledgeBaseId,
    private var name: KnowledgeBaseName,
    private var description: KnowledgeBaseDescription,
    private val ownerId: UserId,
    private val createdAt: Instant,
    private var updatedAt: Instant,
    private val documents: MutableMap<DocumentId, Document> = mutableMapOf()
) : AggregateRoot<KnowledgeBaseId>(id) {
    
    companion object {
        /**
         * 创建新的知识库
         */
        fun create(
            id: KnowledgeBaseId,
            name: KnowledgeBaseName,
            description: KnowledgeBaseDescription,
            ownerId: UserId
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
                KnowledgeBaseCreatedEvent.create(
                    knowledgeBaseId = id,
                    name = name,
                    description = description,
                    ownerId = ownerId,
                    version = knowledgeBase.getVersion() + 1
                )
            )
            
            return knowledgeBase
        }
    }
    
    // Getters
    fun getId(): KnowledgeBaseId = id
    fun getName(): KnowledgeBaseName = name
    fun getDescription(): KnowledgeBaseDescription = description
    fun getOwnerId(): UserId = ownerId
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
            DocumentAddedEvent.create(
                knowledgeBaseId = id,
                documentId = documentId,
                title = title,
                type = type,
                contentLength = content.getLength(),
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
            DocumentUpdatedEvent.create(
                knowledgeBaseId = id,
                documentId = documentId,
                newTitle = newTitle,
                newContentLength = newContent.getLength(),
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
            DocumentRemovedEvent.create(
                knowledgeBaseId = id,
                documentId = documentId,
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
    fun isOwnedBy(userId: UserId): Boolean {
        return this.ownerId == userId
    }
    
    /**
     * 内部方法：直接添加文档（用于从持久化层重建聚合）
     */
    internal fun addDocumentInternal(document: Document) {
        documents[document.getId()] = document
    }
}