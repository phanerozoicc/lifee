package com.github.phanerozoicc.base.statemachine.builder

import com.github.phanerozoicc.statemachine.State
import com.github.phanerozoicc.statemachine.StateMachine
import com.github.phanerozoicc.statemachine.StateMachineFactory
import com.github.phanerozoicc.statemachine.Transition
import com.github.phanerozoicc.statemachine.impl.StateMachineImpl
import com.github.phanerozoicc.statemachine.impl.TransitionType
import java.util.concurrent.ConcurrentHashMap

class StateMachineBuilderImpl<S, E, C>: StateMachineBuilder<S ,E ,C> {

    private val stateMap = ConcurrentHashMap<S, State<S, E, C>>()
    private val stateMachine = StateMachineImpl(stateMap)
    override var failCallback: FailCallback<S, E, C> = NumbFailCallBack()

    /**
     * 构建单个原状态的转换
     */
    override fun externalTransition(): ExternalTransitionBuilder<S, E, C> {
        return TransitionBuilderImpl(stateMap, TransitionType.EXTERNAL)
    }

    /**
     * 构建多个原状态的转换
     */
    override fun externalTransitions(): ExternalTransitionsBuilder<S, E, C> {
        return TransitionsBuilderImpl(stateMap, TransitionType.EXTERNAL)
    }


    override fun externalParallelTransition(transition: Transition<S, E, C>): ExternalParallelTransitionBuilder<S, E, C> {
        return ParallelTransitionBuilderImpl(stateMap, TransitionType.EXTERNAL)
    }

    /**
     * 构建状态内部转换
     */
    override fun internalTransition(): InternalTransitionBuilder<S, E, C> {
        return TransitionBuilderImpl(stateMap, TransitionType.INTERNAL)
    }


    override fun build(machineId: String): StateMachine<S, E, C> {
        return stateMachine.apply {
            require(machineId.isNotBlank()) { "Machine ID cannot be blank" }
            check(!ready) { "StateMachine already built" }
            this.machineId = machineId
            ready = true
            failCallback = this@StateMachineBuilderImpl.failCallback
        }.also {
            StateMachineFactory.register(it)
        }
    }
}