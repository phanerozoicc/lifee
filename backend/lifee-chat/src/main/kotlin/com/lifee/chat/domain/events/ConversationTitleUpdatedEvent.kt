package com.lifee.chat.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.chat.domain.valueobjects.*
import com.lifee.common.domain.valueobjects.UserId
import java.time.Instant

/**
 * 对话标题更新事件
 */
data class ConversationTitleUpdatedEvent(
    val conversationId: ConversationId,
    val oldTitle: ConversationTitle,
    val newTitle: ConversationTitle,
    val userId: UserId,
    val updatedAt: Instant,
    override val aggregateId: String = conversationId.value.toString(),
    override val version: Long = 0,
    override val occurredOn: Instant = updatedAt,
    override val eventId: java.util.UUID = java.util.UUID.randomUUID()
) : DomainEvent(aggregateId, version, occurredOn, eventId) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: java.util.UUID
    ): DomainEvent {
        return copy(
            aggregateId = aggregateId,
            version = version,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
    
    companion object {
        /**
         * 创建对话标题更新事件
         */
        fun create(
            conversationId: ConversationId,
            oldTitle: ConversationTitle,
            newTitle: ConversationTitle,
            userId: UserId
        ): ConversationTitleUpdatedEvent {
            return ConversationTitleUpdatedEvent(
                conversationId = conversationId,
                oldTitle = oldTitle,
                newTitle = newTitle,
                userId = userId,
                updatedAt = Instant.now()
            )
        }
    }
}