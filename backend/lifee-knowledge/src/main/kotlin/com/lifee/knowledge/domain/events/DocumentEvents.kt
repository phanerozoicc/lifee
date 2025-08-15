package com.lifee.knowledge.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.knowledge.domain.valueobjects.DocumentId
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 文档添加事件
 */
data class DocumentAddedEvent(
    val knowledgeBaseId: KnowledgeBaseId,
    val documentId: DocumentId,
    val userId: UserId,
    val title: String,
    val content: String,
    val type: String,
    val contentLength: Int,
    override val aggregateId: String = knowledgeBaseId.value,
    override val version: Long = 1L
) : DomainEvent() {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: java.util.UUID
    ): DomainEvent {
        return this.copy(
            knowledgeBaseId = KnowledgeBaseId(aggregateId)
        )
    }
}

/**
 * 文档向量化完成事件
 */
data class DocumentVectorizedEvent(
    val knowledgeBaseId: KnowledgeBaseId,
    val documentId: DocumentId,
    val userId: UserId,
    val vectorDimension: Int,
    val chunkCount: Int,
    val processingTimeMs: Long,
    override val aggregateId: String = knowledgeBaseId.value,
    override val version: Long = 1L
) : DomainEvent() {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: java.util.UUID
    ): DomainEvent {
        return this.copy(
            knowledgeBaseId = KnowledgeBaseId(aggregateId)
        )
    }
}

/**
 * 文档索引构建完成事件
 */
data class DocumentIndexedEvent(
    val knowledgeBaseId: KnowledgeBaseId,
    val documentId: DocumentId,
    val userId: UserId,
    val indexType: String,
    val indexSize: Long,
    val processingTimeMs: Long,
    override val aggregateId: String = knowledgeBaseId.value,
    override val version: Long = 1L
) : DomainEvent() {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: java.util.UUID
    ): DomainEvent {
        return this.copy(
            knowledgeBaseId = KnowledgeBaseId(aggregateId)
        )
    }
}

/**
 * 文档处理完成事件
 */
data class DocumentProcessingCompletedEvent(
    val knowledgeBaseId: KnowledgeBaseId,
    val documentId: DocumentId,
    val userId: UserId,
    val title: String,
    val totalProcessingTimeMs: Long,
    val isSuccessful: Boolean,
    val errorMessage: String? = null,
    override val aggregateId: String = knowledgeBaseId.value,
    override val version: Long = 1L
) : DomainEvent() {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: java.util.UUID
    ): DomainEvent {
        return this.copy(
            knowledgeBaseId = KnowledgeBaseId(aggregateId)
        )
    }
}