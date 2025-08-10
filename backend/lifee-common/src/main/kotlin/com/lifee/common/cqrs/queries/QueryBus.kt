package com.lifee.common.cqrs.queries

interface QueryBus {
    suspend fun <T : Query, R> send(query: T): R
}