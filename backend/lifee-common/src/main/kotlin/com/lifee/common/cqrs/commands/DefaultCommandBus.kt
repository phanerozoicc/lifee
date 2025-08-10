package com.lifee.common.cqrs.commands

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

@Component
class DefaultCommandBus : CommandBus {
    
    private val logger = LoggerFactory.getLogger(DefaultCommandBus::class.java)
    private val syncHandlers = ConcurrentHashMap<KClass<out Command>, CommandHandler<*, *>>()
    private val asyncHandlers = ConcurrentHashMap<KClass<out Command>, AsyncCommandHandler<*, *>>()
    
    fun <T : Command, R> registerHandler(commandType: KClass<T>, handler: CommandHandler<T, R>) {
        logger.debug("Registering sync command handler for {}", commandType.simpleName)
        syncHandlers[commandType] = handler
    }
    
    fun <T : Command, R> registerAsyncHandler(commandType: KClass<T>, handler: AsyncCommandHandler<T, R>) {
        logger.debug("Registering async command handler for {}", commandType.simpleName)
        asyncHandlers[commandType] = handler
    }
    
    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Command, R> send(command: T): R {
        logger.debug("Sending command: {}", command::class.simpleName)
        
        // 优先查找异步处理器
        val asyncHandler = asyncHandlers[command::class] as? AsyncCommandHandler<T, R>
        if (asyncHandler != null) {
            return try {
                // 真正异步执行：在IO调度器中执行异步处理器
                coroutineScope {
                    val deferred = async(Dispatchers.IO) {
                        asyncHandler.handle(command)
                    }
                    val result = deferred.await()
                    logger.debug("Async command {} handled successfully", command::class.simpleName)
                    result
                }
            } catch (e: Exception) {
                logger.error("Error handling async command {}: {}", command::class.simpleName, e.message, e)
                throw e
            }
        }
        
        // 查找同步处理器
        val syncHandler = syncHandlers[command::class] as? CommandHandler<T, R>
        if (syncHandler != null) {
            return try {
                val result = syncHandler.handle(command)
                logger.debug("Sync command {} handled successfully", command::class.simpleName)
                result
            } catch (e: Exception) {
                logger.error("Error handling sync command {}: {}", command::class.simpleName, e.message, e)
                throw e
            }
        }
        
        throw IllegalArgumentException("No handler found for command: ${command::class.simpleName}")
    }
    
    override suspend fun <T : Command, R> sendAndWait(command: T): R {
        return send(command)
    }
    
    fun getRegisteredHandlersCount(): Int = syncHandlers.size + asyncHandlers.size
    
    fun hasHandler(commandType: KClass<out Command>): Boolean {
        return syncHandlers.containsKey(commandType) || asyncHandlers.containsKey(commandType)
    }
    
    fun hasSyncHandler(commandType: KClass<out Command>): Boolean {
        return syncHandlers.containsKey(commandType)
    }
    
    fun hasAsyncHandler(commandType: KClass<out Command>): Boolean {
        return asyncHandlers.containsKey(commandType)
    }
}