package com.github.phanerozoicc.statemachine.builder

import com.github.phanerozoicc.statemachine.State
import com.github.phanerozoicc.statemachine.impl.StateHelper
import com.github.phanerozoicc.statemachine.impl.TransitionType

abstract class AbstractParallelTransitionBuilder<S, E, C>(
    val stateMap: MutableMap<S, State<S, E, C>>,
    val transitionType: TransitionType
): ParallelFrom<S, E, C>, On<S, E, C>, To<S, E, C>
{
    protected lateinit var targets: List<State<S, E, C>>

    override fun toAmong(vararg stateIds: S): To<S, E, C> {
        targets = StateHelper.getStates(stateMap, *stateIds)
        return this
    }
}