package com.lifee.user.app.saga

import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.saga.*
import com.lifee.user.domain.events.UserRegisteredEvent
import com.lifee.user.domain.events.UserActivatedEvent
// import com.lifee.config.domain.events.UserConfigurationInitializedEvent
// import com.lifee.knowledge.domain.events.DefaultKnowledgeBaseCreatedEvent
import com.lifee.user.domain.events.WelcomeNotificationSentEvent
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * 用户注册Saga
 * 协调完整的用户注册流程：注册->初始化配置->创建默认知识库->发送欢迎通知
 */
@Component
class UserRegistrationSaga : Saga {
    override val id: String = "user-registration-saga"
    override val type: String = "UserRegistration"
}

/**
 * 用户注册Saga定义
 */
@Component
class UserRegistrationSagaDefinition(
    private val eventBus: EventBus
) {
    
    fun createDefinition(): SagaDefinition {
        return SagaDefinition(
            name = "UserRegistrationSaga",
            description = "协调用户注册的完整流程",
            steps = listOf(
                // 步骤1：等待用户注册事件
                SagaStep(
                    name = "WaitForUserRegistration",
                    description = "等待用户注册完成",
                    action = WaitForEventSagaAction(
                        eventType = UserRegisteredEvent::class,
                        timeout = 30000L // 30秒超时
                    ),
                    waitingForEvent = UserRegisteredEvent::class
                ),
                
                // 步骤2：等待配置初始化完成 - 暂时禁用
                /*
                SagaStep(
                    name = "WaitForConfigurationInitialization",
                    description = "等待用户配置初始化完成",
                    action = WaitForEventSagaAction(
                        eventType = UserConfigurationInitializedEvent::class,
                        timeout = 60000L // 60秒超时
                    ),
                    waitingForEvent = UserConfigurationInitializedEvent::class,
                    compensationAction = CompensateConfigurationAction()
                ),
                
                // 步骤3：等待默认知识库创建完成
                SagaStep(
                    name = "WaitForDefaultKnowledgeBaseCreation",
                    description = "等待默认知识库创建完成",
                    action = WaitForEventSagaAction(
                        eventType = DefaultKnowledgeBaseCreatedEvent::class,
                        timeout = 120000L // 120秒超时
                    ),
                    waitingForEvent = DefaultKnowledgeBaseCreatedEvent::class,
                    compensationAction = CompensateKnowledgeBaseAction()
                ),
                */
                
                // 步骤4：发送欢迎通知
                SagaStep(
                    name = "SendWelcomeNotification",
                    description = "发送欢迎通知给用户",
                    action = SendWelcomeNotificationAction(eventBus),
                    compensationAction = CompensateWelcomeNotificationAction()
                )
            ),
            eventTriggers = listOf(
                SagaEventTrigger(
                    eventType = UserRegisteredEvent::class,
                    correlationProperty = "userId"
                )
                /*,
                SagaEventTrigger(
                    eventType = UserConfigurationInitializedEvent::class,
                    correlationProperty = "userId"
                ),
                SagaEventTrigger(
                    eventType = DefaultKnowledgeBaseCreatedEvent::class,
                    correlationProperty = "userId"
                )
                */
            ),
            timeout = 300000L, // 5分钟总超时
            retryPolicy = SagaRetryPolicy(
                maxRetries = 3,
                retryDelay = 5000L,
                backoffMultiplier = 2.0
            )
        )
    }
}

/**
 * 发送欢迎通知动作
 */
class SendWelcomeNotificationAction(
    private val eventBus: EventBus
) : AbstractSagaAction() {
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val userRegisteredEvent = data["lastEvent"] as? UserRegisteredEvent
                ?: throw IllegalStateException("UserRegisteredEvent not found in saga data")
            
            // 创建欢迎通知事件
            val welcomeEvent = WelcomeNotificationSentEvent(
                userId = userRegisteredEvent.userId,
                notificationChannel = "email",
                message = "Welcome to Lifee! Your registration is complete and your account is ready to use."
            )
            
            // 发布事件
            eventBus.publish(welcomeEvent)
            
            SagaStepResult.success(
                mapOf("welcomeNotificationSent" to true),
                "Welcome notification sent successfully"
            )
            
        } catch (e: Exception) {
            SagaStepResult.failed(e)
        }
    }
}

/**
 * 补偿配置初始化动作
 */
class CompensateConfigurationAction : AbstractSagaAction() {
    
    private val logger = LoggerFactory.getLogger(CompensateConfigurationAction::class.java)
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val userRegisteredEvent = data["lastEvent"] as? UserRegisteredEvent
                ?: throw IllegalStateException("UserRegisteredEvent not found in saga data")
            
            // TODO: 实现配置清理逻辑
            // 例如：删除已创建的用户配置
            
            logger.info("Compensated user configuration for user {}", userRegisteredEvent.userId)
            
            SagaStepResult.success(
                mapOf("configurationCompensated" to true),
                "Configuration compensation completed"
            )
            
        } catch (e: Exception) {
            logger.error("Failed to compensate user configuration: {}", e.message, e)
            SagaStepResult.failed(e)
        }
    }
}

/**
 * 补偿知识库创建动作
 */
class CompensateKnowledgeBaseAction : AbstractSagaAction() {
    
    private val logger = LoggerFactory.getLogger(CompensateKnowledgeBaseAction::class.java)
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val userRegisteredEvent = data["lastEvent"] as? UserRegisteredEvent
                ?: throw IllegalStateException("UserRegisteredEvent not found in saga data")
            
            // TODO: 实现知识库清理逻辑
            // 例如：删除已创建的默认知识库
            
            logger.info("Compensated knowledge base for user {}", userRegisteredEvent.userId)
            
            SagaStepResult.success(
                mapOf("knowledgeBaseCompensated" to true),
                "Knowledge base compensation completed"
            )
            
        } catch (e: Exception) {
            logger.error("Failed to compensate knowledge base creation: {}", e.message, e)
            SagaStepResult.failed(e)
        }
    }
}

/**
 * 补偿欢迎通知动作
 */
class CompensateWelcomeNotificationAction : AbstractSagaAction() {
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            // 欢迎通知的补偿通常不需要做什么
            // 因为邮件已经发送，无法撤回
            // 可以记录补偿日志或发送取消通知
            
            SagaStepResult.success(
                mapOf("welcomeNotificationCompensated" to true),
                "Welcome notification compensation completed (no action needed)"
            )
            
        } catch (e: Exception) {
            SagaStepResult.failed(e)
        }
    }
}

/**
 * 用户注册Saga配置
 */
@Component
class UserRegistrationSagaConfig(
    private val sagaOrchestrator: com.lifee.common.saga.SagaOrchestrator,
    private val sagaDefinition: UserRegistrationSagaDefinition
) {
    
    @javax.annotation.PostConstruct
    fun registerSaga() {
        sagaOrchestrator.registerSaga(
            UserRegistrationSaga::class,
            sagaDefinition.createDefinition()
        )
    }
}