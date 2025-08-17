package com.lifee.knowledge.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseName
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseDescription
import com.lifee.common.domain.valueobjects.UserId
import java.time.Instant
import java.util.*

/**
 * 知识库创建事件
 */
class KnowledgeBaseCreatedEvent(
    val knowledgeBaseId: KnowledgeBaseId,
    val name: KnowledgeBaseName,
    val description: KnowledgeBaseDescription,
    val ownerId: UserId
) : DomainEvent(knowledgeBaseId.toString()) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return KnowledgeBaseCreatedEvent(
            knowledgeBaseId = KnowledgeBaseId.fromString(aggregateId),
            name = this.name,
            description = this.description,
            ownerId = this.ownerId
        )
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is KnowledgeBaseCreatedEvent) return false
        if (!super.equals(other)) return false
        return knowledgeBaseId == other.knowledgeBaseId &&
               name == other.name &&
               description == other.description &&
               ownerId == other.ownerId
    }
    
    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + knowledgeBaseId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + ownerId.hashCode()
        return result
    }
    
    override fun toString(): String {
        return "KnowledgeBaseCreatedEvent(knowledgeBaseId=$knowledgeBaseId, name=$name, description=$description, ownerId=$ownerId, ${super.toString()})"
    }
}