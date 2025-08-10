package com.lifee.chat.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.chat.domain.valueobjects.*
import java.time.Instant

/**
 * 消息添加事件
 */
data class MessageAddedEvent(
    val conversationId: ConversationId,
    val messageId: MessageId,
    val messageType: MessageType,
    val userId: UserId,
    val addedAt: Instant
) : DomainEvent(
    aggregateId = conversationId.value,
    occurredOn = addedAt
) {
    
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