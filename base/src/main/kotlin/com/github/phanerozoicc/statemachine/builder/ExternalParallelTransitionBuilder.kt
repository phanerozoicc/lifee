package com.github.phanerozoicc.statemachine.builder

interface ExternalParallelTransitionBuilder<S, E, C> {

    fun from(stateId: S): ParallelFrom<S, E, C>
}
