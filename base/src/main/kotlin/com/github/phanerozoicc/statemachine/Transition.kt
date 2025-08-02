package com.github.phanerozoicc.statemachine

import com.github.phanerozoicc.statemachine.impl.TransitionType

interface Transition<S, E, C> {

    var source: State<S, E, C>
    var target: State<S, E, C>
    var event: E
    var type: TransitionType
    var condition: Condition<C>?
    var action: Action<S, E, C>?

    fun transit(context: C, checkCondition: Boolean): State<S, E, C>

    fun verify()
}