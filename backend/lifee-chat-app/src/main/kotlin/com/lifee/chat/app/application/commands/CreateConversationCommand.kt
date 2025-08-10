package com.lifee.chat.app.application.commands

import com.lifee.common.cqrs.commands.Command
import com.lifee.chat.domain.valueobjects.ConversationTitle
import com.lifee.chat.domain.valueobjects.UserId

/**
 * 创建对话命令
 */
data class CreateConversationCommand(
    val title: ConversationTitle?,
    val userId: UserId
) : Command