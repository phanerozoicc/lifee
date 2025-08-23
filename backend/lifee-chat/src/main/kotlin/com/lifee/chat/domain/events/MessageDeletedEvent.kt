package com.lifee.chat.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.chat.domain.valueobjects.*
import com.lifee.common.domain.valueobjects.UserId
import java.time.Instant

/**
 * 消息删除事件
 */
data class MessageDeletedEvent(
    val conversationId: ConversationId,
    val messageId: MessageId,
    val userId: UserId,
    val deletedAt: Instant,
    override val aggregateId: String = conversationId.value.toString(),
    override val version: Long = 0,
    override val occurredOn: Instant = deletedAt,
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
         * 创建消息删除事件
         */
        fun create(
            conversationId: ConversationId,
            messageId: MessageId,
            userId: UserId
        ): MessageDeletedEvent {
            return MessageDeletedEvent(
                conversationId = conversationId,
                messageId = messageId,
                userId = userId,
                deletedAt = Instant.now()
            )
        }
    }
}