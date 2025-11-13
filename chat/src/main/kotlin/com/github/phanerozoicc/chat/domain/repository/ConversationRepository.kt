package com.github.phanerozoicc.chat.domain.repository

import com.github.phanerozoicc.chat.domain.model.Conversation


/**
 * 记录对话信息
 */
interface ConversationRepository {
    fun findNewByUserId(userId: String): Conversation
}