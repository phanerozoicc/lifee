package com.lifee.chat.application.dtos

import com.lifee.chat.domain.aggregates.Conversation
import com.lifee.chat.domain.entities.Message
import com.lifee.chat.domain.valueobjects.MessageType
import java.time.Instant
import java.util.*

/**
 * 对话DTO
 */
data class ConversationDto(
    val id: UUID,
    val title: String,
    val userId: String,
    val messageCount: Int,
    val lastMessage: MessageDto?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun fromDomain(conversation: Conversation): ConversationDto {
            return ConversationDto(
                id = conversation.getConversationId().value,
                title = conversation.getTitle().value,
                userId = conversation.getUserId().value,
                messageCount = conversation.getMessageCount(),
                lastMessage = conversation.getLastMessage()?.let { MessageDto.fromDomain(it, conversation.getConversationId().value) },
                createdAt = conversation.getCreatedAt(),
                updatedAt = conversation.getUpdatedAt() ?: conversation.getCreatedAt()
            )
        }
    }
}

/**
 * 消息DTO
 */
data class MessageDto(
    val id: UUID,
    val content: String,
    val type: MessageType,
    val conversationId: UUID,
    val userId: String,
    val createdAt: Instant,
    val updatedAt: Instant?
) {
    companion object {
        fun fromDomain(message: Message, conversationId: UUID): MessageDto {
            return MessageDto(
                id = message.id.value,
                content = message.content.value,
                type = message.type,
                conversationId = conversationId,
                userId = message.userId.value,
                createdAt = message.createdAt,
                updatedAt = message.updatedAt
            )
        }
    }
}

/**
 * 对话详情DTO（包含消息列表）
 */
data class ConversationDetailDto(
    val id: UUID,
    val title: String,
    val userId: String,
    val messages: List<MessageDto>,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun fromDomain(conversation: Conversation): ConversationDetailDto {
            return ConversationDetailDto(
                id = conversation.getConversationId().value,
                title = conversation.getTitle().value,
                userId = conversation.getUserId().value,
                messages = conversation.getMessages().map { MessageDto.fromDomain(it, conversation.getConversationId().value) },
                createdAt = conversation.getCreatedAt(),
                updatedAt = conversation.getUpdatedAt() ?: conversation.getCreatedAt()
            )
        }
    }
}

/**
 * 分页对话列表DTO
 */
data class ConversationPageDto(
    val conversations: List<ConversationDto>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int
)