package com.github.phanerozoicc.base.statemachine

import com.github.phanerozoicc.statemachine.exception.StateMachineException
import java.util.concurrent.ConcurrentHashMap

/**
 * 状态机工厂类 用于创建和管理状态机实例
 */
class StateMachineFactory {

    companion object {
        // 使用ConcurrentHashMap来存储状态机实例，确保线程安全
        val stateMachines = ConcurrentHashMap<String, StateMachine<Any, Any, Any>>()

        fun <S, E, C> register(stateMachine: StateMachine<S, E, C>) {
            val machineId = stateMachine.machineId
            require(!stateMachines.containsKey(machineId)) {
                "StateMachine with ID $machineId already exists." }
            stateMachines[machineId] = stateMachine as StateMachine<Any, Any, Any>
        }

        fun get(machineId: String) = stateMachines[machineId]
                ?: throw StateMachineException("StateMachine with ID $machineId not found.")
    }

}
