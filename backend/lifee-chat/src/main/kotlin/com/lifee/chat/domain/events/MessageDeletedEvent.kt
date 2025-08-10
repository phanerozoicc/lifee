package com.lifee.chat.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.chat.domain.valueobjects.*
import java.time.Instant

/**
 * 消息删除事件
 */
data class MessageDeletedEvent(
    val conversationId: ConversationId,
    val messageId: MessageId,
    val userId: UserId,
    val deletedAt: Instant
) : DomainEvent(
    aggregateId = conversationId.value,
    occurredOn = deletedAt
) {
    
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