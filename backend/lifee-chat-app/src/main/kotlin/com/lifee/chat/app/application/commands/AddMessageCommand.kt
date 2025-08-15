package com.lifee.chat.app.application.commands

import com.lifee.common.cqrs.commands.Command
import com.lifee.chat.domain.valueobjects.*
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId

/**
 * 添加消息命令
 */
data class AddMessageCommand(
    val conversationId: ConversationId,
    val content: MessageContent,
    val type: MessageType,
    val userId: UserId,
    val knowledgeBaseIds: List<KnowledgeBaseId>? = null,
    val useRAG: Boolean? = true
) : Command