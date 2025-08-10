package com.lifee.knowledge.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.knowledge.domain.valueobjects.*
import java.time.Instant
import java.util.*

/**
 * 文档添加事件
 */
data class DocumentAddedEvent(
    override val aggregateId: String,
    override val eventId: UUID,
    override val occurredOn: Instant,
    override val version: Long,
    val knowledgeBaseId: KnowledgeBaseId,
    val documentId: DocumentId,
    val title: DocumentTitle,
    val type: DocumentType,
    val contentLength: Int
) : DomainEvent {
    
    companion object {
        fun create(
            knowledgeBaseId: KnowledgeBaseId,
            documentId: DocumentId,
            title: DocumentTitle,
            type: DocumentType,
            contentLength: Int,
            version: Long
        ): DocumentAddedEvent {
            return DocumentAddedEvent(
                aggregateId = knowledgeBaseId.toString(),
                eventId = UUID.randomUUID(),
                occurredOn = Instant.now(),
                version = version,
                knowledgeBaseId = knowledgeBaseId,
                documentId = documentId,
                title = title,
                type = type,
                contentLength = contentLength
            )
        }
    }
}