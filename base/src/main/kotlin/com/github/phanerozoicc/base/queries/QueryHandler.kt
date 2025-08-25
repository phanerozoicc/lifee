package com.github.phanerozoicc.base.queries

interface QueryHandler<T: Query, R> {
    fun handle(query: T): R
}

interface AsyncQueryHandler<T: Query, R> {
    suspend fun handle(query: T): R
}
