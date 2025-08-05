package com.github.phanerozoicc.base.statemachine

/**
 * alibaba cola state machine
 */
interface StateMachine<S, E, C> : Visitable{

    val machineId: String

    /**
     * 验证event E是否可以从当前状态触发
     */
    fun verify(sourcerStateId: S, event: E): Boolean

    /**
     * 触发事件E到状态机
     */
    fun fireEvent(sourcerStateId: S, event: E, context: C): S

    /**
     * 触发并行事件events到状态机，返回最终状态S
     * 并行事件之间没有顺序关系，可以同时触发
     *
     */
    fun fireParallelEvent(sourcerStateId: S, event: E, context: C): List<S>

    fun showStateMachine()

    fun generatePlantUML(): String

}