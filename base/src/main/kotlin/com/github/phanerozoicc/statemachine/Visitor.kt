package com.github.phanerozoicc.statemachine

const val LF = '\n'
interface Visitor {

    fun visitOnEntry(visitable: StateMachine<*, *, *>): String

    fun visitOnExit(visitable: StateMachine<*, *, *>): String

    fun visitOnEntry(visitor: State<*, *, *>): String

    fun visitOnExit(visitor: State<*, *, *>): String

}