package com.github.phanerozoicc.statemachine.builder

import com.github.phanerozoicc.statemachine.Action
import com.github.phanerozoicc.statemachine.Condition
import com.github.phanerozoicc.statemachine.State
import com.github.phanerozoicc.statemachine.Transition
import com.github.phanerozoicc.statemachine.impl.StateHelper
import com.github.phanerozoicc.statemachine.impl.TransitionType
import java.util.concurrent.ConcurrentHashMap

class TransitionBuilderImpl<S, E, C>(
    stateMap: ConcurrentHashMap<S, State<S, E, C>>, external: TransitionType
) : AbstractTransitionBuilder<S, E, C>(stateMap, external),
    ExternalTransitionBuilder<S, E, C>,
    InternalTransitionBuilder<S, E, C>
{
    private lateinit var source: State<S, E, C>
    private lateinit var transition: Transition<S, E, C>

    /**
     * 初始状态
     */
    override fun from(stateId: S): From<S, E, C> {
        source = StateHelper.getState(stateMap, stateId)
        return this
    }

    /**
     * 内部状态流转
     */
    override fun within(stateId: S): To<S, E, C> {
        source = StateHelper.getState(stateMap, stateId)
        target = source
        return this
    }
    /**
     * 关联到目标状态的事件
     */
    override fun on(event: E): On<S, E, C> {
        source.addTransition(event, target, transitionType)
        return this
    }

    /**
     * 为转换添加条件
     */
    override fun `when`(condition: Condition<C>): When<S, E, C> {
        transition.condition = condition
        return this
    }

    /**
     * 为转换添加动作
     */
    override fun perform(action: Action<S, E, C>) {
        transition.action = action
    }

}