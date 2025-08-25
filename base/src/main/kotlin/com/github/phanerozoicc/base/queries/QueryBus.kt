package com.github.phanerozoicc.base.queries

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import mu.KLogging
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

interface QueryBus {
    suspend fun <T: Query, R> send(query: T): R
}


class DefaultQueryBus : QueryBus {

    private val syncHandlers = ConcurrentHashMap<KClass<out Query>, QueryHandler<out Query, *>>()
    private val asyncHandlers = ConcurrentHashMap<KClass<out Query>, AsyncQueryHandler<out Query, *>>()

    companion object: KLogging()

    @Suppress("UNCHECKED_CAST")
    override suspend fun <T: Query, R> send(query: T): R {
        val asyncHandler = asyncHandlers[query::class] as? AsyncQueryHandler<T, R>
        if (asyncHandler != null) {
            return try {
                coroutineScope {
                    val deferred = async(Dispatchers.IO) {
                        asyncHandler.handle(query)
                    }
                    val result = deferred.await()
                    logger.debug("Async query {} handled successfully", query::class.simpleName)
                    result
                }
            } catch (e: Exception) {
                logger.error("Error handling async query ${query::class.simpleName}: ${e.message}", e)
                throw e
            }
        }

        val syncHandler = syncHandlers[query::class] as? QueryHandler<T, R>
        if (syncHandler != null) {
            return try {
                val result = syncHandler.handle(query)
                logger.debug("Sync query {} handled successfully", query::class.simpleName)
                result
            } catch (e: Exception) {
                logger.error("Error handling sync query ${query::class.simpleName}: ${e.message}", e)
                throw e
            }
        }

        throw IllegalArgumentException("No handler found for query type: ${query::class.simpleName}")
    }

    fun <T: Query, R> registerHandler(queryType: KClass<T>, handler: QueryHandler<out Query, out Any>) {
        syncHandlers[queryType] = handler
    }

    fun <T: Query, R> registerAsyncHandler(queryType: KClass<T>, handler: AsyncQueryHandler<T, R>) {
        asyncHandlers[queryType] = handler
    }
}