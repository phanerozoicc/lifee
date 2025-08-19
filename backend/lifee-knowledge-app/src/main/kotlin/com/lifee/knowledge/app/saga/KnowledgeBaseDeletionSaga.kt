package com.lifee.knowledge.app.saga

import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.saga.*
import com.lifee.knowledge.domain.events.KnowledgeBaseDeletionRequestedEvent
import com.lifee.chat.domain.events.ConversationsDeletedEvent
import com.lifee.recommendation.domain.events.RecommendationCacheClearedEvent
import com.lifee.knowledge.domain.events.KnowledgeBaseDeletedEvent
import com.lifee.chat.app.services.ChatService
import com.lifee.recommendation.app.services.RecommendationService
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
    private val chatService: ChatService,
    private val recommendationService: RecommendationService,
    private val knowledgeBaseService: KnowledgeBaseService
) {
    
    fun createDefinition(): SagaDefinition {
        return SagaDefinition(
            name = "KnowledgeBaseDeletionSaga",
            description = "协调知识库删除的完整流程",
            steps = listOf(
                // 步骤1：删除相关对话
                SagaStep(
                    name = "DeleteRelatedConversations",
                    description = "删除与知识库相关的所有对话",
                    action = DeleteConversationsAction(chatService, eventBus),
                    compensationAction = RestoreConversationsAction(chatService)
                ),
                
                // 步骤2：清理推荐缓存
                SagaStep(
                    name = "ClearRecommendationCache",
                    description = "清理与知识库相关的推荐缓存",
                    action = ClearRecommendationCacheAction(recommendationService, eventBus),
                    compensationAction = RestoreRecommendationCacheAction(recommendationService)
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

/**
 * 删除对话动作
 */
class DeleteConversationsAction(
    private val chatService: ChatService,
    private val eventBus: EventBus
) : AbstractSagaAction() {
    
    private val logger = LoggerFactory.getLogger(DeleteConversationsAction::class.java)
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val deletionEvent = data["triggerEvent"] as? KnowledgeBaseDeletionRequestedEvent
                ?: throw IllegalStateException("KnowledgeBaseDeletionRequestedEvent not found in saga data")
            
            // 查找并删除相关对话
            val conversations = chatService.findConversationsByKnowledgeBase(deletionEvent.knowledgeBaseId)
            val deletedConversationIds = mutableListOf<String>()
            
            conversations.forEach { conversation ->
                chatService.deleteConversation(conversation.id)
                deletedConversationIds.add(conversation.id.toString())
                logger.info("Deleted conversation {} related to knowledge base {}", 
                    conversation.id, deletionEvent.knowledgeBaseId)
            }
            
            // 发布对话删除事件
            val conversationsDeletedEvent = ConversationsDeletedEvent(
                knowledgeBaseId = deletionEvent.knowledgeBaseId,
                deletedConversationIds = deletedConversationIds,
                deletedCount = deletedConversationIds.size
            )
            
            eventBus.publish(conversationsDeletedEvent)
            
            SagaStepResult.success(
                mapOf(
                    "deletedConversations" to deletedConversationIds,
                    "conversationsDeletedEvent" to conversationsDeletedEvent
                ),
                "Successfully deleted ${deletedConversationIds.size} conversations"
            )
            
        } catch (e: Exception) {
            logger.error("Failed to delete conversations: {}", e.message, e)
            SagaStepResult.failed(e)
        }
    }
}

/**
 * 清理推荐缓存动作
 */
class ClearRecommendationCacheAction(
    private val recommendationService: RecommendationService,
    private val eventBus: EventBus
) : AbstractSagaAction() {
    
    private val logger = LoggerFactory.getLogger(ClearRecommendationCacheAction::class.java)
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val deletionEvent = data["triggerEvent"] as? KnowledgeBaseDeletionRequestedEvent
                ?: throw IllegalStateException("KnowledgeBaseDeletionRequestedEvent not found in saga data")
            
            // 清理推荐缓存
            val clearedCacheKeys = recommendationService.clearKnowledgeBaseCache(deletionEvent.knowledgeBaseId)
            
            logger.info("Cleared {} recommendation cache entries for knowledge base {}", 
                clearedCacheKeys.size, deletionEvent.knowledgeBaseId)
            
            // 发布缓存清理事件
            val cacheCleared = RecommendationCacheClearedEvent(
                knowledgeBaseId = deletionEvent.knowledgeBaseId,
                clearedCacheKeys = clearedCacheKeys,
                clearedCount = clearedCacheKeys.size
            )
            
            eventBus.publish(cacheCleared)
            
            SagaStepResult.success(
                mapOf(
                    "clearedCacheKeys" to clearedCacheKeys,
                    "cacheCleared" to cacheCleared
                ),
                "Successfully cleared ${clearedCacheKeys.size} cache entries"
            )
            
        } catch (e: Exception) {
            logger.error("Failed to clear recommendation cache: {}", e.message, e)
            SagaStepResult.failed(e)
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
                knowledgeBaseId = deletionEvent.knowledgeBaseId,
                userId = knowledgeBase.userId,
                name = knowledgeBase.name
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

/**
 * 恢复对话补偿动作
 */
class RestoreConversationsAction(
    private val chatService: ChatService
) : AbstractSagaAction() {
    
    private val logger = LoggerFactory.getLogger(RestoreConversationsAction::class.java)
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val deletedConversations = data["deletedConversations"] as? List<String>
                ?: return SagaStepResult.success(emptyMap(), "No conversations to restore")
            
            // 注意：实际场景中，对话删除通常是不可逆的
            // 这里只是记录补偿尝试
            logger.warn("Attempted to restore {} deleted conversations, but conversation deletion is irreversible", 
                deletedConversations.size)
            
            SagaStepResult.success(
                mapOf("restorationAttempted" to true),
                "Conversation restoration attempted (irreversible operation)"
            )
            
        } catch (e: Exception) {
            logger.error("Failed to restore conversations: {}", e.message, e)
            SagaStepResult.failed(e)
        }
    }
}

/**
 * 恢复推荐缓存补偿动作
 */
class RestoreRecommendationCacheAction(
    private val recommendationService: RecommendationService
) : AbstractSagaAction() {
    
    private val logger = LoggerFactory.getLogger(RestoreRecommendationCacheAction::class.java)
    
    override suspend fun doExecute(data: Map<String, Any>): SagaStepResult {
        return try {
            val deletionEvent = data["triggerEvent"] as? KnowledgeBaseDeletionRequestedEvent
                ?: throw IllegalStateException("KnowledgeBaseDeletionRequestedEvent not found in saga data")
            
            // 重新构建推荐缓存
            recommendationService.rebuildKnowledgeBaseCache(deletionEvent.knowledgeBaseId)
            
            logger.info("Restored recommendation cache for knowledge base {}", 
                deletionEvent.knowledgeBaseId)
            
            SagaStepResult.success(
                mapOf("cacheRestored" to true),
                "Recommendation cache restored"
            )
            
        } catch (e: Exception) {
            logger.error("Failed to restore recommendation cache: {}", e.message, e)
            SagaStepResult.failed(e)
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