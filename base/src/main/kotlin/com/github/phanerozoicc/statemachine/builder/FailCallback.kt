package com.github.phanerozoicc.statemachine.builder

import com.github.phanerozoicc.statemachine.exception.TransitionFailException

interface FailCallback<S, E, C> {
    fun onFail(sourceStateId: S, event: E, context: C)
}

class NumbFailCallBack<S, E, C>: FailCallback<S, E, C> {
    override fun onFail(sourceStateId: S, event: E, context: C) {
        // do nothing
    }
}

class AlertFailCallback<S, E, C>: FailCallback<S, E, C> {
    override fun onFail(sourceStateId: S, event: E, context: C) {
        throw TransitionFailException("Transition failed from state $sourceStateId on event $event")
    }
}
