package com.lifee.chat.app.application.commands

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.orchestration.BusinessFlowOrchestrator
import com.lifee.common.orchestration.BusinessFlowType
import com.lifee.chat.domain.aggregates.ConversationAggregate
import com.lifee.chat.domain.valueobjects.*
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.chat.domain.services.LLMService
import com.lifee.chat.domain.services.RAGService
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.common.domain.valueobjects.UserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 创建对话命令
 */
data class CreateConversationCommand(
    val userId: String,
    val title: String? = null,
    val knowledgeBaseId: String? = null,
    val modelConfig: ModelConfigurationDto
) : com.lifee.common.cqrs.commands.Command

/**
 * 发送消息命令
 */
data class SendMessageCommand(
    val conversationId: String,
    val userId: String,
    val content: String,
    val useRAG: Boolean = true,
    val attachments: List<MessageAttachmentDto> = emptyList()
) : com.lifee.common.cqrs.commands.Command

/**
 * 模型配置DTO
 */
data class ModelConfigurationDto(
    val modelName: String,
    val temperature: Double = 0.7,
    val maxTokens: Int = 2048,
    val topP: Double = 1.0,
    val frequencyPenalty: Double = 0.0,
    val presencePenalty: Double = 0.0
)

/**
 * 消息附件DTO
 */
data class MessageAttachmentDto(
    val id: String,
    val name: String,
    val type: String,
    val size: Long,
    val url: String
)

/**
 * 对话命令处理器
 * 协调完整的对话处理流程
 */
@Component
class ConversationCommandHandler(
    private val conversationRepository: ConversationRepository,
    private val llmService: LLMService,
    private val ragService: RAGService,
    private val businessFlowOrchestrator: BusinessFlowOrchestrator
) : AsyncCommandHandler<CreateConversationCommand, ConversationResult> {
    
    private val logger = LoggerFactory.getLogger(ConversationCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: CreateConversationCommand): ConversationResult {
        logger.info("Creating conversation for user: {}", command.userId)
        
        try {
            // 1. 验证输入
            validateCreateConversationInput(command)
            
            // 2. 创建对话
            val conversationId = ConversationId.generate()
            val userId = UserId.fromString(command.userId)
            val title = ConversationTitle(command.title ?: "新对话")
            val knowledgeBaseId = command.knowledgeBaseId?.let { KnowledgeBaseId.fromString(it) }
            val modelConfig = command.modelConfig.toDomain()
            
            val conversation = ConversationAggregate.create(
                id = conversationId,
                userId = userId,
                title = title,
                knowledgeBaseId = knowledgeBaseId,
                modelConfig = modelConfig
            )
            
            // 3. 保存对话
            conversationRepository.save(conversation)
            logger.info("Conversation created successfully with ID: {}", conversationId)
            
            // 4. 启动对话处理业务流程
            val flowId = businessFlowOrchestrator.startFlow(
                flowType = BusinessFlowType.CONVERSATION_PROCESSING,
                initiatorId = command.userId,
                flowData = mapOf(
                    "conversationId" to conversationId.toString(),
                    "userId" to command.userId,
                    "title" to title.value,
                    "knowledgeBaseId" to (knowledgeBaseId?.toString() ?: ""),
                    "modelConfig" to mapOf(
                        "modelName" to modelConfig.modelName,
                        "temperature" to modelConfig.temperature,
                        "maxTokens" to modelConfig.maxTokens
                    )
                )
            )
            
            return ConversationResult.success(
                conversationId = conversationId.toString(),
                flowId = flowId,
                message = "对话创建成功"
            )
            
        } catch (e: Exception) {
            logger.error("Error creating conversation: {}", e.message, e)
            return ConversationResult.failure("对话创建失败: ${e.message}")
        }
    }
    
    /**
     * 处理发送消息
     */
    suspend fun handleSendMessage(command: SendMessageCommand): MessageResult {
        logger.info("Sending message to conversation: {}", command.conversationId)
        
        try {
            // 1. 验证输入
            validateSendMessageInput(command)
            
            // 2. 加载对话
            val conversationId = ConversationId.fromString(command.conversationId)
            val conversation = conversationRepository.findById(conversationId)
                ?: return MessageResult.failure("对话不存在")
            
            // 3. 验证用户权限
            val userId = UserId.fromString(command.userId)
            if (conversation.getUserId() != userId) {
                return MessageResult.failure("无权限访问此对话")
            }
            
            // 4. 添加用户消息
            val userMessageId = MessageId.generate()
            val messageContent = MessageContent(command.content)
            val attachments = command.attachments.map { it.toDomain() }
            
            conversation.addUserMessage(
                messageId = userMessageId,
                content = messageContent,
                attachments = attachments
            )
            
            // 5. 生成助手回复
            val assistantResponse = generateAssistantResponse(
                conversation = conversation,
                userMessage = messageContent,
                useRAG = command.useRAG
            )
            
            // 6. 添加助手消息
            val assistantMessageId = MessageId.generate()
            conversation.addAssistantMessage(
                messageId = assistantMessageId,
                content = MessageContent(assistantResponse.content),
                ragContext = assistantResponse.ragContext,
                modelUsage = assistantResponse.modelUsage
            )
            
            // 7. 保存对话
            conversationRepository.save(conversation)
            
            logger.info("Message sent successfully to conversation: {}", conversationId)
            
            return MessageResult.success(
                userMessageId = userMessageId.toString(),
                assistantMessageId = assistantMessageId.toString(),
                assistantContent = assistantResponse.content,
                ragContext = assistantResponse.ragContext,
                modelUsage = assistantResponse.modelUsage
            )
            
        } catch (e: Exception) {
            logger.error("Error sending message: {}", e.message, e)
            return MessageResult.failure("消息发送失败: ${e.message}")
        }
    }
    
    /**
     * 生成助手回复
     */
    private suspend fun generateAssistantResponse(
        conversation: ConversationAggregate,
        userMessage: MessageContent,
        useRAG: Boolean
    ): AssistantResponse {
        val modelConfig = conversation.getModelConfig()
        val knowledgeBaseId = conversation.getKnowledgeBaseId()
        
        // 1. RAG增强（如果启用且有知识库）
        val ragContext = if (useRAG && knowledgeBaseId != null) {
            try {
                ragService.retrieveRelevantDocuments(
                    knowledgeBaseId = knowledgeBaseId,
                    query = userMessage.value,
                    limit = 5,
                    threshold = 0.7
                )
            } catch (e: Exception) {
                logger.warn("RAG retrieval failed: {}", e.message)
                null
            }
        } else {
            null
        }
        
        // 2. 构建对话历史
        val conversationHistory = buildConversationHistory(conversation)
        
        // 3. 调用LLM生成回复
        val llmResponse = llmService.generateResponse(
            messages = conversationHistory,
            userMessage = userMessage.value,
            ragContext = ragContext,
            modelConfig = modelConfig
        )
        
        return AssistantResponse(
            content = llmResponse.content,
            ragContext = ragContext,
            modelUsage = llmResponse.usage
        )
    }
    
    /**
     * 构建对话历史
     */
    private fun buildConversationHistory(conversation: ConversationAggregate): List<ChatMessage> {
        return conversation.getMessages().map { message ->
            ChatMessage(
                role = when (message.getType()) {
                    MessageType.USER -> "user"
                    MessageType.ASSISTANT -> "assistant"
                },
                content = message.getContent().value
            )
        }
    }
    
    /**
     * 验证创建对话输入
     */
    private fun validateCreateConversationInput(command: CreateConversationCommand) {
        require(command.userId.isNotBlank()) { "用户ID不能为空" }
        require(command.modelConfig.modelName.isNotBlank()) { "模型名称不能为空" }
        require(command.modelConfig.temperature in 0.0..2.0) { "温度参数必须在0.0到2.0之间" }
        require(command.modelConfig.maxTokens > 0) { "最大令牌数必须大于0" }
    }
    
    /**
     * 验证发送消息输入
     */
    private fun validateSendMessageInput(command: SendMessageCommand) {
        require(command.conversationId.isNotBlank()) { "对话ID不能为空" }
        require(command.userId.isNotBlank()) { "用户ID不能为空" }
        require(command.content.isNotBlank()) { "消息内容不能为空" }
        require(command.content.length <= 50000) { "消息内容长度不能超过50000个字符" }
    }
}

/**
 * 扩展函数：DTO转换为领域对象
 */
fun ModelConfigurationDto.toDomain(): ModelConfiguration {
    return ModelConfiguration(
        modelName = this.modelName,
        temperature = this.temperature,
        maxTokens = this.maxTokens,
        topP = this.topP,
        frequencyPenalty = this.frequencyPenalty,
        presencePenalty = this.presencePenalty
    )
}

fun MessageAttachmentDto.toDomain(): MessageAttachment {
    return MessageAttachment(
        id = this.id,
        name = this.name,
        type = AttachmentType.valueOf(this.type.uppercase()),
        size = this.size,
        url = this.url
    )
}

/**
 * 助手回复
 */
data class AssistantResponse(
    val content: String,
    val ragContext: RAGContext?,
    val modelUsage: ModelUsage?
)

/**
 * LLM回复
 */
data class LLMResponse(
    val content: String,
    val usage: ModelUsage?
)

/**
 * 聊天消息
 */
data class ChatMessage(
    val role: String,
    val content: String
)

/**
 * 对话操作结果
 */
data class ConversationResult(
    val success: Boolean,
    val conversationId: String? = null,
    val flowId: String? = null,
    val message: String,
    val error: String? = null
) {
    companion object {
        fun success(conversationId: String, flowId: String, message: String): ConversationResult {
            return ConversationResult(
                success = true,
                conversationId = conversationId,
                flowId = flowId,
                message = message
            )
        }
        
        fun failure(error: String): ConversationResult {
            return ConversationResult(
                success = false,
                message = "操作失败",
                error = error
            )
        }
    }
}

/**
 * 消息操作结果
 */
data class MessageResult(
    val success: Boolean,
    val userMessageId: String? = null,
    val assistantMessageId: String? = null,
    val assistantContent: String? = null,
    val ragContext: RAGContext? = null,
    val modelUsage: ModelUsage? = null,
    val message: String,
    val error: String? = null
) {
    companion object {
        fun success(
            userMessageId: String,
            assistantMessageId: String,
            assistantContent: String,
            ragContext: RAGContext?,
            modelUsage: ModelUsage?
        ): MessageResult {
            return MessageResult(
                success = true,
                userMessageId = userMessageId,
                assistantMessageId = assistantMessageId,
                assistantContent = assistantContent,
                ragContext = ragContext,
                modelUsage = modelUsage,
                message = "消息发送成功"
            )
        }
        
        fun failure(error: String): MessageResult {
            return MessageResult(
                success = false,
                message = "消息发送失败",
                error = error
            )
        }
    }
}