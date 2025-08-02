package com.github.phanerozoicc.statemachine

/**
 * 动作
 */
interface Action<S, E, C> {

    /**
     * execute action with stateContent
     */
    fun execute(from: S, to: S, event: E, context: C)
}