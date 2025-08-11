package com.github.phanerozoicc.base.command

import com.github.phanerozoicc.base.domain.DomainEventPublisher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

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
        // 处理command
        val result = (handler as CommandHandler<Command, R>).handle(command)
        // 统一发送产生的领域事件
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
        // 首先处理异步
        val asyncHandler = asyncHandlers[command::class] as? AsyncCommandHandler<Command, R>
        if (asyncHandler != null) {
            return try {
                coroutineScope {
                    val deferred = async(Dispatchers.IO) {
                        asyncHandler.handle(command)
                    }
                    // 等待
                    val result = deferred.await()
                    logger.debug("handling command:{} success, command id:{}", command::class.simpleName, command.commandId)
                    result
                }
            } catch (e: Throwable) {
                logger.error("error handling async command: {} error:{}", command,  e.message, e)
                throw e
            }
        }

        // 处理同步
        val syncHandler = syncHandlers[command::class] as? CommandHandler<Command, R>
        if (syncHandler != null) {
            return try {
                val result = syncHandler.handle(command)
                logger.debug("handling command:{} success, command id:{}", command::class.simpleName, command.commandId)
                result
            } catch (e: Throwable) {
                logger.error("error handling sync command: {} error:{}", command,  e.message, e)
                throw e
            }
        }

        throw IllegalArgumentException("No handler found for command ${command.commandType}")
    }

    override suspend fun <R> sendAndWait(command: Command): R {
        return send(command)
    }

}