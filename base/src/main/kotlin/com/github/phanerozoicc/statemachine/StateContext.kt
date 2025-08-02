package com.github.phanerozoicc.statemachine

interface StateContext<S, E, C> {

    fun getTransition(): Transition<S, E, C>

    fun getStateMachine(): StateMachine<S, E, C>
}