package com.github.phanerozoicc.statemachine.builder

interface ExternalTransitionsBuilder<S, E, C> {
    fun fromAmong(vararg stateIds: S)
}
