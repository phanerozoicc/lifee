package com.github.phanerozoicc.statemachine.builder

interface InternalTransitionBuilder<S, E, C> {
    fun within(stateId: S): To<S, E, C>
}
