package com.github.phanerozoicc.statemachine.impl

import com.github.phanerozoicc.statemachine.State
import com.github.phanerozoicc.statemachine.StateMachine
import com.github.phanerozoicc.statemachine.Transition
import com.github.phanerozoicc.statemachine.Visitor
import com.github.phanerozoicc.statemachine.builder.FailCallback
import com.github.phanerozoicc.statemachine.exception.StateMachineException
import mu.KLogging

class StateMachineImpl<S, E, C>(val stateMap: MutableMap<S, State<S, E, C>>)
    : StateMachine<S ,E ,C> {

    override lateinit var machineId: String
    var ready: Boolean = false
    var failCallback: FailCallback<S, E, C>? = null

    companion object: KLogging()

    fun isReady() {
        require(ready) {
            throw StateMachineException("StateMachine is not built yet! can not work")
        }
    }

    override fun verify(sourcerStateId: S, event: E): Boolean {
        isReady()
        val sourceState = getState(sourcerStateId)
        val eventTransitions = sourceState.getEventTransitions(event)
        return !eventTransitions.isEmpty()
    }

    fun getState(stateId: S): State<S, E, C> {
        return StateHelper.getState(stateMap, stateId)
    }

    override fun fireEvent(sourcerStateId: S, event: E, context: C): S {
        isReady()
        val transition = routeTransition(sourcerStateId, event, context)
        if (transition == null) {
            logger.debug("there is no transition for {}", event)
            failCallback!!.onFail(sourcerStateId, event, context)
            return sourcerStateId
        }
        return transition.transit(context, false).id
    }

    private fun routeTransition(sourcerStateId: S, event: E, context: C): Transition<S, E, C>? {
        val sourceState = getState(sourcerStateId)
        val transitions = sourceState.getEventTransitions(event)
        if (transitions.isEmpty()) {
            return null
        }
        var transit: Transition<S, E, C>? = null
        for (transition in transitions) {
            if (transition.condition == null) {
                transit = transition
            } else if (transition.condition!!.isSatisfied(context)) {
                transit = transition
                break
            }
        }
        return transit
    }

    override fun fireParallelEvent(sourcerStateId: S, event: E, context: C): List<S> {
        isReady()
        val transitions = routeTransitions(sourcerStateId, event, context)
        val result: MutableList<S> = mutableListOf()
        if (transitions==null || transitions.isEmpty()) {
            logger.debug("there is no transition for {}", event)
            failCallback!!.onFail(sourcerStateId, event, context)
            result.add(sourcerStateId)
            return result
        }
        return transitions.map { transition ->
            transition.transit(context, false) as S
        }
    }

    private fun routeTransitions(sourcerStateId: S, event: E, context: C) : List<Transition<S, E, C>>? {
        val sourceState = getState(sourcerStateId)
        val transitions = sourceState.getEventTransitions(event)
        if (transitions.isEmpty()) {
            return null
        }
        val result: MutableList<Transition<S, E, C>> = ArrayList()
        for (transition in transitions) {
            if (transition.condition == null) {
                result.add(transition)
            } else if (transition.condition!!.isSatisfied(context)) {
                result.add(transition)
            } else {
                throw IllegalStateException("condition not satisfied")
            }
        }
        return result
    }

    override fun showStateMachine() {
        accept(SysOutVisitor())
    }

    override fun generatePlantUML(): String {
        return accept(PlantUMLVisitor())
    }


    override fun accept(visitor: Visitor): String {
        return buildString {
            append(visitor.visitOnEntry(this@StateMachineImpl))
            stateMap.values.forEach { state ->
                append(state.accept(visitor))
            }
            append(visitor.visitOnExit(this@StateMachineImpl))
        }
    }
}