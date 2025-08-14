package com.github.phanerozoicc.base.event

interface DomainEventHandler<E: DomainEvent, R> {
    fun onDomainEvent(event: E): R
}