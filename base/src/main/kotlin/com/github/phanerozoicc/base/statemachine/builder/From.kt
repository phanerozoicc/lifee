package com.github.phanerozoicc.base.statemachine.builder

interface From<S, E, C> {
    fun to(state: S): To<S, E, C>
}
