package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.queries.GetConversationQuery
import com.lifee.chat.app.application.dtos.ConversationDetailDto
import com.lifee.chat.domain.exceptions.ConversationNotFoundException
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.common.cqrs.QueryHandler
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GetConversationQueryHandler(
    private val conversationRepository: ConversationRepository
) : QueryHandler<GetConversationQuery, ConversationDetailDto> {

    @Transactional(readOnly = true)
    override suspend fun handle(query: GetConversationQuery): ConversationDetailDto {
        val conversation = conversationRepository.findByIdAndUserId(query.conversationId, query.userId)
            ?: throw ConversationNotFoundException(query.conversationId)
        
        return ConversationDetailDto.fromDomain(conversation)
    }
}