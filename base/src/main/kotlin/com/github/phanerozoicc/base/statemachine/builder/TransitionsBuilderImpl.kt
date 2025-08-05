package com.github.phanerozoicc.base.statemachine.builder

import com.github.phanerozoicc.statemachine.Action
import com.github.phanerozoicc.statemachine.Condition
import com.github.phanerozoicc.statemachine.State
import com.github.phanerozoicc.statemachine.Transition
import com.github.phanerozoicc.statemachine.impl.StateHelper
import com.github.phanerozoicc.statemachine.impl.TransitionType
import java.util.concurrent.ConcurrentHashMap

class TransitionsBuilderImpl<S, E, C>(
    stateMap: ConcurrentHashMap<S, State<S, E, C>>, external: TransitionType
): AbstractTransitionBuilder<S, E, C>(stateMap, external),
    ExternalTransitionsBuilder<S, E, C>
{
    private val sources = mutableListOf<State<S, E, C>>()
    private val transitions = mutableListOf<Transition<S, E, C>>()

    /**
     * 将多个状态作为源状态添加到当前构建器中。
     */
    override fun fromAmong(vararg stateIds: S) {
        sources.addAll(stateIds.map { StateHelper.getState(stateMap, it) })
    }

    override fun on(event: E): On<S, E, C> {
        sources.forEach {
            val transition = it.addTransition(event, target, transitionType)
            transitions.add(transition)  // 添加到转换列表中
        }
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