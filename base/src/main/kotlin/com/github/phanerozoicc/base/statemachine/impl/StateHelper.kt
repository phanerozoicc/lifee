package com.github.phanerozoicc.base.statemachine.impl

import com.github.phanerozoicc.statemachine.State

class StateHelper {
    companion object {
        fun <S, E, C> getState(stateMap: MutableMap<S, State<S, E, C>>, stateId: S): State<S, E, C> {
            return stateMap.getOrPut(stateId) {
                StateImpl(stateId)
            }
        }

        fun <S, E, C> getStates(stateMap: MutableMap<S, State<S, E, C>>, vararg stateIds: S): List<State<S, E, C>> {
            return stateIds.map { getState(stateMap, it) }
        }
    }
}