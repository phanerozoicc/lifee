package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.queries.GetConversationMessagesQuery
import com.lifee.chat.app.application.dtos.MessageDto
import com.lifee.chat.domain.exceptions.ConversationNotFoundException
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.common.cqrs.QueryHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GetConversationMessagesQueryHandler(
    private val conversationRepository: ConversationRepository
) : QueryHandler<GetConversationMessagesQuery, List<MessageDto>> {

    @Transactional(readOnly = true)
    override suspend fun handle(query: GetConversationMessagesQuery): List<MessageDto> {
        val conversation = conversationRepository.findByIdAndUserId(query.conversationId, query.userId)
            ?: throw ConversationNotFoundException(query.conversationId)
        
        return conversation.messages
            .sortedBy { it.createdAt }
            .map { MessageDto.fromDomain(it) }
    }
}