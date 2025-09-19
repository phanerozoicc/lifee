package com.github.phanerozoicc.chat.application.event

import com.github.phanerozoicc.base.event.DomainEvent
import java.time.Instant
import java.util.*

/**
 *
 */
class ConversationCreatedEvent(
    val conversationId: String,
    val userId: String,
    version: Long = 0,
    eventId: String = UUID.randomUUID().toString(),
    occurredOn: Instant = Instant.now()
): DomainEvent(conversationId, version, eventId, occurredOn) {
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: String
    ): DomainEvent {
        return ConversationCreatedEvent(
            conversationId,
            userId,
            version,
            eventId,
            occurredOn
        )
    }
}
