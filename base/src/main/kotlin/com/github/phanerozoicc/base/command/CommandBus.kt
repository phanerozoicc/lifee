package com.github.phanerozoicc.base.command

import com.github.phanerozoicc.base.domain.DomainEventPublisher
import org.springframework.transaction.support.TransactionTemplate

interface CommandBus {
    fun <R> send(command: Command): R
}

class SpringCommandBus(
    private val commandHandlers: List<CommandHandler<*, *>>,
    private val eventPublisher: DomainEventPublisher,
    private val transitionTemplate: TransactionTemplate
) : CommandBus {
    override fun <R> send(command: Command): R {
        val handler = commandHandlers.find { it.canHandle(command) }
            ?: throw IllegalArgumentException("No handler found for command ${command.commandType}")
        return transitionTemplate.execute {
            val result = (handler as CommandHandler<Command, R>).handle(command)
            if (result is CommandResult.Success<*> && result.events.isNotEmpty()) {
                result.events.forEach { eventPublisher.publish(it) }
            }
            result
        } as R
    }

}