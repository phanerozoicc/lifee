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
    val userId: UUID,
    val messageCount: Int,
    val lastMessage: MessageDto?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun fromDomain(conversation: Conversation): ConversationDto {
            return ConversationDto(
                id = conversation.id.value,
                title = conversation.title.value,
                userId = conversation.userId.value,
                messageCount = conversation.getMessageCount(),
                lastMessage = conversation.getLastMessage()?.let { MessageDto.fromDomain(it) },
                createdAt = conversation.createdAt,
                updatedAt = conversation.updatedAt
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
    val userId: UUID,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun fromDomain(message: Message): MessageDto {
            return MessageDto(
                id = message.id.value,
                content = message.content.value,
                type = message.type,
                conversationId = message.conversationId.value,
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
    val userId: UUID,
    val messages: List<MessageDto>,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun fromDomain(conversation: Conversation): ConversationDetailDto {
            return ConversationDetailDto(
                id = conversation.id.value,
                title = conversation.title.value,
                userId = conversation.userId.value,
                messages = conversation.messages.map { MessageDto.fromDomain(it) },
                createdAt = conversation.createdAt,
                updatedAt = conversation.updatedAt
            )
        }
    }
}

/**
 * 分页对话列表DTO
 */
data class ConversationPageDto(
    val