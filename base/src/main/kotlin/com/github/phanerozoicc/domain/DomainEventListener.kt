package com.github.phanerozoicc.domain

interface DomainEventListener {
    fun onDomainEvent(event: DomainEvent)
}