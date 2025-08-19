package com.github.phanerozoicc.user.infrastructure.cqrs

import com.github.phanerozoicc.base.queries.QueryBus
import com.github.phanerozoicc.base.queries.QueryHandler
import com.github.phanerozoicc.base.queries.QueryResult
import com.github.phanerozoicc.user.bak.domain.cqrs.UserQuery
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * 命令总线实现
 */
//@Component
//class CommandBusImpl(
//    private val applicationContext: ApplicationContext
//) : CommandBus {
//
//    private val handlerCache = mutableMapOf<KClass<out UserCommand>, CommandHandler<UserCommand>>()
//
//    @Suppress("UNCHECKED_CAST")
//    override suspend fun <T : UserCommand> send(command: T): CommandResult {
//        val handler = getHandler(command::class) as CommandHandler<T>
//        return handler.handle(command)
//    }
//
//    override fun <T : UserCommand> register(commandClass: Class<T>, handler: CommandHandler<T>) {
//        // Implementation for registering command handlers
//    }
//
//    @Suppress("UNCHECKED_CAST")
//    private fun getHandler(commandClass: KClass<out UserCommand>): CommandHandler<UserCommand> {
//        return handlerCache.getOrPut(commandClass) {
//            findHandler(commandClass) as CommandHandler<UserCommand>
//        }
//    }
//
//    private fun findHandler(commandClass: KClass<out UserCommand>): CommandHandler<*> {
//        val handlers = applicationContext.getBeansOfType(CommandHandler::class.java).values
//
//        for (handler in handlers) {
//            val handlerClass = handler::class
//            val interfaces = handlerClass.supertypes
//
//            for (interfaceType in interfaces) {
//                if (interfaceType.classifier == CommandHandler::class) {
//                    val typeArguments = interfaceType.arguments
//                    if (typeArguments.isNotEmpty()) {
//                        val commandType = typeArguments[0].type?.classifier as? KClass<*>
//                        if (commandType != null && commandClass.isSubclassOf(commandType)) {
//                            return handler
//                        }
//                    }
//                }
//            }
//        }
//
//        throw IllegalArgumentException("No handler found for command: ${commandClass.simpleName}")
//    }
//}

/**
 * 查询总线实现
 */
@Component
class QueryBusImpl(
    private val applicationContext: ApplicationContext
) : QueryBus {
    
    private val handlerCache = mutableMapOf<KClass<out UserQuery>, QueryHandler<*, *>>()
    
    @Suppress("UNCHECKED_CAST")
    override suspend fun <TQuery : UserQuery, TResult> send(query: TQuery): QueryResult<TResult> {
        val handler = getHandler(query::class) as? QueryHandler<TQuery, TResult>
            ?: return QueryResult.Error("未找到查询处理器: ${query::class.simpleName}")
        return handler.handle(query)
    }
    
    override fun <TQuery : UserQuery, TResult> register(
        queryClass: Class<TQuery>,
        handler: QueryHandler<TQuery, TResult>
    ) {
        // Implementation for registering query handlers
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun getHandler(queryClass: KClass<out UserQuery>): QueryHandler<*, *>? {
        return handlerCache.getOrPut(queryClass) {
            findHandler(queryClass) ?: return null
        }
    }
    
    private fun findHandler(queryClass: KClass<out UserQuery>): QueryHandler<*, *>? {
        val handlers = applicationContext.getBeansOfType(QueryHandler::class.java).values
        
        for (handler in handlers) {
            val handlerClass = handler::class
            val interfaces = handlerClass.supertypes
            
            for (interfaceType in interfaces) {
                if (interfaceType.classifier == QueryHandler::class) {
                    val typeArguments = interfaceType.arguments
                    if (typeArguments.isNotEmpty()) {
                        val queryType = typeArguments[0].type?.classifier as? KClass<*>
                        if (queryType != null && queryClass.isSubclassOf(queryType)) {
                            return handler
                        }
                    }
                }
            }
        }
        
        return null
    }
}

/**
 * 简化的命令总线实现（基于类名匹配）
 */
@Component
class SimpleCommandBusImpl(
    private val applicationContext: ApplicationContext
) : CommandBus {
    
    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : UserCommand> send(command: T): CommandResult {
        val handlerName = "${command::class.simpleName}Handler"
        
        try {
            val handler = applicationContext.getBean(handlerName) as CommandHandler<T>
            return handler.handle(command)
        } catch (e: Exception) {
            return CommandResult.Failure("Command execution failed: ${e.message}", "COMMAND_EXECUTION_ERROR")
        }
    }
    
    override fun <T : UserCommand> register(commandClass: Class<T>, handler: CommandHandler<T>) {
        // Implementation for registering command handlers
    }
}

/**
 * 简化的查询总线实现（基于类名匹配）
 */
@Component
class SimpleQueryBusImpl(
    private val applicationContext: ApplicationContext
) : QueryBus {
    
    @Suppress("UNCHECKED_CAST")
    override suspend fun <TQuery : UserQuery, TResult> send(query: TQuery): QueryResult<TResult> {
        val handlerName = "${query::class.simpleName}Handler"
        
        try {
            val handler = applicationContext.getBean(handlerName) as QueryHandler<TQuery, TResult>
            return handler.handle(query)
        } catch (e: Exception) {
            return QueryResult.Error("Query execution failed: ${e.message}", "QUERY_EXECUTION_ERROR")
        }
    }
    
    override fun <TQuery : UserQuery, TResult> register(
        queryClass: Class<TQuery>,
        handler: QueryHandler<TQuery, TResult>
    ) {
        // Implementation for registering query handlers
    }
}

/**
 * 装饰器模式的命令总线实现（支持中间件）
 */
@Component
class DecoratedCommandBusImpl(
    private val applicationContext: ApplicationContext,
    private val middlewares: List<CommandMiddleware> = emptyList()
) : CommandBus {
    
    @Suppress("UNCHECKED_CAST")
    override suspend fun <T : UserCommand> send(command: T): CommandResult {
        return executeWithMiddlewares(command, 0)
    }
    
    override fun <T : UserCommand> register(commandClass: Class<T>, handler: CommandHandler<T>) {
        // Implementation for registering command handlers
    }
    
    @Suppress("UNCHECKED_CAST")
    private suspend fun <T : UserCommand> executeWithMiddlewares(
        command: T,
        middlewareIndex: Int
    ): CommandResult {
        if (middlewareIndex >= middlewares.size) {
            // 执行实际的命令处理器
            val handlerName = "${command::class.simpleName}Handler"
            val handler = applicationContext.getBean(handlerName) as CommandHandler<T>
            return handler.handle(command)
        }
        
        val middleware = middlewares[middlewareIndex]
        return middleware.execute(command) {
            executeWithMiddlewares(command, middlewareIndex + 1)
        }
    }
}

/**
 * 装饰器模式的查询总线实现（支持中间件）
 */
@Component
class DecoratedQueryBusImpl(
    private val applicationContext: ApplicationContext,
    private val middlewares: List<QueryMiddleware> = emptyList()
) : QueryBus {
    
    @Suppress("UNCHECKED_CAST")
    override suspend fun <TQuery : UserQuery, TResult> send(query: TQuery): QueryResult<TResult> {
        return executeWithMiddlewares(query, 0)
    }
    
    override fun <TQuery : UserQuery, TResult> register(
        queryClass: Class<TQuery>,
        handler: QueryHandler<TQuery, TResult>
    ) {
        // Implementation for registering query handlers
    }
    
    @Suppress("UNCHECKED_CAST")
    private suspend fun <TQuery : UserQuery, TResult> executeWithMiddlewares(
        query: TQuery,
        middlewareIndex: Int
    ): QueryResult<TResult> {
        if (middlewareIndex >= middlewares.size) {
            // 执行实际的查询处理器
            val handlerName = "${query::class.simpleName}Handler"
            val handler = applicationContext.getBean(handlerName) as QueryHandler<TQuery, TResult>
            return handler.handle(query)
        }
        
        val middleware = middlewares[middlewareIndex]
        return middleware.execute(query) {
            executeWithMiddlewares(query, middlewareIndex + 1)
        }
    }
}

/**
 * 命令中间件接口
 */
interface CommandMiddleware {
    suspend fun <T : UserCommand> execute(
        command: T,
        next: suspend () -> CommandResult
    ): CommandResult
}

/**
 * 查询中间件接口
 */
interface QueryMiddleware {
    suspend fun <T : UserQuery, TResult> execute(
        query: T,
        next: suspend () -> QueryResult<TResult>
    ): QueryResult<TResult>
}

/**
 * 日志中间件
 */
@Component
class LoggingCommandMiddleware : CommandMiddleware {
    override suspend fun <T : UserCommand> execute(
        command: T,
        next: suspend () -> CommandResult
    ): CommandResult {
        println("Executing command: ${command::class.simpleName}")
        val startTime = System.currentTimeMillis()
        
        try {
            val result = next()
            val duration = System.currentTimeMillis() - startTime
            println("Command ${command::class.simpleName} completed in ${duration}ms")
            return result
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            println("Command ${command::class.simpleName} failed in ${duration}ms: ${e.message}")
            throw e
        }
    }
}

/**
 * 验证中间件
 */
@Component
class ValidationCommandMiddleware : CommandMiddleware {
    override suspend fun <T : UserCommand> execute(
        command: T,
        next: suspend () -> CommandResult
    ): CommandResult {
        // 这里可以添加通用的验证逻辑
        // 例如：检查用户权限、验证输入格式等
        
        return next()
    }
}

/**
 * 缓存查询中间件
 */
@Component
class CachingQueryMiddleware : QueryMiddleware {
    private val cache = mutableMapOf<String, QueryResult<Any>>()
    
    override suspend fun <T : UserQuery, TResult> execute(
        query: T,
        next: suspend () -> QueryResult<TResult>
    ): QueryResult<TResult> {
        val cacheKey = generateCacheKey(query)
        
        // 检查缓存
        cache[cacheKey]?.let { cachedResult ->
            println("Cache hit for query: ${query::class.simpleName}")
            @Suppress("UNCHECKED_CAST")
            return cachedResult as QueryResult<TResult>
        }
        
        // 执行查询
        val result = next()
        
        // 缓存结果（只缓存成功的结果）
        if (result is QueryResult.Success<*>) {
            cache[cacheKey] = result as QueryResult<Any>
        }
        
        return result
    }
    
    private fun generateCacheKey(query: UserQuery): String {
        return "${query::class.simpleName}:${query.hashCode()}"
    }
}