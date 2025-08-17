package com.lifee.chat.domain.aggregates

import com.lifee.common.domain.EventSourcedAggregateRoot
import com.lifee.chat.domain.entities.Message
import com.lifee.chat.domain.events.*
import com.lifee.chat.domain.valueobjects.*
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 对话聚合根
 */
class Conversation(
    id: ConversationId,
    private var title: ConversationTitle,
    private val userId: UserId,
    private val createdAt: Instant,
    private var updatedAt: Instant? = null
) : EventSourcedAggregateRoot<ConversationId>(id) {
    
    private val messages = mutableListOf<Message>()
    
    companion object {
        /**
         * 创建新对话
         */
        fun create(
            title: ConversationTitle,
            userId: UserId
        ): Conversation {
            val conversationId = ConversationId.generate()
            val conversation = Conversation(
                id = conversationId,
                title = title,
                userId = userId,
                createdAt = Instant.now()
            )
            
            // 发布对话创建事件
            conversation.addDomainEvent(
                ConversationCreatedEvent.create(
                    conversationId = conversationId,
                    title = title,
                    userId = userId
                )
            )
            
            return conversation
        }
        
        /**
         * 创建带默认标题的对话
         */
        fun createWithDefaultTitle(userId: UserId): Conversation {
            return create(ConversationTitle.default(), userId)
        }
        
        /**
         * 从首条消息创建对话
         */
        fun createFromFirstMessage(
            userId: UserId,
            firstMessage: Message
        ): Conversation {
            val title = ConversationTitle.fromContent(firstMessage.content.value)
            val conversation = create(title, userId)
            conversation.addMessage(firstMessage)
            return conversation
        }
    }
    
    /**
     * 添加消息
     */
    fun addMessage(message: Message) {
        messages.add(message)
        updateTimestamp()
        
        // 如果是第一条用户消息且标题是默认标题，则更新标题
        if (messages.size == 1 && 
            message.isUserMessage() && 
            title.value == ConversationTitle.default().value) {
            updateTitle(ConversationTitle.fromContent(message.content.value))
        }
        
        // 发布消息添加事件
        addDomainEvent(
            MessageAddedEvent.create(
                conversationId = id,
                messageId = message.id,
                messageType = message.type,
                userId = userId
            )
        )
    }
    
    /**
     * 更新消息
     */
    fun updateMessage(messageId: MessageId, newContent: MessageContent) {
        val messageIndex = messages.indexOfFirst { it.id == messageId }
        if (messageIndex == -1) {
            throw IllegalArgumentException("消息不存在: $messageId")
        }
        
        val updatedMessage = messages[messageIndex].updateContent(newContent)
        messages[messageIndex] = updatedMessage
        updateTimestamp()
        
        // 发布消息更新事件
        addDomainEvent(
            MessageUpdatedEvent.create(
                conversationId = id,
                messageId = messageId,
                userId = userId
            )
        )
    }
    
    /**
     * 删除消息
     */
    fun removeMessage(messageId: MessageId) {
        val removed = messages.removeIf { it.id == messageId }
        if (!removed) {
            throw IllegalArgumentException("消息不存在: $messageId")
        }
        
        updateTimestamp()
        
        // 发布消息删除事件
        addDomainEvent(
            MessageDeletedEvent.create(
                conversationId = id,
                messageId = messageId,
                userId = userId
            )
        )
    }
    
    /**
     * 更新对话标题
     */
    fun updateTitle(newTitle: ConversationTitle) {
        if (title != newTitle) {
            val oldTitle = title
            title = newTitle
            updateTimestamp()
            
            // 发布标题更新事件
            addDomainEvent(
                ConversationTitleUpdatedEvent.create(
                    conversationId = id,
                    oldTitle = oldTitle,
                    newTitle = newTitle,
                    userId = userId
                )
            )
        }
    }
    
    /**
     * 获取所有消息
     */
    fun getMessages(): List<Message> = messages.toList()
    
    /**
     * 获取指定类型的消息
     */
    fun getMessagesByType(type: MessageType): List<Message> {
        return messages.filter { it.type == type }
    }
    
    /**
     * 获取最后一条消息
     */
    fun getLastMessage(): Message? = messages.lastOrNull()
    
    /**
     * 获取消息数量
     */
    fun getMessageCount(): Int = messages.size
    
    /**
     * 检查是否为空对话
     */
    fun isEmpty(): Boolean = messages.isEmpty()
    
    /**
     * 检查用户是否有权限访问
     */
    fun checkOwnership(requestUserId: UserId) {
        if (userId != requestUserId) {
            throw IllegalArgumentException("用户无权限访问此对话")
        }
    }
    
    /**
     * 获取对话信息
     */
    fun getTitle(): ConversationTitle = title
    fun getUserId(): UserId = userId
    fun getCreatedAt(): Instant = createdAt
    fun getUpdatedAt(): Instant? = updatedAt
    
    /**
     * 更新时间戳
     */
    private fun updateTimestamp() {
        updatedAt = Instant.now()
    }
    
    /**
     * 序列化聚合根状态
     */
    override fun serializeState(): Map<String, Any> {
        return mapOf(
            "id" to id.toString(),
            "title" to title.toString(),
            "userId" to userId.toString(),
            "createdAt" to createdAt.toString(),
            "updatedAt" to (updatedAt?.toString() ?: ""),
            "messages" to messages.map { message ->
                mapOf(
                    "id" to message.id.toString(),
                    "content" to message.content.toString(),
                    "type" to message.type.toString(),
                    "userId" to message.userId.toString(),
                    "createdAt" to message.createdAt.toString(),
                    "updatedAt" to (message.updatedAt?.toString() ?: "")
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
            title = ConversationTitle.of(stateData["title"] as String)
            
            // 恢复时间戳
            val updatedAtStr = stateData["updatedAt"] as? String
            if (!updatedAtStr.isNullOrEmpty()) {
                updatedAt = Instant.parse(updatedAtStr)
            }
            
            // 恢复消息
            @Suppress("UNCHECKED_CAST")
            val messagesData = stateData["messages"] as? List<Map<String, Any>> ?: emptyList()
            
            messagesData.forEach { msgData ->
                try {
                    val msgId = MessageId.of(msgData["id"] as String)
                    val content = MessageContent.of(msgData["content"] as String)
                    val type = MessageType.valueOf(msgData["type"] as String)
                    val msgUserId = UserId.of(msgData["userId"] as String)
                    val createdAt = Instant.parse(msgData["createdAt"] as String)
                    val updatedAtStr = msgData["updatedAt"] as? String
                    val updatedAt = if (!updatedAtStr.isNullOrEmpty()) {
                        Instant.parse(updatedAtStr)
                    } else null
                    
                    val message = Message.create(
                        id = msgId,
                        content = content,
                        type = type,
                        userId = msgUserId,
                        createdAt = createdAt,
                        updatedAt = updatedAt
                    )
                    
                    messages.add(message)
                } catch (e: Exception) {
                    // 记录错误但继续处理其他消息
                    // 在实际应用中可能需要更严格的错误处理
                }
            }
            
        } catch (e: Exception) {
            // 在实际应用中需要更严格的错误处理
            throw IllegalStateException("Failed to deserialize Conversation state", e)
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
                // 消息添加事件已在addMessage方法中处理
            }
            is MessageUpdatedEvent -> {
                // 消息更新事件已在updateMessage方法中处理
            }
            is MessageDeletedEvent -> {
                // 消息删除事件已在removeMessage方法中处理
            }
            is ConversationTitleUpdatedEvent -> {
                // 标题更新事件已在updateTitle方法中处理
            }
            // 可以根据需要添加更多事件处理
        }
    }
}