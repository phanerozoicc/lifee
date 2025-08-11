package com.github.phanerozoicc.base.command

import com.github.phanerozoicc.base.domain.DomainEventPublisher
import mu.KLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KType

interface CommandBus {
    suspend fun <R> send(command: Command): R
    suspend fun <R> sendAndWait(command: Command): R
}



class SpringEventCommandBus(
    private val commandHandlers: MutableMap<Command, CommandHandler<out Command, *>>,
    private val eventPublisher: DomainEventPublisher,
//    private val transitionTemplate: TransactionTemplate
) : CommandBus {
    override suspend fun <R> sendAndWait(command: Command): R {
        val handler = commandHandlers[command]
            ?: throw IllegalArgumentException("No handler found for command ${command.commandType}")
        val result = (handler as CommandHandler<Command, R>).handle(command)
        if (result is CommandResult.Success<*> && result.events.isNotEmpty()) {
            result.events.forEach { eventPublisher.publish(it) }
        }
        return result
    }

    override suspend fun <R> send(command: Command): R {
        // spring事务且考虑到命令大多为同步处理
        // 仅作同步处理
        return sendAndWait(command)
    }

    fun register(command: Command, handler: CommandHandler<out Command, *>) {
        commandHandlers[command] = handler
    }
}



class DefaultCommandBus(

): CommandBus {
    companion object: KLogging()

    private val syncHandlers = ConcurrentHashMap<KClass<out Command>, CommandHandler<out Command, *>>()
    private val asyncHandlers = ConcurrentHashMap<KClass<out Command>, AsyncCommandHandler<out Command, *>>()


    fun registerHandler(commandType: KClass<out Command>, handler: CommandHandler<out Command, *>) {
        logger.debug("registering sync command handler for {}", commandType.simpleName)
        syncHandlers[commandType] = handler
    }

    fun registerAsyncHandler(commandType: KClass<out Command>, handler: AsyncCommandHandler<out Command, *>) {
        logger.debug("registering async command handler for {}", commandType.simpleName)
        asyncHandlers[commandType] = handler
    }

    override suspend fun <R> send(command: Command): R {
        logger.debug("sending command: {}", command::class.simpleName)
        val asyncHandler = asyncHandlers[command::class] as? AsyncCommandHandler<Command, R>
        if (asyncHandler != null) {
            try {
                coroutineScope {

                }
            }
        }
    }

}