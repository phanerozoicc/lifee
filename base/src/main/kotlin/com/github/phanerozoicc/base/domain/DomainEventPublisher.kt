package com.github.phanerozoicc.base.domain

interface DomainEventPublisher {
    fun publish(event: DomainEvent)
}