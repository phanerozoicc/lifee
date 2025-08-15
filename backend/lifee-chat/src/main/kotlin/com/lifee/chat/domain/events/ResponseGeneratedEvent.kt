package com.lifee.chat.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 响应生成事件
 */
data class ResponseGeneratedEvent(
    val conversationId: ConversationId,
    val userId: UserId,
    val userMessage: String,
    val assistantResponse: String,
    val retrievedDocumentCount: Int,
    val tokensUsed: Int,
    val processingTimeMs: Long,
    override val aggregateId: String = conversationId.value,
    override val version: Long = 1L
) : DomainEvent() {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant
    ): DomainEvent {
        return copy(
            aggregateId = aggregateId,
            version = version
        )
    }
}