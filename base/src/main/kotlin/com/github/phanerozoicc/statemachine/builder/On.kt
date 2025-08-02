package com.github.phanerozoicc.statemachine.builder

import com.github.phanerozoicc.statemachine.Condition

interface On<S, E, C>: When<S, E, C>  {
    /**
     * 为转换添加条件
     */
    fun `when`(condition: Condition<C>): When<S, E, C>
}