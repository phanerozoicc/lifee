package com.lifee.knowledge.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.knowledge.domain.valueobjects.*
import java.time.Instant
import java.util.*

/**
 * 文档更新事件
 */
data class DocumentUpdatedEvent(
    override val aggregateId: String,
    override val eventId: UUID,
    override val occurredOn: Instant,
    override val version: Long,
    val knowledgeBaseId: KnowledgeBaseId,
    val documentId: DocumentId,
    val newTitle: DocumentTitle,
    val newContentLength: Int
) : DomainEvent {
    
    companion object {
        fun create(
            knowledgeBaseId: KnowledgeBaseId,
            documentId: DocumentId,
            newTitle: DocumentTitle,
            newContentLength: Int,
            version: Long
        ): DocumentUpdatedEvent {
            return DocumentUpdatedEvent(
                aggregateId = knowledgeBaseId.toString(),
                eventId = UUID.randomUUID(),
                occurredOn = Instant.now(),
                version = version,
                knowledgeBaseId = knowledgeBaseId,
                documentId = documentId,
                newTitle = newTitle,
                newContentLength = newContentLength
            )
        }
    }
}