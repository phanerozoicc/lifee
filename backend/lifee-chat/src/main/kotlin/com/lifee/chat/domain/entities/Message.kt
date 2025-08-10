package com.lifee.chat.domain.entities

import com.lifee.chat.domain.valueobjects.*
import com.lifee.common.valueobjects.UserId
import java.time.Instant

/**
 * 消息实体
 */
data class Message(
    val id: MessageId,
    val content: MessageContent,
    val type: MessageType,
    val userId: UserId,
    val createdAt: Instant,
    val updatedAt: Instant? = null
) {
    
    companion object {
        /**
         * 创建新消息
         */
        fun create(
            content: MessageContent,
            type: MessageType,
            userId: UserId
        ): Message {
            return Message(
                id = MessageId.generate(),
                content = content,
                type = type,
                userId = userId,
                createdAt = Instant.now()
            )
        }
        
        /**
         * 创建用户消息
         */
        fun createUserMessage(content: MessageContent, userId: UserId): Message {
            return create(content, MessageType.USER, userId)
        }
        
        /**
         * 创建助手消息
         */
        fun createAssistantMessage(content: MessageContent, userId: UserId): Message {
            return create(content, MessageType.ASSISTANT, userId)
        }
        
        /**
         * 创建系统消息
         */
        fun createSystemMessage(content: MessageContent, userId: UserId): Message {
            return create(content, MessageType.SYSTEM, userId)
        }
    }
    
    /**
     * 更新消息内容
     */
    fun updateContent(newContent: MessageContent): Message {
        return copy(
            content = newContent,
            updatedAt = Instant.now()
        )
    }
    
    /**
     * 检查是否为用户消息
     */
    fun isUserMessage(): Boolean = type == MessageType.USER
    
    /**
     * 检查是否为助手消息
     */
    fun isAssistantMessage(): Boolean = type == MessageType.ASSISTANT
    
    /**
     * 检查是否为系统消息
     */
    fun isSystemMessage(): Boolean = type == MessageType.SYSTEM
    
    /**
     * 获取消息摘要
     */
    fun getSummary(): String = content.getSummary()
    
    /**
     * 检查消息是否已被修改
     */
    fun isModified(): Boolean = updatedAt != null
}