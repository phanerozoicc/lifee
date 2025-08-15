package com.github.phanerozoicc.base.event

import java.time.Instant
import java.util.*

abstract class DomainEvent(
    open val aggregateId: String,
    open val eventType: String,
    open val eventId: String = UUID.randomUUID().toString(),
    open val occurredOn: Instant = Instant.now(),
): Event


interface Event