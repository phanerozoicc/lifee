package com.github.phanerozoicc.base.event

import java.time.Instant

interface DomainEvent {
    val eventId: String
    val occurredOn: Instant
    val eventType: String
}