package com.github.phanerozoicc.statemachine.builder

import com.github.phanerozoicc.statemachine.Action

interface When<S, E, C> {
    fun perform(action: Action<S, E, C>)
}