package com.lifee.knowledge.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.common.domain.valueobjects.UserId
import java.time.Instant

/**
 * 默认知识库创建完成事件
 */
data class DefaultKnowledgeBaseCreatedEvent(
    val userId: UserId,
    val knowledgeBaseId: String,
    val knowledgeBaseName: String,
    val email: String,
    val firstName: String?,
    val lastName: String?,
    val createdAt: Instant = Instant.now(),
    override val aggregateId: String = userId.value,
    override val version: Long = 1L
) : DomainEvent(aggregateId, version) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: java.util.UUID
    ): DomainEvent {
        return DefaultKnowledgeBaseCreatedEvent(
            userId = this.userId,
            knowledgeBaseId = this.knowledgeBaseId,
            knowledgeBaseName = this.knowledgeBaseName,
            email = this.email,
            firstName = this.firstName,
            lastName = this.lastName,
            createdAt = this.createdAt,
            aggregateId = aggregateId,
            version = version
        )
    }
}