package com.lifee.chat.app.application.commands

import com.lifee.chat.domain.valueobjects.ConversationTitle
import com.lifee.chat.domain.valueobjects.UserId
import com.lifee.common.cqrs.commands.Command
import java.util.UUID

/**
 * 更新对话标题命令
 */
data class UpdateConversationTitleCommand(
    val conversationId: UUID,
    val newTitle: ConversationTitle,
    val userId: UserId
) : Command