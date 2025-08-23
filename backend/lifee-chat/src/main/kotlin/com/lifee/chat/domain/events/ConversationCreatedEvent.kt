package com.lifee.chat.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.chat.domain.valueobjects.ConversationTitle
import com.lifee.common.domain.valueobjects.UserId
import java.time.Instant

/**
 * 对话创建事件
 */
data class ConversationCreatedEvent(
    val conversationId: ConversationId,
    val title: ConversationTitle,
    val userId: UserId,
    val createdAt: Instant,
    override val aggregateId: String = conversationId.value.toString(),
    override val version: Long = 0,
    override val occurredOn: Instant = Instant.now(),
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
         * 创建对话创建事件
         */
        fun create(
            conversationId: ConversationId,
            title: ConversationTitle,
            userId: UserId
        ): ConversationCreatedEvent {
            return ConversationCreatedEvent(
                conversationId = conversationId,
                title = title,
                userId = userId,
                createdAt = Instant.now()
            )
        }
    }
}