package com.lifee.chat.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.chat.domain.valueobjects.*
import com.lifee.common.domain.valueobjects.UserId
import java.time.Instant

/**
 * 消息更新事件
 */
data class MessageUpdatedEvent(
    val conversationId: ConversationId,
    val messageId: MessageId,
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
         * 创建消息更新事件
         */
        fun create(
            conversationId: ConversationId,
            messageId: MessageId,
            userId: UserId
        ): MessageUpdatedEvent {
            return MessageUpdatedEvent(
                conversationId = conversationId,
                messageId = messageId,
                userId = userId,
                updatedAt = Instant.now()
            )
        }
    }
}