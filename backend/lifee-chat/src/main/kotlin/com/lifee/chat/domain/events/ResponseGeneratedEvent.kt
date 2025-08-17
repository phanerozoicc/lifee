package com.lifee.chat.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

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
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return ResponseGeneratedEvent(
            conversationId = ConversationId(aggregateId),
            userId = this.userId,
            userMessage = this.userMessage,
            assistantResponse = this.assistantResponse,
            retrievedDocumentCount = this.retrievedDocumentCount,
            tokensUsed = this.tokensUsed,
            processingTimeMs = this.processingTimeMs,
            aggregateId = aggregateId,
            version = version
        )
    }
}