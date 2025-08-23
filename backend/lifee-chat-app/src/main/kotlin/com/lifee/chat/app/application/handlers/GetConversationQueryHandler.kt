package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.queries.GetConversationQuery
import com.lifee.chat.app.application.dtos.ConversationDetailDto
import com.lifee.chat.domain.exceptions.ConversationNotFoundException
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.common.cqrs.queries.AsyncQueryHandler
import com.lifee.common.domain.valueobjects.UserId as CommonUserId
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GetConversationQueryHandler(
    private val conversationRepository: ConversationRepository
) : AsyncQueryHandler<GetConversationQuery, ConversationDetailDto> {

    @Transactional(readOnly = true)
    override suspend fun handle(query: GetConversationQuery): ConversationDetailDto {
        val commonUserId = CommonUserId(query.userId.value.toString())
        val conversation = conversationRepository.findByIdAndUserId(query.conversationId, commonUserId)
            ?: throw ConversationNotFoundException(query.conversationId.value.toString())
        
        return ConversationDetailDto.fromDomain(conversation)
    }
}