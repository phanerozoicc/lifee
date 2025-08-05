package com.github.phanerozoicc.base.statemachine.builder

interface ExternalTransitionBuilder<S, E, C> {
    fun from(stateId: S): From<S, E, C>
}
