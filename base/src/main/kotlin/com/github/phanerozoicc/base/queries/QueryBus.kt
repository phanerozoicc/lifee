package com.github.phanerozoicc.base.queries

interface QueryBus {
    suspend fun <R> send(query: Query): R
}