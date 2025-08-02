package com.github.phanerozoicc.statemachine

import com.github.phanerozoicc.statemachine.impl.TransitionType

interface State<S, E, C>: Visitable {
    /**
     * 状态唯一标识符
     */
    val id: S

    /**
     * 添加状态流转
     */
    fun addTransition(event: E, target: State<S, E, C>, transitionType: TransitionType): Transition<S, E, C>

    fun addTransitions(event: E, targets: List<State<S, E, C>>, transitionType: TransitionType): List<Transition<S, E, C>>

    fun getEventTransitions(event: E): List<Transition<S, E, C>>

    fun getAllTransitions(): Collection<Transition<S, E, C>>

}