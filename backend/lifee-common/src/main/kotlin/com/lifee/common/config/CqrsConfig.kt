package com.lifee.common.config

import com.lifee.common.cqrs.commands.Command
import com.lifee.common.cqrs.commands.CommandHandler
import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.cqrs.commands.DefaultCommandBus
import com.lifee.common.cqrs.events.Event
import com.lifee.common.cqrs.events.EventHandler
import com.lifee.common.cqrs.events.DefaultEventBus
import com.lifee.common.cqrs.queries.Query
import com.lifee.common.cqrs.queries.QueryHandler
import com.lifee.common.cqrs.queries.AsyncQueryHandler
import com.lifee.common.cqrs.queries.DefaultQueryBus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.core.GenericTypeResolver
import org.springframework.stereotype.Component

/**
 * CQRS配置类
 * 自动注册所有的命令处理器、查询处理器和事件处理器
 */
@Configuration
@Component
class CqrsConfig(
    private val applicationContext: ApplicationContext,
    private val commandBus: DefaultCommandBus,
    private val queryBus: DefaultQueryBus,
    private val eventBus: DefaultEventBus
) : InitializingBean {
    
    private val logger = LoggerFactory.getLogger(CqrsConfig::class.java)
    
    override fun afterPropertiesSet() {
        registerCommandHandlers()
        registerAsyncCommandHandlers()
        registerQueryHandlers()
        registerAsyncQueryHandlers()
        registerEventHandlers()
        
        logger.info("CQRS configuration initialized successfully")
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun registerCommandHandlers() {
        val commandHandlers = applicationContext.getBeansOfType(CommandHandler::class.java)
        
        commandHandlers.values.forEach { handler ->
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, CommandHandler::class.java)
            
            if (genericTypes != null && genericTypes.isNotEmpty()) {
                val commandType = genericTypes[0] as Class<out Command>
                commandBus.registerHandler(commandType.kotlin as kotlin.reflect.KClass<Command>, handler as CommandHandler<Command, Any>)
                logger.info("Registered command handler: {} for command: {}", 
                    handlerClass.simpleName, commandType.simpleName)
            }
        }
        
        logger.info("Registered {} command handlers", commandHandlers.size)
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun registerAsyncCommandHandlers() {
        val asyncCommandHandlers = applicationContext.getBeansOfType(AsyncCommandHandler::class.java)
        
        asyncCommandHandlers.values.forEach { handler ->
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, AsyncCommandHandler::class.java)
            
            if (genericTypes != null && genericTypes.isNotEmpty()) {
                val commandType = genericTypes[0] as Class<out Command>
                commandBus.registerAsyncHandler(commandType.kotlin as kotlin.reflect.KClass<Command>, handler as AsyncCommandHandler<Command, Any>)
                logger.info("Registered async command handler: {} for command: {}", 
                    handlerClass.simpleName, commandType.simpleName)
            }
        }
        
        logger.info("Registered {} async command handlers", asyncCommandHandlers.size)
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun registerQueryHandlers() {
        val queryHandlers = applicationContext.getBeansOfType(QueryHandler::class.java)
        
        queryHandlers.values.forEach { handler ->
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, QueryHandler::class.java)
            
            if (genericTypes != null && genericTypes.size >= 2) {
                val queryType = genericTypes[0] as Class<out Query>
                queryBus.registerHandler(queryType.kotlin as kotlin.reflect.KClass<Query>, handler as QueryHandler<Query, Any>)
                logger.info("Registered query handler: {} for query: {}", 
                    handlerClass.simpleName, queryType.simpleName)
            }
        }
        
        logger.info("Registered {} query handlers", queryHandlers.size)
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun registerAsyncQueryHandlers() {
        val asyncQueryHandlers = applicationContext.getBeansOfType(AsyncQueryHandler::class.java)
        
        asyncQueryHandlers.values.forEach { handler ->
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, AsyncQueryHandler::class.java)
            
            if (genericTypes != null && genericTypes.size >= 2) {
                val queryType = genericTypes[0] as Class<out Query>
                queryBus.registerAsyncHandler(queryType.kotlin as kotlin.reflect.KClass<Query>, handler as AsyncQueryHandler<Query, Any>)
                logger.info("Registered async query handler: {} for query: {}", 
                    handlerClass.simpleName, queryType.simpleName)
            }
        }
        
        logger.info("Registered {} async query handlers", asyncQueryHandlers.size)
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun registerEventHandlers() {
        val eventHandlers = applicationContext.getBeansOfType(EventHandler::class.java)
        
        eventHandlers.values.forEach { handler ->
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, EventHandler::class.java)
            
            if (genericTypes != null && genericTypes.isNotEmpty()) {
                val eventType = genericTypes[0] as Class<out Event>
                eventBus.registerHandler(eventType.kotlin as kotlin.reflect.KClass<Event>, handler as EventHandler<Event>)
                logger.info("Registered event handler: {} for event: {}", 
                    handlerClass.simpleName, eventType.simpleName)
            }
        }
        
        logger.info("Registered {} event handlers", eventHandlers.size)
    }
}