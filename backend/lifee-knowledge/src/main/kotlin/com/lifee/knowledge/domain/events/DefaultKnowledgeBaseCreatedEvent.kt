package com.lifee.knowledge.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
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
) : DomainEvent() {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: java.util.UUID
    ): DomainEvent {
        return this.copy(
            userId = UserId(aggregateId)
        )
    }
}