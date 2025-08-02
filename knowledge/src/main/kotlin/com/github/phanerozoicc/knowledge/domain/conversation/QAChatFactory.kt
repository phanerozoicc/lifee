package com.github.phanerozoicc.knowledge.domain.conversation

import org.springframework.stereotype.Component

@Component
class QAChatFactory {
    fun create(cmd: CreateChatCmd): Conversation {
        return ConversationImpl()
    }
}