package com.github.phanerozoicc.base.queries

interface QueryHandler<T: Query, R> {
    fun handle(query: T): R
    fun canHandle(query: Query): Boolean
}

sealed class QueryResult<T> {
    data class Success<T>(val data: T) : QueryResult<T>()
    data class Failure<T>(val error: String, val exception: Throwable? = null) : QueryResult<T>()
}