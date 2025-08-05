package com.github.phanerozoicc.base.statemachine.builder

class StateMachineBuilderFactory {
    companion object {
        fun <S, E, C> create(): StateMachineBuilder<S, E, C> = StateMachineBuilderImpl()
    }
}
