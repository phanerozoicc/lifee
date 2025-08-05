package com.github.phanerozoicc.base.statemachine.impl

import com.github.phanerozoicc.statemachine.LF
import com.github.phanerozoicc.statemachine.State
import com.github.phanerozoicc.statemachine.StateMachine
import com.github.phanerozoicc.statemachine.Visitor


class SysOutVisitor: Visitor {
     override fun visitOnEntry(visitable: StateMachine<*, *, *>): String {
        val entry = "-----StateMachine:${visitable.machineId}-------"
        println(entry)
        return entry
    }

    override fun visitOnExit(visitable: StateMachine<*, *, *>): String {
        val exit = "------------------------"
        println(exit)
        return exit
    }

    override fun visitOnEntry(visitor: State<*, *, *>): String {
        val sb = StringBuilder()
        val stateStr = "State:${visitor.id}"
        sb.append(stateStr).append(LF)
        println(stateStr)
        for (transition in visitor.getAllTransitions()) {
            val transitionStr = "    Transition:$transition"
            sb.append(transitionStr).append(LF)
            println(transitionStr)
        }
        return sb.toString()
    }

    override fun visitOnExit(visitor: State<*, *, *>): String {
        return ""
    }
}