package com.github.phanerozoicc.base.event

interface EventHandler<E: Event, R> {
    fun onEvent(event: E): R
}