package com.github.phanerozoicc.base.statemachine.impl

import com.github.phanerozoicc.statemachine.LF
import com.github.phanerozoicc.statemachine.State
import com.github.phanerozoicc.statemachine.StateMachine
import com.github.phanerozoicc.statemachine.Visitor

class PlantUMLVisitor: Visitor {
    override fun visitOnEntry(visitable: StateMachine<*, *, *>): String {
        return "@startuml$LF"
    }

    override fun visitOnExit(visitable: StateMachine<*, *, *>): String {
        return "@enduml"
    }

    override fun visitOnEntry(visitor: State<*, *, *>): String {
        val sb = StringBuilder()
        for (transition in visitor.getAllTransitions()) {
            sb.append(transition.source.id)
                .append(" --> ")
                .append(transition.target.id)
                .append(" : ")
                .append(transition.event)
                .append(LF)
        }
        return sb.toString()
    }

    override fun visitOnExit(visitor: State<*, *, *>): String {
        return ""
    }


}