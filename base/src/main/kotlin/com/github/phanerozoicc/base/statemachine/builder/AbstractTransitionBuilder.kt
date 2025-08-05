package com.github.phanerozoicc.base.statemachine.builder

import com.github.phanerozoicc.statemachine.State
import com.github.phanerozoicc.statemachine.impl.StateHelper
import com.github.phanerozoicc.statemachine.impl.TransitionType

abstract class AbstractTransitionBuilder<S, E, C>(
  val stateMap: MutableMap<S, State<S, E, C>>,
  val transitionType: TransitionType
) : From<S, E, C>, To<S, E, C>, On<S, E, C> {

    // 目标状态
    protected lateinit var target: State<S, E, C>

    override fun to(state: S): To<S, E, C> {
        target = StateHelper.getState(stateMap, state)
        return this
    }
}
