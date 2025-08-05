package com.github.phanerozoicc.base.statemachine.builder

import com.github.phanerozoicc.statemachine.StateMachine
import com.github.phanerozoicc.statemachine.Transition

interface StateMachineBuilder<S, E, C> {

    var failCallback: FailCallback<S, E, C>

    fun externalTransition(): ExternalTransitionBuilder<S, E, C>

    fun externalTransitions(): ExternalTransitionsBuilder<S, E, C>

    fun externalParallelTransition(transition: Transition<S, E, C>): ExternalParallelTransitionBuilder<S, E, C>

    fun internalTransition(): InternalTransitionBuilder<S, E, C>

    fun build(machineId: String): StateMachine<S, E, C>
}