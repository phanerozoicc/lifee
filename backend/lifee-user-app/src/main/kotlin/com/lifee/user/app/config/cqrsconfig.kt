package com.lifee.user.app.config

import com.lifee.common.cqrs.commands.*
import com.lifee.common.cqrs.events.*
import com.lifee.common.cqrs.queries.*
import com.lifee.common.domain.DomainEvent
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.KafkaTemplate
import jakarta.annotation.PostConstruct
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.GenericTypeResolver
import org.slf4j.LoggerFactory

/**
 * CQRS配置类
 */
@Configuration
class UserCqrsConfig(
    private val applicationContext: ApplicationContext,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    
    private val logger = LoggerFactory.getLogger(UserCqrsConfig::class.java)
    
    /**
     * 命令总线Bean
     */
    @Bean
    fun commandBus(): CommandBus {
        return DefaultCommandBus()
    }
    
    /**
     * 查询总线Bean
     */
    @Bean
    fun queryBus(): QueryBus {
        return DefaultQueryBus()
    }
    
    /**
     * 事件总线Bean
     */
    @Bean
    fun eventBus(objectMapper: ObjectMapper): EventBus {
        return DefaultEventBus(kafkaTemplate, objectMapper)
    }
    
    /**
     * 初始化CQRS处理器注册
     * 自动注册所有的命令处理器、查询处理器和事件处理器
     */
    @PostConstruct
    fun initializeCqrsHandlers() {
        registerCommandHandlers()
        registerAsyncCommandHandlers()
        registerQueryHandlers()
        registerAsyncQueryHandlers()
        registerEventHandlers()
        
        logger.info("用户模块CQRS配置初始化成功")
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun registerCommandHandlers() {
        val commandHandlers = applicationContext.getBeansOfType(CommandHandler::class.java)
        
        commandHandlers.values.forEach { handler ->
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, CommandHandler::class.java)
            
            if (genericTypes != null && genericTypes.isNotEmpty()) {
                val commandType = genericTypes[0] as Class<out Command>
                (commandBus() as DefaultCommandBus).registerHandler(commandType.kotlin as kotlin.reflect.KClass<Command>, handler as CommandHandler<Command, Any>)
                logger.info("注册命令处理器: {} for command: {}", 
                    handlerClass.simpleName, commandType.simpleName)
            }
        }
        
        logger.info("注册了 {} 个命令处理器", commandHandlers.size)
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun registerAsyncCommandHandlers() {
        val asyncCommandHandlers = applicationContext.getBeansOfType(AsyncCommandHandler::class.java)
        
        asyncCommandHandlers.values.forEach { handler ->
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, AsyncCommandHandler::class.java)
            
            if (genericTypes != null && genericTypes.isNotEmpty()) {
                val commandType = genericTypes[0] as Class<out Command>
                (commandBus() as DefaultCommandBus).registerAsyncHandler(commandType.kotlin as kotlin.reflect.KClass<Command>, handler as AsyncCommandHandler<Command, Any>)
                logger.info("注册异步命令处理器: {} for command: {}", 
                    handlerClass.simpleName, commandType.simpleName)
            }
        }
        
        logger.info("注册了 {} 个异步命令处理器", asyncCommandHandlers.size)
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun registerQueryHandlers() {
        val queryHandlers = applicationContext.getBeansOfType(QueryHandler::class.java)
        
        queryHandlers.values.forEach { handler ->
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, QueryHandler::class.java)
            
            if (genericTypes != null && genericTypes.size >= 2) {
                val queryType = genericTypes[0] as Class<out Query>
                (queryBus() as DefaultQueryBus).registerHandler(queryType.kotlin as kotlin.reflect.KClass<Query>, handler as QueryHandler<Query, Any>)
                logger.info("注册查询处理器: {} for query: {}", 
                    handlerClass.simpleName, queryType.simpleName)
            }
        }
        
        logger.info("注册了 {} 个查询处理器", queryHandlers.size)
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun registerAsyncQueryHandlers() {
        val asyncQueryHandlers = applicationContext.getBeansOfType(AsyncQueryHandler::class.java)
        
        asyncQueryHandlers.values.forEach { handler ->
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, AsyncQueryHandler::class.java)
            
            if (genericTypes != null && genericTypes.size >= 2) {
                val queryType = genericTypes[0] as Class<out Query>
                (queryBus() as DefaultQueryBus).registerAsyncHandler(queryType.kotlin as kotlin.reflect.KClass<Query>, handler as AsyncQueryHandler<Query, Any>)
                logger.info("注册异步查询处理器: {} for query: {}", 
                    handlerClass.simpleName, queryType.simpleName)
            }
        }
        
        logger.info("注册了 {} 个异步查询处理器", asyncQueryHandlers.size)
    }
    
    @Suppress("UNCHECKED_CAST")
    private fun registerEventHandlers() {
        val eventHandlers = applicationContext.getBeansOfType(EventHandler::class.java)
        
        eventHandlers.values.forEach { handler ->
            val handlerClass = handler::class.java
            val genericTypes = GenericTypeResolver.resolveTypeArguments(handlerClass, EventHandler::class.java)
            
            if (genericTypes != null && genericTypes.isNotEmpty()) {
                val eventType = genericTypes[0] as Class<out Event>
                (eventBus(applicationContext.getBean(ObjectMapper::class.java)) as DefaultEventBus).registerHandler(eventType.kotlin as kotlin.reflect.KClass<Event>, handler as EventHandler<Event>)
                logger.info("注册事件处理器: {} for event: {}", 
                    handlerClass.simpleName, eventType.simpleName)
            }
        }
        
        logger.info("注册了 {} 个事件处理器", eventHandlers.size)
    }
}