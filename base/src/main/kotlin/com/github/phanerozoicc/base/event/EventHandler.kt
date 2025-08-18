package com.github.phanerozoicc.base.event

interface EventHandler<E: Event, R> {
    fun onDomainEvent(event: E): R
}