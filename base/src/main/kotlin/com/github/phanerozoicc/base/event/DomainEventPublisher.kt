package com.github.phanerozoicc.base.event

interface DomainEventPublisher {
    fun publish(event: Event)
}