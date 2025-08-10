package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.queries.GetUserConversationsQuery
import com.lifee.chat.app.application.dtos.ConversationPageDto
import com.lifee.chat.app.application.dtos.ConversationDto
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.common.cqrs.QueryHandler
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GetUserConversationsQueryHandler(
    private val conversationRepository: ConversationRepository
) : QueryHandler<GetUserConversationsQuery, ConversationPageDto> {

    @Transactional(readOnly = true)
    override suspend fun handle(query: GetUserConversationsQuery): ConversationPageDto {
        val pageRequest = PageRequest.of(query.page, query.size)
        val conversationsPage = conversationRepository.findByUserId(query.userId, pageRequest)
        
        val conversationDtos = conversationsPage.content.map { ConversationDto.fromDomain(it) }
        
        return ConversationPageDto(
            content = conversationDtos,
            page = conversationsPage.number,
            size = conversationsPage.size,
            totalElements = conversationsPage.totalElements,
            totalPages = conversationsPage.totalPages,
            first = conversationsPage.isFirst,
            last = conversationsPage.isLast
        )
    }
}