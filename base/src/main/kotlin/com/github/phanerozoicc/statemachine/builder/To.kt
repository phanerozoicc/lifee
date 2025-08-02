package com.github.phanerozoicc.statemachine.builder

interface To<S, E, C> {
    fun on(event: E): On<S, E, C>
}