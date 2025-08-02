package com.github.phanerozoicc.domain

interface DomainEventPublisher {
    fun publish(event: DomainEvent)
}