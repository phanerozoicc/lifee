package com.github.phanerozoicc.base.command

import com.github.phanerozoicc.base.domain.DomainEvent

interface CommandHandler<T:Command, R> {
    fun handle(command: T): R
}

interface AsyncCommandHandler<T:Command, R> {
    suspend fun handle(command: T): R
}

sealed class CommandResult<T> {
    data class Success<T>(val data: T, val events: List<DomainEvent> = emptyList()) : CommandResult<T>()
    data class Failure<T>(val error: String, val exception: Throwable?=null) : CommandResult<T>()
}

