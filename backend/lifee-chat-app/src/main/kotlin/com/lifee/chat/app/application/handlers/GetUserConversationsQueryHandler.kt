package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.queries.GetUserConversationsQuery
import com.lifee.chat.app.application.dtos.ConversationPageDto
import com.lifee.chat.app.application.dtos.ConversationDto
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.common.cqrs.queries.AsyncQueryHandler
import com.lifee.common.domain.valueobjects.UserId as CommonUserId
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class GetUserConversationsQueryHandler(
    private val conversationRepository: ConversationRepository
) : AsyncQueryHandler<GetUserConversationsQuery, ConversationPageDto> {

    @Transactional(readOnly = true)
    override suspend fun handle(query: GetUserConversationsQuery): ConversationPageDto {
        val pageRequest = PageRequest.of(query.page, query.size)
        val commonUserId = CommonUserId(query.userId.value.toString())
        val conversationsPage = conversationRepository.findByUserId(commonUserId, pageRequest)
        
        val conversationDtos = conversationsPage.content.map { ConversationDto.fromDomain(it) }
        
        return ConversationPageDto(
            content = conversationDtos,
            totalElements = conversationsPage.totalElements,
            totalPages = conversationsPage.totalPages,
            size = conversationsPage.size,
            number = conversationsPage.number,
            numberOfElements = conversationsPage.numberOfElements,
            first = conversationsPage.isFirst,
            last = conversationsPage.isLast
        )
    }
}