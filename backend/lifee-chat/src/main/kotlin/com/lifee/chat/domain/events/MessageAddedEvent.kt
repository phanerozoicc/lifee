package com.lifee.chat.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.chat.domain.valueobjects.*
import com.lifee.common.domain.valueobjects.UserId
import java.time.Instant

/**
 * 消息添加事件
 */
data class MessageAddedEvent(
    val conversationId: ConversationId,
    val messageId: MessageId,
    val messageType: MessageType,
    val userId: UserId,
    val addedAt: Instant,
    override val aggregateId: String = conversationId.value.toString(),
    override val version: Long = 0,
    override val occurredOn: Instant = addedAt,
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
         * 创建消息添加事件
         */
        fun create(
            conversationId: ConversationId,
            messageId: MessageId,
            messageType: MessageType,
            userId: UserId
        ): MessageAddedEvent {
            return MessageAddedEvent(
                conversationId = conversationId,
                messageId = messageId,
                messageType = messageType,
                userId = userId,
                addedAt = Instant.now()
            )
        }
    }
}