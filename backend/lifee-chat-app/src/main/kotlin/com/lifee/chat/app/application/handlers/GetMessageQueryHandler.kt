package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.queries.GetMessageQuery
import com.lifee.chat.app.application.dtos.MessageDto
import com.lifee.chat.domain.exceptions.ConversationNotFoundException
import com.lifee.chat.domain.exceptions.MessageNotFoundException
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.common.cqrs.QueryHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GetMessageQueryHandler(
    private val conversationRepository: ConversationRepository
) : QueryHandler<GetMessageQuery, MessageDto> {

    @Transactional(readOnly = true)
    override suspend fun handle(query: GetMessageQuery): MessageDto {
        val conversation = conversationRepository.findByIdAndUserId(query.conversationId, query.userId)
            ?: throw ConversationNotFoundException(query.conversationId)
        
        val message = conversation.messages.find { it.id == query.messageId }
            ?: throw MessageNotFoundException(query.messageId)
        
        return MessageDto.fromDomain(message)
    }
}