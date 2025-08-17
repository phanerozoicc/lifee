package com.lifee.knowledge.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.knowledge.domain.valueobjects.DocumentId
import java.time.Instant
import java.util.*

/**
 * 文档删除事件
 */
class DocumentRemovedEvent(
    val knowledgeBaseId: KnowledgeBaseId,
    val documentId: DocumentId,
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
        return DocumentRemovedEvent(
            knowledgeBaseId = KnowledgeBaseId.fromString(aggregateId),
            documentId = this.documentId,
            aggregateId = aggregateId,
            version = version,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DocumentRemovedEvent) return false
        if (!super.equals(other)) return false
        return knowledgeBaseId == other.knowledgeBaseId &&
               documentId == other.documentId
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + knowledgeBaseId.hashCode()
        result = 31 * result + documentId.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "DocumentRemovedEvent(knowledgeBaseId=$knowledgeBaseId, documentId=$documentId, ${super.toString()})"
    }
    
    companion object {
        fun create(
            knowledgeBaseId: KnowledgeBaseId,
            documentId: DocumentId
        ): DocumentRemovedEvent {
            return DocumentRemovedEvent(
                knowledgeBaseId = knowledgeBaseId,
                documentId = documentId
            )
        }
    }
}