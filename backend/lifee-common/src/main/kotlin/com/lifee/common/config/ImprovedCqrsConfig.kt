package com.lifee.common.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.lifee.common.cqrs.commands.*
import com.lifee.common.cqrs.events.*
import com.lifee.common.cqrs.queries.*
import com.lifee.common.domain.DomainEvent
import com.lifee.common.eventsourcing.EventStore
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.GenericTypeResolver
import org.springframework.kafka.core.KafkaTemplate
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

/**
 * 改进的CQRS配置
 * 修复处理器注册机制的问题
 */
@Configuration
class ImprovedCqrsConfig(
    private val applicationContext: ApplicationContext
) : InitializingBean {
    
    private val logger = LoggerFactory.getLogger(ImprovedCqrsConfig::class.java)
    
    @Bean
    fun commandBus(): CommandBus {
        return DefaultCommandBus()
    }
    
    @Bean
    fun queryBus(): QueryBus {
        return DefaultQueryBus()
    }
    
    @Bean
    fun eventBus(
        kafkaTemplate: KafkaTemplate<String, Any>,
        objectMapper: ObjectMapper,
        eventStore: EventStore
    ): EventBus {
        return DefaultEventBus(kafkaTemplate, objectMapper, eventStore)
    }
    
    override fun afterPropertiesSet() {
        val commandBus = commandBus()
        val queryBus = queryBus()
        val eventBus = eventBus(
            applicationContext.getBean(KafkaTemplate::class.java) as KafkaTemplate<String, Any>,
            applicationContext.getBean(ObjectMapper::class.java),
            applicationContext.getBean(EventStore::class.java)
        )
        
        registerCommandHandlers(commandBus)
        registerQueryHandlers(queryBus)
        registerEventHandlers(eventBus)
        
        logger.info("Improved CQRS configuration initialized successfully")
        logger.info("Registered {} command handlers", (commandBus as DefaultCommandBus).getRegisteredHandlersCount())
        logger.info("Registered {} query handlers", (queryBus as DefaultQueryBus).getRegisteredHandlersCount())
        logger.info("Registered {} event handlers", (eventBus as DefaultEventBus).getRegisteredHandlersCount())
    }
    
    /**
     * 注册命令处理器
     */
    private fun registerCommandHandlers(commandBus: CommandBus) {
        val defaultCommandBus = commandBus as DefaultCommandBus
        
        // 注册同步命令处理器
        val syncHandlers = applicationContext.getBeansOfType(CommandHandler::class.java)
        syncHandlers.values.forEach { handler ->
            registerSyncCommandHandler(defaultCommandBus, handler)
        }
        
        // 注册异步命令处理器
        val asyncHandlers = applicationContext.getBeansOfType(AsyncCommandHandler::class.java)
        asyncHandlers.values.forEach { handler ->
            registerAsyncCommandHandler(defaultCommandBus, handler)
        }
    }
    
    /**
     * 注册查询处理器
     */
    private fun registerQueryHandlers(queryBus: QueryBus) {
        val defaultQueryBus = queryBus as DefaultQueryBus
        
        // 注册同步查询处理器
        val syncHandlers = applicationContext.getBeansOfType(QueryHandler::class.java)
        syncHandlers.values.forEach { handler ->
            registerSyncQueryHandler(defaultQueryBus, handler)
        }
        
        // 注册异步查询处理器
        val asyncHandlers = applicationContext.getBeansOfType(AsyncQueryHandler::class.java)
        asyncHandlers.values.forEach { handler ->
            registerAsyncQueryHandler(defaultQueryBus, handler)
        }
    }
    
    /**
     * 注册事件处理器
     */
    private fun registerEventHandlers(eventBus: EventBus) {
        val defaultEventBus = eventBus as DefaultEventBus
        
        val eventHandlers = applicationContext.getBeansOfType(EventHandler::class.java)
        eventHandlers.values.forEach { handler ->
            registerEventHandler(defaultEventBus, handler)
        }
    }
    
    /**
     * 注册同步命令处理器
     */
    @Suppress("UNCHECKED_CAST")
    private fun registerSyncCommandHandler(commandBus: DefaultCommandBus, handler: CommandHandler<*, *>) {
        try {
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, CommandHandler::class.java)
            
            if (genericTypes != null && genericTypes.size >= 2) {
                val commandType = genericTypes[0] as Class<out Command>
                val commandKClass = commandType.kotlin as KClass<Command>
                
                // 验证命令类型
                if (commandKClass.isSubclassOf(Command::class)) {
                    commandBus.registerHandler(commandKClass, handler as CommandHandler<Command, Any>)
                    logger.debug("Registered sync command handler: {} for command: {}", 
                        handlerClass.simpleName, commandType.simpleName)
                } else {
                    logger.warn("Invalid command type for handler {}: {}", 
                        handlerClass.simpleName, commandType.simpleName)
                }
            } else {
                logger.warn("Could not resolve generic types for command handler: {}", handlerClass.simpleName)
            }
        } catch (e: Exception) {
            logger.error("Error registering sync command handler: {}", handler::class.simpleName, e)
        }
    }
    
    /**
     * 注册异步命令处理器
     */
    @Suppress("UNCHECKED_CAST")
    private fun registerAsyncCommandHandler(commandBus: DefaultCommandBus, handler: AsyncCommandHandler<*, *>) {
        try {
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, AsyncCommandHandler::class.java)
            
            if (genericTypes != null && genericTypes.size >= 2) {
                val commandType = genericTypes[0] as Class<out Command>
                val commandKClass = commandType.kotlin as KClass<Command>
                
                // 验证命令类型
                if (commandKClass.isSubclassOf(Command::class)) {
                    commandBus.registerAsyncHandler(commandKClass, handler as AsyncCommandHandler<Command, Any>)
                    logger.debug("Registered async command handler: {} for command: {}", 
                        handlerClass.simpleName, commandType.simpleName)
                } else {
                    logger.warn("Invalid command type for async handler {}: {}", 
                        handlerClass.simpleName, commandType.simpleName)
                }
            } else {
                logger.warn("Could not resolve generic types for async command handler: {}", handlerClass.simpleName)
            }
        } catch (e: Exception) {
            logger.error("Error registering async command handler: {}", handler::class.simpleName, e)
        }
    }
    
    /**
     * 注册同步查询处理器
     */
    @Suppress("UNCHECKED_CAST")
    private fun registerSyncQueryHandler(queryBus: DefaultQueryBus, handler: QueryHandler<*, *>) {
        try {
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, QueryHandler::class.java)
            
            if (genericTypes != null && genericTypes.size >= 2) {
                val queryType = genericTypes[0] as Class<out Query>
                val queryKClass = queryType.kotlin as KClass<Query>
                
                // 验证查询类型
                if (queryKClass.isSubclassOf(Query::class)) {
                    queryBus.registerHandler(queryKClass, handler as QueryHandler<Query, Any>)
                    logger.debug("Registered sync query handler: {} for query: {}", 
                        handlerClass.simpleName, queryType.simpleName)
                } else {
                    logger.warn("Invalid query type for handler {}: {}", 
                        handlerClass.simpleName, queryType.simpleName)
                }
            } else {
                logger.warn("Could not resolve generic types for query handler: {}", handlerClass.simpleName)
            }
        } catch (e: Exception) {
            logger.error("Error registering sync query handler: {}", handler::class.simpleName, e)
        }
    }
    
    /**
     * 注册异步查询处理器
     */
    @Suppress("UNCHECKED_CAST")
    private fun registerAsyncQueryHandler(queryBus: DefaultQueryBus, handler: AsyncQueryHandler<*, *>) {
        try {
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, AsyncQueryHandler::class.java)
            
            if (genericTypes != null && genericTypes.size >= 2) {
                val queryType = genericTypes[0] as Class<out Query>
                val queryKClass = queryType.kotlin as KClass<Query>
                
                // 验证查询类型
                if (queryKClass.isSubclassOf(Query::class)) {
                    queryBus.registerAsyncHandler(queryKClass, handler as AsyncQueryHandler<Query, Any>)
                    logger.debug("Registered async query handler: {} for query: {}", 
                        handlerClass.simpleName, queryType.simpleName)
                } else {
                    logger.warn("Invalid query type for async handler {}: {}", 
                        handlerClass.simpleName, queryType.simpleName)
                }
            } else {
                logger.warn("Could not resolve generic types for async query handler: {}", handlerClass.simpleName)
            }
        } catch (e: Exception) {
            logger.error("Error registering async query handler: {}", handler::class.simpleName, e)
        }
    }
    
    /**
     * 注册事件处理器
     */
    @Suppress("UNCHECKED_CAST")
    private fun registerEventHandler(eventBus: DefaultEventBus, handler: EventHandler<*>) {
        try {
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, EventHandler::class.java)
            
            if (genericTypes != null && genericTypes.isNotEmpty()) {
                val eventType = genericTypes[0] as Class<out DomainEvent>
                val eventKClass = eventType.kotlin as KClass<DomainEvent>
                
                // 验证事件类型
                if (eventKClass.isSubclassOf(DomainEvent::class)) {
                    eventBus.registerHandler(eventKClass, handler as EventHandler<DomainEvent>)
                    logger.debug("Registered event handler: {} for event: {}", 
                        handlerClass.simpleName, eventType.simpleName)
                } else {
                    logger.warn("Invalid event type for handler {}: {}", 
                        handlerClass.simpleName, eventType.simpleName)
                }
            } else {
                logger.warn("Could not resolve generic types for event handler: {}", handlerClass.simpleName)
            }
        } catch (e: Exception) {
            logger.error("Error registering event handler: {}", handler::class.simpleName, e)
        }
    }
}