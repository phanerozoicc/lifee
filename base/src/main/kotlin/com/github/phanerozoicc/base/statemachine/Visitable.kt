package com.github.phanerozoicc.base.statemachine

interface Visitable {

    fun accept(visitor: Visitor): String
}