package com.lifee.chat.application.queries

import com.lifee.common.cqrs.queries.Query
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.chat.domain.valueobjects.MessageId
import com.lifee.chat.domain.valueobjects.UserId
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Min

/**
 * 获取对话详情查询
 */
data class GetConversationQuery(
    @field:NotNull(message = "对话ID不能为空")
    val conversationId: ConversationId,
    
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId
) : Query

/**
 * 获取用户对话列表查询
 */
data class GetUserConversationsQuery(
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId,
    
    @field:Min(value = 0, message = "页码不能小于0")
    val page: Int = 0,
    
    @field:Min(value = 1, message = "页大小不能小于1")
    val size: Int = 20
) : Query

/**
 * 获取对话消息列表查询
 */
data class GetConversationMessagesQuery(
    @field:NotNull(message = "对话ID不能为空")
    val conversationId: ConversationId,
    
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId,
    
    @field:Min(value = 0, message = "页码不能小于0")
    val page: Int = 0,
    
    @field:Min(value = 1, message = "页大小不能小于1")
    val size: Int = 50
) : Query

/**
 * 获取消息详情查询
 */
data class GetMessageQuery(
    @field:NotNull(message = "对话ID不能为空")
    val conversationId: ConversationId,
    
    @field:NotNull(message = "消息ID不能为空")
    val messageId: MessageId,
    
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId
) : Query