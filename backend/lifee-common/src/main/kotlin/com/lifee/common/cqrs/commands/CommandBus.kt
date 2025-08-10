package com.lifee.common.cqrs.commands

interface CommandBus {
    suspend fun <T : Command, R> send(command: T): R
    suspend fun <T : Command, R> sendAndWait(command: T): R
}