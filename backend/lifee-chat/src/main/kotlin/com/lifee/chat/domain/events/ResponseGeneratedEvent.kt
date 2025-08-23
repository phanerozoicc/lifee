package com.lifee.chat.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.common.domain.valueobjects.UserId
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
    override val aggregateId: String = conversationId.value.toString(),
    override val version: Long = 1L,
    override val occurredOn: Instant = Instant.now(),
    override val eventId: UUID = UUID.randomUUID()
) : DomainEvent(aggregateId, version, occurredOn, eventId) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return copy(
            aggregateId = aggregateId,
            version = version,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
}