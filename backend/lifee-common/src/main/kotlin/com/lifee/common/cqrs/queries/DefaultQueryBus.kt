package com.lifee.common.cqrs.queries

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * 默认查询总线实现
 * 使用内存映射来存储查询类型和处理器的关系
 */
@Component
class DefaultQueryBus : QueryBus {
    
    private val logger = LoggerFactory.getLogger(DefaultQueryBus::class.java)
    private val syncHandlers = ConcurrentHashMap<KClass<out Query>, QueryHandler<*, *>>()
    private val asyncHandlers = ConcurrentHashMap<KClass<out Query>, AsyncQueryHandler<*, *>>()
    
    /**
     * 注册同步查询处理器
     * 
     * @param queryType 查询类型
     * @param handler 查询处理器
     */
    fun <T : Query, R> registerHandler(queryType: KClass<T>, handler: QueryHandler<T, R>) {
        logger.debug("Registering sync query handler for {}", queryType.simpleName)
        syncHandlers[queryType] = handler
    }
    
    /**
     * 注册异步查询处理器
     * 
     * @param queryType 查询类型
     * @param handler 异步查询处理器
     */
    fun <T : Query, R> registerAsyncHandler(queryType: KClass<T>, handler: AsyncQueryHandler<T, R>) {
        logger.debug("Registering async query handler for {}", queryType.simpleName)
        asyncHandlers[queryType] = handler
    }
    
    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : Query, R> send(query: T): R {
        logger.debug("Sending query: {}", query::class.simpleName)
        
        // 优先查找异步处理器
        val asyncHandler = asyncHandlers[query::class] as? AsyncQueryHandler<T, R>
        if (asyncHandler != null) {
            return try {
                // 真正异步执行：在IO调度器中执行异步处理器
                coroutineScope {
                    val deferred = async(Dispatchers.IO) {
                        asyncHandler.handle(query)
                    }
                    val result = deferred.await()
                    logger.debug("Async query {} handled successfully", query::class.simpleName)
                    result
                }
            } catch (e: Exception) {
                logger.error("Error handling async query {}: {}", query::class.simpleName, e.message, e)
                throw e
            }
        }
        
        // 查找同步处理器
        val syncHandler = syncHandlers[query::class] as? QueryHandler<T, R>
        if (syncHandler != null) {
            return try {
                val result = syncHandler.handle(query)
                logger.debug("Sync query {} handled successfully", query::class.simpleName)
                result
            } catch (e: Exception) {
                logger.error("Error handling sync query {}: {}", query::class.simpleName, e.message, e)
                throw e
            }
        }
        
        throw IllegalArgumentException("No handler found for query: ${query::class.simpleName}")
    }
    
    /**
     * 获取已注册的处理器数量
     */
    fun getRegisteredHandlersCount(): Int = syncHandlers.size + asyncHandlers.size
    
    /**
     * 检查是否有处理器注册给指定的查询类型
     */
    fun <T : Query> hasHandler(queryType: KClass<T>): Boolean {
        return syncHandlers.containsKey(queryType) || asyncHandlers.containsKey(queryType)
    }
    
    /**
     * 检查是否有同步处理器注册给指定的查询类型
     */
    fun <T : Query> hasSyncHandler(queryType: KClass<T>): Boolean {
        return syncHandlers.containsKey(queryType)
    }
    
    /**
     * 检查是否有异步处理器注册给指定的查询类型
     */
    fun <T : Query> hasAsyncHandler(queryType: KClass<T>): Boolean {
        return asyncHandlers.containsKey(queryType)
    }
}