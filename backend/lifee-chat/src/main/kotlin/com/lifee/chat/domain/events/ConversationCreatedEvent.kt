package com.lifee.chat.domain.events

import com.lifee.common.cqrs.events.DomainEvent
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.chat.domain.valueobjects.ConversationTitle
import com.lifee.chat.domain.valueobjects.UserId
import java.time.Instant

/**
 * 对话创建事件
 */
data class ConversationCreatedEvent(
    val conversationId: ConversationId,
    val title: ConversationTitle,
    val userId: UserId,
    val createdAt: Instant
) : DomainEvent {
    
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