package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.queries.GetMessageQuery
import com.lifee.chat.app.application.dtos.MessageDto
import com.lifee.chat.domain.exceptions.ConversationNotFoundException
import com.lifee.chat.domain.exceptions.MessageNotFoundException
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.common.cqrs.queries.AsyncQueryHandler
import com.lifee.common.domain.valueobjects.UserId as CommonUserId
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GetMessageQueryHandler(
    private val conversationRepository: ConversationRepository
) : AsyncQueryHandler<GetMessageQuery, MessageDto> {

    @Transactional(readOnly = true)
    override suspend fun handle(query: GetMessageQuery): MessageDto {
        val conversationId = ConversationId(query.conversationId.value.toString())
        val commonUserId = CommonUserId(query.userId.value.toString())
        val conversation = conversationRepository.findByIdAndUserId(conversationId, commonUserId)
            ?: throw ConversationNotFoundException(query.conversationId.value.toString())
        
        val message = conversation.getMessages().find { it.id.value.toString() == query.messageId.value.toString() }
            ?: throw MessageNotFoundException(query.messageId.value.toString())
        
        return MessageDto.fromDomain(message)
    }
}