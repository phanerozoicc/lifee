package com.github.phanerozoicc.base.domain

import java.time.Instant

interface DomainEvent {
    val eventId: String
    val occurredOn: Instant
    val eventType: String
}