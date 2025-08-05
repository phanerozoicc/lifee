package com.github.phanerozoicc.base.statemachine.impl

import com.github.phanerozoicc.statemachine.Transition

/**
 * 事件流转记录
 * 记录每个事件对应的多个状态转换
 */
class EventTransitions<S, E, C> {

    private val eventTransitions: MutableMap<E, MutableList<Transition<S, E, C>>> = mutableMapOf()

    fun put(event: E, transition: Transition<S, E, C>) {
        var existTransitions = eventTransitions[event]
        if (existTransitions == null) {
            existTransitions = mutableListOf()
            existTransitions.add(transition)
            eventTransitions[event] = existTransitions
        } else {
            verify(existTransitions, transition)
            existTransitions.add(transition)
        }
    }

    private fun verify(
        existTransitions: MutableList<Transition<S, E, C>>,
        transition: Transition<S, E, C>
    ) {
        require(existTransitions.none { it == transition }) {
            "$transition already exists, cannot add again."
        }
    }

    fun get(event: E): List<Transition<S, E, C>> {
        return eventTransitions[event] ?: emptyList()
    }

    fun allTransitions(): List<Transition<S, E, C>> {
        return eventTransitions.values.flatten()
    }
}