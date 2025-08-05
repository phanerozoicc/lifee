package com.github.phanerozoicc.base.statemachine.impl

import com.github.phanerozoicc.statemachine.State
import com.github.phanerozoicc.statemachine.Transition
import com.github.phanerozoicc.statemachine.Visitor
import mu.KLogging

class StateImpl<S, E, C>(override val id: S) : State<S, E, C> {

    companion object: KLogging()

    private val eventTransitions = EventTransitions<S, E, C>()

    override fun addTransition(
        event: E,
        target: State<S, E, C>,
        transitionType: TransitionType
    ): Transition<S, E, C> {
        val transition = TransitionImpl(this, target, event, transitionType)
        logger.debug { "begin to add new transition $transition" }
        eventTransitions.put(event, transition)
        return transition
    }

    override fun addTransitions(
        event: E,
        targets: List<State<S, E, C>>,
        transitionType: TransitionType
    ): List<Transition<S, E, C>> {
        return targets.map { target ->
            addTransition(event, target, transitionType)
        }
    }

    override fun getEventTransitions(event: E): List<Transition<S, E, C>> {
        return eventTransitions.get(event)
    }

    override fun getAllTransitions(): Collection<Transition<S, E, C>> {
        return eventTransitions.allTransitions()
    }

    override fun accept(visitor: Visitor): String {
        val entry = visitor.visitOnEntry(this)
        val exit = visitor.visitOnExit(this)
        return exit+entry
    }

    override fun toString(): String {
        return id.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (other is State<*, *, *>) {
            return this.id == other.id
        }
        return false
    }

}