package com.lifee.chat.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.chat.domain.valueobjects.*
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 对话标题更新事件
 */
data class ConversationTitleUpdatedEvent(
    val conversationId: ConversationId,
    val oldTitle: ConversationTitle,
    val newTitle: ConversationTitle,
    val userId: UserId,
    val updatedAt: Instant
) : DomainEvent(
    aggregateId = conversationId.value,
    occurredOn = updatedAt
) {
    
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