package com.lifee.common.orchestration

import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.domain.DomainEvent
import com.lifee.common.saga.SagaOrchestrator
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * 业务流程协调器
 * 负责协调跨服务的业务流程，确保数据流向清晰
 */
@Service
class BusinessFlowOrchestrator(
    private val eventBus: EventBus,
    private val sagaOrchestrator: SagaOrchestrator
) {
    
    private val logger = LoggerFactory.getLogger(BusinessFlowOrchestrator::class.java)
    private val activeFlows = ConcurrentHashMap<String, BusinessFlow>()
    
    /**
     * 启动业务流程
     */
    suspend fun startFlow(
        flowType: BusinessFlowType,
        flowId: String = UUID.randomUUID().toString(),
        initiatorId: String,
        flowData: Map<String, Any> = emptyMap()
    ): String {
        logger.info("Starting business flow: {} with ID: {}", flowType, flowId)
        
        val flow = BusinessFlow(
            id = flowId,
            type = flowType,
            initiatorId = initiatorId,
            status = FlowStatus.STARTED,
            data = flowData.toMutableMap(),
            startedAt = Instant.now()
        )
        
        activeFlows[flowId] = flow
        
        // 根据流程类型启动相应的Saga
        when (flowType) {
            BusinessFlowType.USER_REGISTRATION -> {
                // 启动用户注册Saga
                logger.info("Starting user registration saga for flow {}", flowId)
                handleUserRegistrationFlow(flow)
            }
            BusinessFlowType.KNOWLEDGE_BASE_CREATION -> {
                // 启动知识库创建流程
                handleKnowledgeBaseCreationFlow(flow)
            }
            BusinessFlowType.CONVERSATION_PROCESSING -> {
                // 启动对话处理流程
                handleConversationProcessingFlow(flow)
            }
            BusinessFlowType.KNOWLEDGE_BASE_DELETION -> {
                // 启动知识库删除Saga
                logger.info("Starting knowledge base deletion saga for flow {}", flowId)
                handleKnowledgeBaseDeletionFlow(flow)
            }
        }
        
        return flowId
    }
    
    /**
     * 处理业务流程事件
     */
    suspend fun handleFlowEvent(flowId: String, event: DomainEvent) {
        val flow = activeFlows[flowId]
        if (flow == null) {
            logger.warn("Received event for unknown flow: {}", flowId)
            return
        }
        
        logger.debug("Handling event {} for flow {}", event::class.simpleName, flowId)
        
        // 更新流程数据
        flow.addEvent(event)
        flow.updatedAt = Instant.now()
        
        // 根据事件类型和流程类型进行处理
        when (flow.type) {
            BusinessFlowType.USER_REGISTRATION -> {
                handleUserRegistrationEvent(flow, event)
            }
            BusinessFlowType.KNOWLEDGE_BASE_CREATION -> {
                handleKnowledgeBaseCreationEvent(flow, event)
            }
            BusinessFlowType.CONVERSATION_PROCESSING -> {
                handleConversationProcessingEvent(flow, event)
            }
            BusinessFlowType.KNOWLEDGE_BASE_DELETION -> {
                handleKnowledgeBaseDeletionEvent(flow, event)
            }
        }
    }
    
    /**
     * 完成业务流程
     */
    suspend fun completeFlow(flowId: String, result: FlowResult) {
        val flow = activeFlows[flowId]
        if (flow == null) {
            logger.warn("Attempting to complete unknown flow: {}", flowId)
            return
        }
        
        flow.status = if (result.success) FlowStatus.COMPLETED else FlowStatus.FAILED
        flow.result = result
        flow.completedAt = Instant.now()
        
        logger.info("Business flow {} completed with status: {}", flowId, flow.status)
        
        // 发布流程完成事件
        eventBus.publish(
            BusinessFlowCompletedEvent(
                flowId = flowId,
                flowType = flow.type,
                status = flow.status,
                result = result,
                completedAt = flow.completedAt!!
            )
        )
        
        // 从活跃流程中移除
        activeFlows.remove(flowId)
    }
    
    /**
     * 处理用户注册流程事件
     */
    private suspend fun handleUserRegistrationEvent(flow: BusinessFlow, event: DomainEvent) {
        when (event::class.simpleName) {
            "UserRegisteredEvent" -> {
                logger.info("User registration completed for flow {}", flow.id)
                flow.addStep("用户注册完成")
            }
            "UserConfigurationInitializedEvent" -> {
                logger.info("User configuration initialized for flow {}", flow.id)
                flow.addStep("用户配置初始化完成")
            }
            "DefaultKnowledgeBaseCreatedEvent" -> {
                logger.info("Default knowledge base created for flow {}", flow.id)
                flow.addStep("默认知识库创建完成")
            }
            "WelcomeNotificationSentEvent" -> {
                logger.info("Welcome notification sent for flow {}", flow.id)
                flow.addStep("欢迎通知发送完成")
                completeFlow(flow.id, FlowResult.success("用户注册流程完成"))
            }
        }
    }
    
    /**
     * 处理知识库创建流程事件
     */
    private suspend fun handleKnowledgeBaseCreationEvent(flow: BusinessFlow, event: DomainEvent) {
        when (event::class.simpleName) {
            "KnowledgeBaseCreatedEvent" -> {
                logger.info("Knowledge base created for flow {}", flow.id)
                flow.addStep("知识库创建完成")
                completeFlow(flow.id, FlowResult.success("知识库创建流程完成"))
            }
        }
    }
    
    /**
     * 处理对话处理流程事件
     */
    private suspend fun handleConversationProcessingEvent(flow: BusinessFlow, event: DomainEvent) {
        when (event::class.simpleName) {
            "ConversationCreatedEvent" -> {
                logger.info("Conversation created for flow {}", flow.id)
                flow.addStep("对话创建完成")
            }
            "MessageAddedEvent" -> {
                logger.info("Message added to conversation for flow {}", flow.id)
                flow.addStep("消息添加完成")
            }
            "ConversationCompletedEvent" -> {
                logger.info("Conversation completed for flow {}", flow.id)
                flow.addStep("对话处理完成")
                completeFlow(flow.id, FlowResult.success("对话处理流程完成"))
            }
        }
    }
    
    /**
     * 处理知识库删除流程事件
     */
    private suspend fun handleKnowledgeBaseDeletionEvent(flow: BusinessFlow, event: DomainEvent) {
        when (event::class.simpleName) {
            "ConversationsDeletedEvent" -> {
                logger.info("Related conversations deleted for flow {}", flow.id)
                flow.addStep("相关对话删除完成")
            }
            "RecommendationCacheClearedEvent" -> {
                logger.info("Recommendation cache cleared for flow {}", flow.id)
                flow.addStep("推荐缓存清理完成")
            }
            "KnowledgeBaseDeletedEvent" -> {
                logger.info("Knowledge base deleted for flow {}", flow.id)
                flow.addStep("知识库删除完成")
                completeFlow(flow.id, FlowResult.success("知识库删除流程完成"))
            }
        }
    }
    
    /**
     * 处理用户注册流程
     */
    private suspend fun handleUserRegistrationFlow(flow: BusinessFlow) {
        // 用户注册流程包括用户创建、配置初始化、知识库创建等
        logger.info("Handling user registration flow: {}", flow.id)
        flow.addStep("用户注册流程启动")
    }
    
    /**
     * 处理知识库创建流程
     */
    private suspend fun handleKnowledgeBaseCreationFlow(flow: BusinessFlow) {
        // 知识库创建流程相对简单，主要是创建知识库实体
        logger.info("Handling knowledge base creation flow: {}", flow.id)
        flow.addStep("知识库创建流程启动")
    }
    
    /**
     * 处理对话处理流程
     */
    private suspend fun handleConversationProcessingFlow(flow: BusinessFlow) {
        // 对话处理流程包括创建对话、处理消息、RAG增强等
        logger.info("Handling conversation processing flow: {}", flow.id)
        flow.addStep("对话处理流程启动")
    }
    
    /**
     * 处理知识库删除流程
     */
    private suspend fun handleKnowledgeBaseDeletionFlow(flow: BusinessFlow) {
        // 知识库删除流程包括删除相关对话、清理缓存、删除知识库等
        logger.info("Handling knowledge base deletion flow: {}", flow.id)
        flow.addStep("知识库删除流程启动")
    }
    
    /**
     * 获取流程状态
     */
    fun getFlowStatus(flowId: String): BusinessFlow? {
        return activeFlows[flowId]
    }
    
    /**
     * 获取所有活跃流程
     */
    fun getActiveFlows(): List<BusinessFlow> {
        return activeFlows.values.toList()
    }
}

/**
 * 业务流程类型
 */
enum class BusinessFlowType {
    USER_REGISTRATION,           // 用户注册流程
    KNOWLEDGE_BASE_CREATION,     // 知识库创建流程
    KNOWLEDGE_BASE_DELETION,     // 知识库删除流程
    CONVERSATION_PROCESSING      // 对话处理流程
}

/**
 * 流程状态
 */
enum class FlowStatus {
    STARTED,    // 已启动
    RUNNING,    // 运行中
    COMPLETED,  // 已完成
    FAILED,     // 失败
    CANCELLED   // 已取消
}

/**
 * 业务流程
 */
data class BusinessFlow(
    val id: String,
    val type: BusinessFlowType,
    val initiatorId: String,
    var status: FlowStatus,
    val data: MutableMap<String, Any>,
    val startedAt: Instant,
    var updatedAt: Instant = startedAt,
    var completedAt: Instant? = null,
    var result: FlowResult? = null,
    private val events: MutableList<DomainEvent> = mutableListOf(),
    private val steps: MutableList<String> = mutableListOf()
) {
    
    fun addEvent(event: DomainEvent) {
        events.add(event)
    }
    
    fun addStep(step: String) {
        steps.add(step)
    }
    
    fun getEvents(): List<DomainEvent> = events.toList()
    fun getSteps(): List<String> = steps.toList()
}

/**
 * 流程结果
 */
data class FlowResult(
    val success: Boolean,
    val message: String,
    val data: Map<String, Any> = emptyMap(),
    val error: Throwable? = null
) {
    companion object {
        fun success(message: String, data: Map<String, Any> = emptyMap()): FlowResult {
            return FlowResult(true, message, data)
        }
        
        fun failure(message: String, error: Throwable? = null, data: Map<String, Any> = emptyMap()): FlowResult {
            return FlowResult(false, message, data, error)
        }
    }
}

/**
 * 业务流程完成事件
 */
data class BusinessFlowCompletedEvent(
    val flowId: String,
    val flowType: BusinessFlowType,
    val status: FlowStatus,
    val result: FlowResult,
    val completedAt: Instant,
    override val aggregateId: String = flowId,
    override val version: Long = 1,
    override val occurredOn: Instant = completedAt,
    override val eventId: UUID = UUID.randomUUID()
) : DomainEvent(aggregateId, version, occurredOn, eventId) {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return copy(
            aggregateId = aggregateId,
            version = version,
            occurredOn = occurredOn,
            eventId = eventId
        )
    }
}