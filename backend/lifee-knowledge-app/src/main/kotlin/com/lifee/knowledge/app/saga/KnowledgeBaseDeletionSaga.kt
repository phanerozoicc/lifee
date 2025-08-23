package com.lifee.knowledge.app.saga

import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.saga.*
import com.lifee.knowledge.domain.events.KnowledgeBaseDeletionRequestedEvent
import com.lifee.knowledge.domain.events.KnowledgeBaseDeletedEvent
import com.lifee.knowledge.domain.aggregates.KnowledgeBase
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.common.domain.valueobjects.UserId
import kotlin.reflect.KClass
import com.lifee.knowledge.app.services.KnowledgeBaseService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 知识库删除Saga
 * 协调知识库删除的完整流程：删除相关对话->清理推荐缓存->删除知识库
 */
@Component
class KnowledgeBaseDeletionSaga : Saga {
    override val id: String = "knowledge-base-deletion-saga"
    override val type: String = "KnowledgeBaseDeletion"
}

/**
 * 知识库删除Saga定义
 */
@Component
class KnowledgeBaseDeletionSagaDefinition(
    private val eventBus: EventBus,
    private val knowledgeBaseService: KnowledgeBaseService
) {
    
    fun createDefinition(): SagaDefinition {
        return SagaDefinition(
            name = "KnowledgeBaseDeletionSaga",
            description = "协调知识库删除的完整流程",
            steps = listOf(
                // 步骤1：发布删除对话事件
                SagaStep(
                    name = "PublishDeleteConversationsEvent",
                    description = "发布删除与知识库相关对话的事件",
                    action = PublishDeleteConversationsEventAction(eventBus),
                    compensationAction = PublishRestoreConversationsEventAction(eventBus)
                ),
                
                // 步骤2：发布清理推荐缓存事件
                SagaStep(
                    name = "PublishClearRecommendationCacheEvent",
                    description = "发布清理与知识库相关推荐缓存的事件",
                    action = PublishClearRecommendationCacheEventAction(eventBus),
                    compensationAction = PublishRestoreRecommendationCacheEventAction(eventBus)
                ),
                
                // 步骤3：删除知识库
                SagaStep(
                    name = "DeleteKnowledgeBase",
                    description = "删除知识库本身",
                    action = DeleteKnowledgeBaseAction(knowledgeBaseService, eventBus),
                    compensationAction = RestoreKnowledgeBaseAction(knowledgeBaseService)
                )
            ),
            eventTriggers = listOf(
                SagaEventTrigger(
                    eventType = KnowledgeBaseDeletionRequestedEvent::class,
                    correlationProperty = "knowledgeBaseId"
                )
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

// 发布删除对话事件的Action
class PublishDeleteConversationsEventAction(
    private val eventBus: EventBus
) : AbstractSagaAction() {
    
    private val logger = LoggerFactory.getLogger(PublishDeleteConversationsEventAction::class.java)
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val knowledgeBaseId = data["knowledgeBaseId"] as? String
                ?: return SagaStepResult.failed(Exception("Missing knowledgeBaseId"))
            
            logger.info("发布删除知识库 {} 相关对话的事件", knowledgeBaseId)
            
            // 发布删除对话事件
            val event = ConversationsDeletedEvent(
                userId = data["userId"] as? String ?: "",
                knowledgeBaseId = knowledgeBaseId,
                deletedAt = java.time.Instant.now()
            )
            eventBus.publish(event)
            
            logger.info("成功发布删除对话事件")
            
            SagaStepResult.success()
        } catch (e: Exception) {
            logger.error("发布删除对话事件失败", e)
            SagaStepResult.failed(Exception("发布删除对话事件失败: ${e.message}"))
        }
    }
}

/**
 * 对话删除事件
 */
data class ConversationsDeletedEvent(
    val userId: String,
    val knowledgeBaseId: String,
    val deletedAt: java.time.Instant
) : com.lifee.common.cqrs.events.Event

/**
 * 推荐缓存清理事件
 */
data class RecommendationCacheClearedEvent(
    val userId: String,
    val knowledgeBaseId: String,
    val clearedAt: java.time.Instant
) : com.lifee.common.cqrs.events.Event

/**
 * 对话恢复事件
 */
data class ConversationsRestoredEvent(
    val userId: String,
    val knowledgeBaseId: String,
    val restoredAt: java.time.Instant
) : com.lifee.common.cqrs.events.Event

/**
 * 推荐缓存恢复事件
 */
data class RecommendationCacheRestoredEvent(
    val userId: String,
    val knowledgeBaseId: String,
    val restoredAt: java.time.Instant
) : com.lifee.common.cqrs.events.Event

// 发布清理推荐缓存事件的Action
class PublishClearRecommendationCacheEventAction(
    private val eventBus: EventBus
) : AbstractSagaAction() {
    
    private val logger = LoggerFactory.getLogger(PublishClearRecommendationCacheEventAction::class.java)
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val knowledgeBaseId = data["knowledgeBaseId"] as? String
                ?: return SagaStepResult.failed(Exception("Missing knowledgeBaseId"))
            
            logger.info("发布清理知识库 {} 推荐缓存的事件", knowledgeBaseId)
            
            // 发布清理推荐缓存事件
            val event = RecommendationCacheClearedEvent(
                userId = data["userId"] as? String ?: "",
                knowledgeBaseId = knowledgeBaseId,
                clearedAt = java.time.Instant.now()
            )
            eventBus.publish(event)
             
             logger.info("成功发布清理推荐缓存事件")
            
            SagaStepResult.success()
        } catch (e: Exception) {
            logger.error("发布清理推荐缓存事件失败", e)
            SagaStepResult.failed(Exception("发布清理推荐缓存事件失败: ${e.message}"))
        }
    }
}

/**
 * 删除知识库动作
 */
class DeleteKnowledgeBaseAction(
    private val knowledgeBaseService: KnowledgeBaseService,
    private val eventBus: EventBus
) : AbstractSagaAction() {
    
    private val logger = LoggerFactory.getLogger(DeleteKnowledgeBaseAction::class.java)
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val deletionEvent = data["triggerEvent"] as? KnowledgeBaseDeletionRequestedEvent
                ?: throw IllegalStateException("KnowledgeBaseDeletionRequestedEvent not found in saga data")
            
            // 获取知识库信息用于补偿
            val knowledgeBase = knowledgeBaseService.getKnowledgeBase(deletionEvent.knowledgeBaseId)
                ?: throw IllegalStateException("Knowledge base not found: ${deletionEvent.knowledgeBaseId}")
            
            // 删除知识库
            knowledgeBaseService.deleteKnowledgeBase(deletionEvent.knowledgeBaseId)
            
            logger.info("Deleted knowledge base {}", deletionEvent.knowledgeBaseId)
            
            // 发布知识库删除事件
            val kbDeletedEvent = KnowledgeBaseDeletedEvent(
                knowledgeBaseId = deletionEvent.knowledgeBaseId.value.toString(),
                userId = deletionEvent.userId.value,
                name = knowledgeBase.getName().value,
                deletedAt = java.time.Instant.now()
            )
            
            eventBus.publish(kbDeletedEvent)
            
            SagaStepResult.success(
                mapOf(
                    "deletedKnowledgeBase" to knowledgeBase,
                    "knowledgeBaseDeletedEvent" to kbDeletedEvent
                ),
                "Successfully deleted knowledge base"
            )
            
        } catch (e: Exception) {
            logger.error("Failed to delete knowledge base: {}", e.message, e)
            SagaStepResult.failed(e)
        }
    }
}

// 发布恢复对话事件的补偿Action
class PublishRestoreConversationsEventAction(
    private val eventBus: EventBus
) : AbstractSagaAction() {
    
    private val logger = LoggerFactory.getLogger(PublishRestoreConversationsEventAction::class.java)
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val knowledgeBaseId = data["knowledgeBaseId"] as? String
                ?: return SagaStepResult.failed(Exception("Missing knowledgeBaseId"))
            
            val userId = data["userId"] as? String
                ?: return SagaStepResult.failed(Exception("Missing userId"))
            
            logger.info("发布恢复知识库 {} 相关对话的补偿事件", knowledgeBaseId)
            
            // 发布对话恢复事件
            val restoreEvent = ConversationsRestoredEvent(
                userId = userId,
                knowledgeBaseId = knowledgeBaseId,
                restoredAt = java.time.Instant.now()
            )
            
            eventBus.publish(restoreEvent)
            
            SagaStepResult.success()
        } catch (e: Exception) {
            logger.error("发布恢复对话补偿事件失败", e)
            SagaStepResult.failed(Exception("发布恢复对话补偿事件失败: ${e.message}"))
        }
    }
}

// 发布恢复推荐缓存事件的补偿Action
class PublishRestoreRecommendationCacheEventAction(
    private val eventBus: EventBus
) : AbstractSagaAction() {
    
    private val logger = LoggerFactory.getLogger(PublishRestoreRecommendationCacheEventAction::class.java)
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val knowledgeBaseId = data["knowledgeBaseId"] as? String
                ?: return SagaStepResult.failed(Exception("Missing knowledgeBaseId"))
            
            val userId = data["userId"] as? String
                ?: return SagaStepResult.failed(Exception("Missing userId"))
            
            logger.info("发布恢复知识库 {} 推荐缓存的补偿事件", knowledgeBaseId)
            
            // 发布推荐缓存恢复事件
            val restoreEvent = RecommendationCacheRestoredEvent(
                userId = userId,
                knowledgeBaseId = knowledgeBaseId,
                restoredAt = java.time.Instant.now()
            )
            
            eventBus.publish(restoreEvent)
            
            SagaStepResult.success()
        } catch (e: Exception) {
            logger.error("发布恢复推荐缓存补偿事件失败", e)
            SagaStepResult.failed(Exception("发布恢复推荐缓存补偿事件失败: ${e.message}"))
        }
    }
}

/**
 * 恢复知识库补偿动作
 */
class RestoreKnowledgeBaseAction(
    private val knowledgeBaseService: KnowledgeBaseService
) : AbstractSagaAction() {
    
    private val logger = LoggerFactory.getLogger(RestoreKnowledgeBaseAction::class.java)
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val deletedKnowledgeBase = data["deletedKnowledgeBase"]
                ?: return SagaStepResult.success(emptyMap(), "No knowledge base to restore")
            
            // 注意：实际场景中，知识库删除通常是不可逆的
            // 这里只是记录补偿尝试
            logger.warn("Attempted to restore deleted knowledge base, but deletion is irreversible")
            
            SagaStepResult.success(
                mapOf("restorationAttempted" to true),
                "Knowledge base restoration attempted (irreversible operation)"
            )
            
        } catch (e: Exception) {
            logger.error("Failed to restore knowledge base: {}", e.message, e)
            SagaStepResult.failed(e)
        }
    }
}