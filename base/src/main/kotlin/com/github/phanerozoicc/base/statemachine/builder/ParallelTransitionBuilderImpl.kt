package com.github.phanerozoicc.base.statemachine.builder

import com.github.phanerozoicc.statemachine.Action
import com.github.phanerozoicc.statemachine.Condition
import com.github.phanerozoicc.statemachine.State
import com.github.phanerozoicc.statemachine.Transition
import com.github.phanerozoicc.statemachine.impl.StateHelper
import com.github.phanerozoicc.statemachine.impl.TransitionType

class ParallelTransitionBuilderImpl<S, E, C>(
    stateMap: MutableMap<S, State<S, E, C>>,
    transitionType: TransitionType
): AbstractParallelTransitionBuilder<S, E, C>(stateMap, transitionType), ExternalParallelTransitionBuilder<S, E, C>
{
    private lateinit var source: State<S, E, C>
    private lateinit var transitions: List<Transition<S, E, C>>

    override fun from(stateId: S): ParallelFrom<S, E, C> {
        source = StateHelper.getState(stateMap, stateId)
        return this
    }

    override fun on(event: E): On<S, E, C> {
        transitions = source.addTransitions(event, targets, transitionType)
        return this
    }

    override fun `when`(condition: Condition<C>): When<S, E, C> {
        transitions.forEach { it.condition = condition }
        return this
    }

    override fun perform(action: Action<S, E, C>) {
        transitions.forEach { it.action = action }
    }



}