package com.lifee.knowledge.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.knowledge.domain.valueobjects.*
import java.time.Instant
import java.util.*

/**
 * 文档更新事件
 */
class DocumentUpdatedEvent(
    val knowledgeBaseId: KnowledgeBaseId,
    val documentId: DocumentId,
    val newTitle: DocumentTitle,
    val newContentLength: Int,
    aggregateId: String = knowledgeBaseId.toString(),
    version: Long = 0,
    occurredOn: Instant = Instant.now(),
    eventId: UUID = UUID.randomUUID()
) : DomainEvent(aggregateId, version, occurredOn, eventId) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return DocumentUpdatedEvent(
            knowledgeBaseId = KnowledgeBaseId.fromString(aggregateId),
            documentId = this.documentId,
            newTitle = this.newTitle,
            newContentLength = this.newContentLength,
            aggregateId = aggregateId,
            version = version,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentUpdatedEvent) return false
        if (!super.equals(other)) return false
        return knowledgeBaseId == other.knowledgeBaseId &&
               documentId == other.documentId &&
               newTitle == other.newTitle &&
               newContentLength == other.newContentLength
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + knowledgeBaseId.hashCode()
        result = 31 * result + documentId.hashCode()
        result = 31 * result + newTitle.hashCode()
        result = 31 * result + newContentLength
        return result
    }
    
    override fun toString(): String {
        return "DocumentUpdatedEvent(knowledgeBaseId=$knowledgeBaseId, documentId=$documentId, newTitle=$newTitle, newContentLength=$newContentLength, ${super.toString()})"
    }
    
    companion object {
        fun create(
            knowledgeBaseId: KnowledgeBaseId,
            documentId: DocumentId,
            newTitle: DocumentTitle,
            newContentLength: Int
        ): DocumentUpdatedEvent {
            return DocumentUpdatedEvent(
                knowledgeBaseId = knowledgeBaseId,
                documentId = documentId,
                newTitle = newTitle,
                newContentLength = newContentLength
            )
        }
    }
}