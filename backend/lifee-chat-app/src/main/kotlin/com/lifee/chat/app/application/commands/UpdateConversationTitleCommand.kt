package com.lifee.chat.app.application.commands

import com.lifee.common.cqrs.commands.Command
import com.lifee.chat.domain.valueobjects.*

/**
 * 更新对话标题命令
 */
data class UpdateConversationTitleCommand(
    val conversationId: ConversationId,
    val newTitle: ConversationTitle,
    val userId: UserId
) : Command