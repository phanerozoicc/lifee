package com.lifee.knowledge.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.knowledge.domain.valueobjects.DocumentId
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.knowledge.domain.valueobjects.DocumentTitle
import com.lifee.knowledge.domain.valueobjects.DocumentType
import com.lifee.knowledge.domain.valueobjects.DocumentContent
import com.lifee.common.domain.valueobjects.UserId
import java.time.Instant

/**
 * 文档添加事件
 */
class DocumentAddedEvent(
    val knowledgeBaseId: KnowledgeBaseId,
    val documentId: DocumentId,
    val userId: UserId,
    val title: DocumentTitle,
    val content: DocumentContent,
    val type: DocumentType,
    val contentLength: Int,
    aggregateId: String = knowledgeBaseId.toString(),
    version: Long = 0,
    occurredOn: Instant = Instant.now(),
    eventId: java.util.UUID = java.util.UUID.randomUUID()
) : DomainEvent(aggregateId, version, occurredOn, eventId) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: java.util.UUID
    ): DomainEvent {
        return DocumentAddedEvent(
            knowledgeBaseId = KnowledgeBaseId.fromString(aggregateId),
            documentId = this.documentId,
            userId = this.userId,
            title = this.title,
            content = this.content,
            type = this.type,
            contentLength = this.contentLength,
            aggregateId = aggregateId,
            version = version,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentAddedEvent) return false
        if (!super.equals(other)) return false
        return knowledgeBaseId == other.knowledgeBaseId &&
               documentId == other.documentId &&
               userId == other.userId &&
               title == other.title &&
               content == other.content &&
               type == other.type &&
               contentLength == other.contentLength
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + knowledgeBaseId.hashCode()
        result = 31 * result + documentId.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + content.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + contentLength
        return result
    }
    
    override fun toString(): String {
        return "DocumentAddedEvent(knowledgeBaseId=$knowledgeBaseId, documentId=$documentId, userId=$userId, title='$title', type='$type', contentLength=$contentLength, ${super.toString()})"
    }
}

/**
 * 文档向量化完成事件
 */
class DocumentVectorizedEvent(
    val knowledgeBaseId: KnowledgeBaseId,
    val documentId: DocumentId,
    val userId: UserId,
    val vectorDimension: Int,
    val chunkCount: Int,
    val processingTimeMs: Long,
    aggregateId: String = knowledgeBaseId.toString(),
    version: Long = 0,
    occurredOn: Instant = Instant.now(),
    eventId: java.util.UUID = java.util.UUID.randomUUID()
) : DomainEvent(aggregateId, version, occurredOn, eventId) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: java.util.UUID
    ): DomainEvent {
        return DocumentVectorizedEvent(
            knowledgeBaseId = KnowledgeBaseId.fromString(aggregateId),
            documentId = this.documentId,
            userId = this.userId,
            vectorDimension = this.vectorDimension,
            chunkCount = this.chunkCount,
            processingTimeMs = this.processingTimeMs,
            aggregateId = aggregateId,
            version = version,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentVectorizedEvent) return false
        if (!super.equals(other)) return false
        return knowledgeBaseId == other.knowledgeBaseId &&
               documentId == other.documentId &&
               userId == other.userId &&
               vectorDimension == other.vectorDimension &&
               chunkCount == other.chunkCount &&
               processingTimeMs == other.processingTimeMs
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + knowledgeBaseId.hashCode()
        result = 31 * result + documentId.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + vectorDimension
        result = 31 * result + chunkCount
        result = 31 * result + processingTimeMs.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "DocumentVectorizedEvent(knowledgeBaseId=$knowledgeBaseId, documentId=$documentId, userId=$userId, vectorDimension=$vectorDimension, chunkCount=$chunkCount, processingTimeMs=$processingTimeMs, ${super.toString()})"
    }
}

/**
 * 文档索引构建完成事件
 */
class DocumentIndexedEvent(
    val knowledgeBaseId: KnowledgeBaseId,
    val documentId: DocumentId,
    val userId: UserId,
    val indexType: String,
    val indexSize: Long,
    val processingTimeMs: Long,
    aggregateId: String = knowledgeBaseId.toString(),
    version: Long = 0,
    occurredOn: Instant = Instant.now(),
    eventId: java.util.UUID = java.util.UUID.randomUUID()
) : DomainEvent(aggregateId, version, occurredOn, eventId) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: java.util.UUID
    ): DomainEvent {
        return DocumentIndexedEvent(
            knowledgeBaseId = KnowledgeBaseId.fromString(aggregateId),
            documentId = this.documentId,
            userId = this.userId,
            indexType = this.indexType,
            indexSize = this.indexSize,
            processingTimeMs = this.processingTimeMs,
            aggregateId = aggregateId,
            version = version,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentIndexedEvent) return false
        if (!super.equals(other)) return false
        return knowledgeBaseId == other.knowledgeBaseId &&
               documentId == other.documentId &&
               userId == other.userId &&
               indexType == other.indexType &&
               indexSize == other.indexSize &&
               processingTimeMs == other.processingTimeMs
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + knowledgeBaseId.hashCode()
        result = 31 * result + documentId.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + indexType.hashCode()
        result = 31 * result + indexSize.hashCode()
        result = 31 * result + processingTimeMs.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "DocumentIndexedEvent(knowledgeBaseId=$knowledgeBaseId, documentId=$documentId, userId=$userId, indexType='$indexType', indexSize=$indexSize, processingTimeMs=$processingTimeMs, ${super.toString()})"
    }
}

/**
 * 文档处理完成事件
 */
class DocumentProcessingCompletedEvent(
    val knowledgeBaseId: KnowledgeBaseId,
    val documentId: DocumentId,
    val userId: UserId,
    val totalProcessingTimeMs: Long,
    val success: Boolean,
    val errorMessage: String? = null,
    aggregateId: String = knowledgeBaseId.toString(),
    version: Long = 0,
    occurredOn: Instant = Instant.now(),
    eventId: java.util.UUID = java.util.UUID.randomUUID()
) : DomainEvent(aggregateId, version, occurredOn, eventId) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: java.util.UUID
    ): DomainEvent {
        return DocumentProcessingCompletedEvent(
            knowledgeBaseId = KnowledgeBaseId.fromString(aggregateId),
            documentId = this.documentId,
            userId = this.userId,
            totalProcessingTimeMs = this.totalProcessingTimeMs,
            success = this.success,
            errorMessage = this.errorMessage,
            aggregateId = aggregateId,
            version = version,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentProcessingCompletedEvent) return false
        if (!super.equals(other)) return false
        return knowledgeBaseId == other.knowledgeBaseId &&
               documentId == other.documentId &&
               userId == other.userId &&
               totalProcessingTimeMs == other.totalProcessingTimeMs &&
               success == other.success &&
               errorMessage == other.errorMessage
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + knowledgeBaseId.hashCode()
        result = 31 * result + documentId.hashCode()
        result = 31 * result + userId.hashCode()
        result = 31 * result + totalProcessingTimeMs.hashCode()
        result = 31 * result + success.hashCode()
        result = 31 * result + (errorMessage?.hashCode() ?: 0)
        return result
    }
    
    override fun toString(): String {
        return "DocumentProcessingCompletedEvent(knowledgeBaseId=$knowledgeBaseId, documentId=$documentId, userId=$userId, totalProcessingTimeMs=$totalProcessingTimeMs, success=$success, errorMessage='$errorMessage', ${super.toString()})"
    }
}