package com.lifee.chat.application.commands

import com.lifee.common.cqrs.commands.Command
import com.lifee.chat.domain.valueobjects.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * 创建对话命令
 */
data class CreateConversationCommand(
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId,
    
    @field:NotBlank(message = "对话标题不能为空")
    @field:Size(min = 1, max = 100, message = "对话标题长度必须在1-100个字符之间")
    val title: String
) : Command

/**
 * 发送消息命令
 */
data class SendMessageCommand(
    @field:NotNull(message = "对话ID不能为空")
    val conversationId: ConversationId,
    
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId,
    
    @field:NotBlank(message = "消息内容不能为空")
    @field:Size(max = 10000, message = "消息内容长度不能超过10000个字符")
    val content: String,
    
    @field:NotNull(message = "消息类型不能为空")
    val type: MessageType
) : Command

/**
 * 更新对话标题命令
 */
data class UpdateConversationTitleCommand(
    @field:NotNull(message = "对话ID不能为空")
    val conversationId: ConversationId,
    
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId,
    
    @field:NotBlank(message = "对话标题不能为空")
    @field:Size(min = 1, max = 100, message = "对话标题长度必须在1-100个字符之间")
    val title: String
) : Command

/**
 * 删除对话命令
 */
data class DeleteConversationCommand(
    @field:NotNull(message = "对话ID不能为空")
    val conversationId: ConversationId,
    
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId
) : Command

/**
 * 删除消息命令
 */
data class DeleteMessageCommand(
    @field:NotNull(message = "对话ID不能为空")
    val conversationId: ConversationId,
    
    @field:NotNull(message = "消息ID不能为空")
    val messageId: MessageId,
    
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId
) : Command