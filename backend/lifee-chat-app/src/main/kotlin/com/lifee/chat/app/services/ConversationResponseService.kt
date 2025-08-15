package com.lifee.chat.app.services

import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.chat.domain.valueobjects.MessageId
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.common.cqrs.events.EventBus
import com.lifee.chat.domain.events.MessageAddedEvent
import com.lifee.chat.domain.events.ResponseGeneratedEvent
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.user.domain.UserId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.system.measureTimeMillis

/**
 * 对话响应生成服务
 * 整合RAG检索和LLM调用，生成智能响应
 */
@Service
class ConversationResponseService(
    private val ragService: RAGService,
    private val llmService: LLMService,
    private val conversationRepository: ConversationRepository,
    private val eventBus: EventBus
) {
    
    private val logger = LoggerFactory.getLogger(ConversationResponseService::class.java)
    
    /**
     * 生成对话响应
     */
    suspend fun generateResponse(
        conversationId: ConversationId,
        userMessage: String,
        userId: UserId,
        knowledgeBaseIds: List<KnowledgeBaseId> = emptyList(),
        useRAG: Boolean = true
    ): ConversationResponse {
        logger.info("开始生成对话响应: conversationId={}, userId={}, useRAG={}", 
            conversationId.value, userId.value, useRAG)
        
        val totalTime = measureTimeMillis {
            try {
                // 1. 获取对话历史
                val conversation = conversationRepository.findById(conversationId)
                    ?: throw ConversationNotFoundException(conversationId)
                
                val conversationHistory = buildConversationHistory(conversation)
                
                // 2. RAG检索（如果启用）
                val retrievedDocuments = if (useRAG && knowledgeBaseIds.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        ragService.retrieveRelevantDocuments(
                            query = userMessage,
                            userId = userId,
                            knowledgeBaseIds = knowledgeBaseIds,
                            maxResults = 5
                        )
                    }
                } else {
                    emptyList()
                }
                
                logger.debug("RAG检索完成: 找到{}个相关文档", retrievedDocuments.size)
                
                // 3. LLM生成响应
                val llmResponse = withContext(Dispatchers.IO) {
                    llmService.generateResponse(
                        userMessage = userMessage,
                        context = retrievedDocuments,
                        conversationHistory = conversationHistory,
                        userId = userId
                    )
                }
                
                logger.debug("LLM响应生成完成: responseLength={}", llmResponse.content.length)
                
                // 4. 构建响应对象
                val response = ConversationResponse(
                    conversationId = conversationId,
                    content = llmResponse.content,
                    retrievedDocuments = retrievedDocuments,
                    tokensUsed = llmResponse.tokensUsed,
                    model = llmResponse.model,
                    processingTimeMs = 0L // 将在外层设置
                )
                
                // 5. 发布响应生成事件
                val responseGeneratedEvent = ResponseGeneratedEvent(
                    conversationId = conversationId,
                    userId = userId,
                    userMessage = userMessage,
                    assistantResponse = llmResponse.content,
                    retrievedDocumentCount = retrievedDocuments.size,
                    tokensUsed = llmResponse.tokensUsed,
                    processingTimeMs = 0L // 将在外层设置
                )
                eventBus.publish(responseGeneratedEvent)
                
                return@measureTimeMillis response
                
            } catch (e: Exception) {
                logger.error("对话响应生成失败: conversationId={}", conversationId.value, e)
                throw ConversationResponseException("响应生成失败: ${e.message}", e)
            }
        }
        
        logger.info("对话响应生成完成: conversationId={}, totalTime={}ms", 
            conversationId.value, totalTime)
        
        // 更新处理时间
        return ConversationResponse(
            conversationId = conversationId,
            content = "", // 这里需要从上面的结果中获取
            retrievedDocuments = emptyList(),
            tokensUsed = 0,
            model = "",
            processingTimeMs = totalTime
        )
    }
    
    /**
     * 流式生成响应
     */
    suspend fun generateStreamResponse(
        conversationId: ConversationId,
        userMessage: String,
        userId: UserId,
        knowledgeBaseIds: List<KnowledgeBaseId> = emptyList(),
        onChunk: (String) -> Unit
    ) {
        logger.info("开始流式生成响应: conversationId={}, userId={}", 
            conversationId.value, userId.value)
        
        try {
            // 1. 获取对话历史
            val conversation = conversationRepository.findById(conversationId)
                ?: throw ConversationNotFoundException(conversationId)
            
            val conversationHistory = buildConversationHistory(conversation)
            
            // 2. RAG检索
            val retrievedDocuments = if (knowledgeBaseIds.isNotEmpty()) {
                ragService.retrieveRelevantDocuments(
                    query = userMessage,
                    userId = userId,
                    knowledgeBaseIds = knowledgeBaseIds
                )
            } else {
                emptyList()
            }
            
            // 3. 流式LLM生成
            llmService.generateStreamResponse(
                userMessage = userMessage,
                context = retrievedDocuments,
                conversationHistory = conversationHistory,
                userId = userId,
                onChunk = onChunk
            )
            
        } catch (e: Exception) {
            logger.error("流式响应生成失败: conversationId={}", conversationId.value, e)
            throw ConversationResponseException("流式响应生成失败: ${e.message}", e)
        }
    }
    
    /**
     * 构建对话历史
     */
    private fun buildConversationHistory(conversation: com.lifee.chat.domain.aggregates.Conversation): List<ChatMessage> {
        return conversation.getMessages().takeLast(10).map { message ->
            ChatMessage(
                role = when (message.getType()) {
                    "USER" -> "user"
                    "ASSISTANT" -> "assistant"
                    else -> "user"
                },
                content = message.getContent().value
            )
        }
    }
}

/**
 * 对话响应
 */
data class ConversationResponse(
    val conversationId: ConversationId,
    val content: String,
    val retrievedDocuments: List<RetrievedDocument>,
    val tokensUsed: Int,
    val model: String,
    val processingTimeMs: Long
)

/**
 * 对话未找到异常
 */
class ConversationNotFoundException(conversationId: ConversationId) : 
    RuntimeException("对话不存在: ${conversationId.value}")

/**
 * 对话响应异常
 */
class ConversationResponseException(message: String, cause: Throwable? = null) : 
    RuntimeException(message, cause)