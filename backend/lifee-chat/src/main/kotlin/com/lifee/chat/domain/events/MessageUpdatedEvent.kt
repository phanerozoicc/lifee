package com.lifee.chat.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.chat.domain.valueobjects.*
import java.time.Instant

/**
 * 消息更新事件
 */
data class MessageUpdatedEvent(
    val conversationId: ConversationId,
    val messageId: MessageId,
    val userId: UserId,
    val updatedAt: Instant
) : DomainEvent(
    aggregateId = conversationId.value,
    occurredOn = updatedAt
) {
    
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