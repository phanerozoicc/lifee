package com.lifee.chat.domain.aggregates

import com.lifee.common.domain.EventSourcedAggregateRoot
import com.lifee.chat.domain.entities.Message
import com.lifee.chat.domain.events.*
import com.lifee.chat.domain.valueobjects.*
import com.lifee.common.domain.valueobjects.UserId
// import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import java.time.Instant

/**
 * 对话聚合根
 * 管理对话的完整生命周期和消息流
 */
class ConversationAggregate private constructor(
    private val id: ConversationId,
    private val userId: UserId,
    private var title: ConversationTitle,
    // private var knowledgeBaseId: KnowledgeBaseId?,
    private var modelConfig: ModelConfiguration,
    private var status: ConversationStatus,
    private val createdAt: Instant,
    private var updatedAt: Instant,
    private val messages: MutableList<Message> = mutableListOf()
) : EventSourcedAggregateRoot<ConversationId>(id) {
    
    companion object {
        /**
         * 创建新对话
         */
        fun create(
            id: ConversationId,
            userId: UserId,
            title: ConversationTitle,
            // knowledgeBaseId: KnowledgeBaseId? = null,
            modelConfig: ModelConfiguration
        ): ConversationAggregate {
            val now = Instant.now()
            
            val conversation = ConversationAggregate(
                id = id,
                userId = userId,
                title = title,
                // knowledgeBaseId = knowledgeBaseId,
                modelConfig = modelConfig,
                status = ConversationStatus.ACTIVE,
                createdAt = now,
                updatedAt = now
            )
            
            // 发布对话创建事件
            conversation.recordEvent(
                ConversationCreatedEvent(
                    conversationId = id,
                    title = title,
                    userId = userId,
                    createdAt = now
                )
            )
            
            return conversation
        }
    }
    
    // Getters
    fun getConversationId(): ConversationId = id
    fun getUserId(): UserId = userId
    fun getTitle(): ConversationTitle = title
    // fun getKnowledgeBaseId(): KnowledgeBaseId? = knowledgeBaseId
    fun getModelConfig(): ModelConfiguration = modelConfig
    fun getStatus(): ConversationStatus = status
    fun getCreatedAt(): Instant = createdAt
    fun getUpdatedAt(): Instant = updatedAt
    fun getMessages(): List<Message> = messages.toList()
    fun getMessageCount(): Int = messages.size
    
    /**
     * 添加用户消息
     */
    fun addUserMessage(
        messageId: MessageId,
        content: MessageContent,
        attachments: List<MessageAttachment> = emptyList()
    ) {
        require(status == ConversationStatus.ACTIVE) {
            "只有活跃状态的对话才能添加消息"
        }
        
        val message = Message.createUserMessage(content, userId)
        
        messages.add(message)
        updatedAt = Instant.now()
        
        recordEvent(
            MessageAddedEvent(
                conversationId = id,
                messageId = message.id,
                messageType = MessageType.USER,
                userId = userId,
                addedAt = updatedAt
            )
        )
    }
    
    /**
     * 添加助手消息
     */
    fun addAssistantMessage(
        messageId: MessageId,
        content: MessageContent,
        ragContext: RAGContext? = null,
        modelUsage: ModelUsage? = null
    ) {
        require(status == ConversationStatus.ACTIVE) {
            "只有活跃状态的对话才能添加消息"
        }
        
        val message = Message.createAssistantMessage(content, userId)
        
        messages.add(message)
        updatedAt = Instant.now()
        
        recordEvent(
            MessageAddedEvent(
                conversationId = id,
                messageId = message.id,
                messageType = MessageType.ASSISTANT,
                userId = userId,
                addedAt = updatedAt
            )
        )
    }
    
    /**
     * 更新对话标题
     */
    fun updateTitle(newTitle: ConversationTitle) {
        require(status != ConversationStatus.DELETED) {
            "已删除的对话不能更新标题"
        }
        
        val oldTitle = title
        title = newTitle
        updatedAt = Instant.now()
        
        recordEvent(
            ConversationTitleUpdatedEvent(
                conversationId = id,
                oldTitle = oldTitle,
                newTitle = newTitle,
                userId = userId,
                updatedAt = updatedAt
            )
        )
    }
    
    /**
     * 更新模型配置
     */
    fun updateModelConfig(newModelConfig: ModelConfiguration) {
        require(status == ConversationStatus.ACTIVE) {
            "只有活跃状态的对话才能更新模型配置"
        }
        
        val oldModelConfig = modelConfig
        modelConfig = newModelConfig
        updatedAt = Instant.now()
        
        recordEvent(
            ConversationModelConfigUpdatedEvent(
                conversationId = id,
                oldConfig = oldModelConfig,
                newConfig = newModelConfig,
                userId = userId,
                updatedAt = updatedAt
            )
        )
    }
    
    /**
     * 归档对话
     */
    fun archive() {
        require(status == ConversationStatus.ACTIVE) {
            "只有活跃状态的对话才能归档"
        }
        
        val oldStatus = status
        status = ConversationStatus.ARCHIVED
        updatedAt = Instant.now()
        
        recordEvent(
            ConversationStatusChangedEvent(
                conversationId = id,
                oldStatus = oldStatus,
                newStatus = status,
                userId = userId,
                changedAt = updatedAt
            )
        )
    }
    
    /**
     * 恢复对话
     */
    fun restore() {
        require(status == ConversationStatus.ARCHIVED) {
            "只有归档状态的对话才能恢复"
        }
        
        val oldStatus = status
        status = ConversationStatus.ACTIVE
        updatedAt = Instant.now()
        
        recordEvent(
            ConversationStatusChangedEvent(
                conversationId = id,
                oldStatus = oldStatus,
                newStatus = status,
                userId = userId,
                changedAt = updatedAt
            )
        )
    }
    
    /**
     * 删除对话
     */
    fun delete() {
        require(status != ConversationStatus.DELETED) {
            "对话已经被删除"
        }
        
        val oldStatus = status
        status = ConversationStatus.DELETED
        updatedAt = Instant.now()
        
        recordEvent(
            ConversationDeletedEvent(
                conversationId = id,
                userId = userId,
                deletedAt = updatedAt,
                messageCount = messages.size
            )
        )
    }
    
    /**
     * 获取最后一条消息
     */
    fun getLastMessage(): Message? = messages.lastOrNull()
    
    /**
     * 获取用户消息数量
     */
    fun getUserMessageCount(): Int = messages.count { it.type == MessageType.USER }
    
    /**
     * 获取助手消息数量
     */
    fun getAssistantMessageCount(): Int = messages.count { it.type == MessageType.ASSISTANT }
    
    /**
     * 检查对话是否为空
     */
    fun isEmpty(): Boolean = messages.isEmpty()
    
    /**
     * 获取对话总字符数
     */
    fun getTotalCharacterCount(): Int = messages.sumOf { it.content.length }
    
    /**
     * 序列化聚合根状态
     */
    override fun serializeState(): Map<String, Any> {
        return mapOf(
            "id" to id.toString(),
            "userId" to userId.toString(),
            "title" to title.value,
            // "knowledgeBaseId" to (knowledgeBaseId?.toString() ?: ""),
            "modelConfig" to mapOf(
                "modelName" to modelConfig.modelName,
                "temperature" to modelConfig.temperature,
                "maxTokens" to modelConfig.maxTokens,
                "topP" to modelConfig.topP
            ),
            "status" to status.name,
            "createdAt" to createdAt.toString(),
            "updatedAt" to updatedAt.toString(),
            "messages" to messages.map { message ->
                mapOf(
                    "id" to message.id.toString(),
                    "type" to message.type.name,
                    "content" to message.content.value,
                    "createdAt" to message.createdAt.toString()
                )
            }
        )
    }
    
    /**
     * 反序列化聚合根状态
     */
    override fun deserializeState(stateData: Map<String, Any>) {
        try {
            // 清空当前消息
            messages.clear()
            
            // 恢复基本信息
            title = ConversationTitle(stateData["title"] as String)
            // val kbId = stateData["knowledgeBaseId"] as String
            // knowledgeBaseId = if (kbId.isNotEmpty()) KnowledgeBaseId.fromString(kbId) else null
            
            // 恢复模型配置
            @Suppress("UNCHECKED_CAST")
            val modelConfigData = stateData["modelConfig"] as Map<String, Any>
            modelConfig = ModelConfiguration(
                modelName = modelConfigData["modelName"] as String,
                temperature = (modelConfigData["temperature"] as Number).toDouble(),
                maxTokens = modelConfigData["maxTokens"] as Int,
                topP = (modelConfigData["topP"] as Number).toDouble()
            )
            
            status = ConversationStatus.valueOf(stateData["status"] as String)
            updatedAt = Instant.parse(stateData["updatedAt"] as String)
            
            // 恢复消息列表
            @Suppress("UNCHECKED_CAST")
            val messagesData = stateData["messages"] as List<Map<String, Any>>
            messagesData.forEach { messageData ->
                val messageId = MessageId.fromString(messageData["id"] as String)
                val messageType = MessageType.valueOf(messageData["type"] as String)
                val content = MessageContent(messageData["content"] as String)
                val createdAt = Instant.parse(messageData["createdAt"] as String)
                
                val message = when (messageType) {
                    MessageType.USER -> Message.createUserMessage(content, userId)
                    MessageType.ASSISTANT -> Message.createAssistantMessage(content, userId)
                    MessageType.SYSTEM -> Message.createSystemMessage(content, userId)
                }
                
                messages.add(message)
            }
            
        } catch (e: Exception) {
            throw IllegalStateException("Failed to deserialize conversation state", e)
        }
    }
    
    /**
     * 应用领域事件到聚合根
     */
    override fun applyEvent(event: com.lifee.common.domain.DomainEvent) {
        when (event) {
            is ConversationCreatedEvent -> {
                // 对话创建事件已在构造函数中处理
            }
            is MessageAddedEvent -> {
                // 消息添加事件已在相应方法中处理
            }
            is ConversationTitleUpdatedEvent -> {
                // 标题更新事件已在updateTitle方法中处理
            }
            is ConversationModelConfigUpdatedEvent -> {
                // 模型配置更新事件已在updateModelConfig方法中处理
            }
            is ConversationStatusChangedEvent -> {
                // 状态变更事件已在相应方法中处理
            }
            is ConversationDeletedEvent -> {
                // 删除事件已在delete方法中处理
            }
            else -> {
                // 忽略未知事件类型
            }
        }
    }
}