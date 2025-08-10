package com.lifee.knowledge.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.knowledge.domain.valueobjects.DocumentId
import java.time.Instant
import java.util.*

/**
 * 文档删除事件
 */
data class DocumentRemovedEvent(
    override val aggregateId: String,
    override val eventId: UUID,
    override val occurredOn: Instant,
    override val version: Long,
    val knowledgeBaseId: KnowledgeBaseId,
    val documentId: DocumentId
) : DomainEvent {
    
    companion object {
        fun create(
            knowledgeBaseId: KnowledgeBaseId,
            documentId: DocumentId,
            version: Long
        ): DocumentRemovedEvent {
            return DocumentRemovedEvent(
                aggregateId = knowledgeBaseId.toString(),
                eventId = UUID.randomUUID(),
                occurredOn = Instant.now(),
                version = version,
                knowledgeBaseId = knowledgeBaseId,
                documentId = documentId
            )
        }
    }
}