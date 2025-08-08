package com.github.phanerozoicc.base.query

interface QueryBus {
    suspend fun <R> send(query: Query): R
}