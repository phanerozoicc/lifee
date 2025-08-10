package com.lifee.common.cqrs.events

interface EventHandler<T : Event> {
    fun handle(event: T)
}