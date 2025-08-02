package com.github.phanerozoicc.statemachine

interface Visitable {

    fun accept(visitor: Visitor): String
}