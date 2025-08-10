package com.lifee.knowledge.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseName
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseDescription
import com.lifee.knowledge.domain.valueobjects.UserId
import java.time.Instant
import java.util.*

/**
 * 知识库创建事件
 */
data class KnowledgeBaseCreatedEvent(
    override val aggregateId: String,
    override val eventId: UUID,
    override val occurredOn: Instant,
    override val version: Long,
    val knowledgeBaseId: KnowledgeBaseId,
    val name: KnowledgeBaseName,
    val description: KnowledgeBaseDescription,
    val ownerId: UserId
) : DomainEvent {
    
    companion object {
        fun create(
            knowledgeBaseId: KnowledgeBaseId,
            name: KnowledgeBaseName,
            description: KnowledgeBaseDescription,
            ownerId: UserId,
            version: Long = 1
        ): KnowledgeBaseCreatedEvent {
            return KnowledgeBaseCreatedEvent(
                aggregateId = knowledgeBaseId.toString(),
                eventId = UUID.randomUUID(),
                occurredOn = Instant.now(),
                version = version,
                knowledgeBaseId = knowledgeBaseId,
                name = name,
                description = description,
                ownerId = ownerId
            )
        }
    }
}