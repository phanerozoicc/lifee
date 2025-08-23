package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.queries.GetConversationMessagesQuery
import com.lifee.chat.app.application.dtos.MessageDto
import com.lifee.chat.domain.exceptions.ConversationNotFoundException
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.common.cqrs.queries.AsyncQueryHandler
import com.lifee.common.domain.valueobjects.UserId as CommonUserId
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GetConversationMessagesQueryHandler(
    private val conversationRepository: ConversationRepository
) : AsyncQueryHandler<GetConversationMessagesQuery, List<MessageDto>> {

    @Transactional(readOnly = true)
    override suspend fun handle(query: GetConversationMessagesQuery): List<MessageDto> {
        val commonUserId = CommonUserId(query.userId.value.toString())
        val conversation = conversationRepository.findByIdAndUserId(query.conversationId, commonUserId)
            ?: throw ConversationNotFoundException(query.conversationId.value.toString())
        
        return conversation.getMessages()
            .drop(query.offset)
            .take(query.limit)
            .map { MessageDto.fromDomain(it) }
    }
}