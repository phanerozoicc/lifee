package com.github.phanerozoicc.base.domain

import java.time.Instant
import java.util.*

interface DomainEvent {
    val eventId: String
    val occurredOn: Instant
    val eventType: String
}