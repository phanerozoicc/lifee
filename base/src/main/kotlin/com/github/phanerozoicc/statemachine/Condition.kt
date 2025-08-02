package com.github.phanerozoicc.statemachine

interface Condition<C> {

    /**
     * 是否满足条件
     */
    fun isSatisfied(context: C): Boolean

    /**
     * 当前类的名称
     */
    fun name(): String = this::class.java.simpleName
}