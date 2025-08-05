package com.github.phanerozoicc.base.statemachine.builder

interface ExternalTransitionsBuilder<S, E, C> {
    fun fromAmong(vararg stateIds: S)
}
