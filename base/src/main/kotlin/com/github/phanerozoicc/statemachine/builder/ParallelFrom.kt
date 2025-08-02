package com.github.phanerozoicc.statemachine.builder

interface ParallelFrom<S, E, C> {
    fun toAmong(vararg stateIds: S): To<S, E, C>
}