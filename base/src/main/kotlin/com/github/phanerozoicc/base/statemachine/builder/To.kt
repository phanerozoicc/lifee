package com.github.phanerozoicc.base.statemachine.builder

interface To<S, E, C> {
    fun on(event: E): On<S, E, C>
}