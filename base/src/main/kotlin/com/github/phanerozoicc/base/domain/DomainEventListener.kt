package com.github.phanerozoicc.base.domain

interface DomainEventListener {
    fun onDomainEvent(event: DomainEvent)
}