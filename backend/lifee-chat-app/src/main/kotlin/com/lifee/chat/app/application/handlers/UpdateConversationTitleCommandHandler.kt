package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.commands.UpdateConversationTitleCommand
import com.lifee.chat.app.application.dtos.ConversationDto
import com.lifee.chat.domain.exceptions.ConversationNotFoundException
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.domain.valueobjects.UserId as CommonUserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UpdateConversationTitleCommandHandler(
    private val conversationRepository: ConversationRepository
) : AsyncCommandHandler<UpdateConversationTitleCommand, ConversationDto> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(UpdateConversationTitleCommandHandler::class.java)
    }

    @Transactional
    override suspend fun handle(command: UpdateConversationTitleCommand): ConversationDto {
        logger.info("处理更新对话标题命令: conversationId={}, userId={}, newTitle={}", 
                   command.conversationId, command.userId.value, command.newTitle.value)
        
        try {
            val conversationId = ConversationId(command.conversationId.toString())
            val commonUserId = CommonUserId(command.userId.value.toString())
            val conversation = conversationRepository.findByIdAndUserId(conversationId, commonUserId)
                ?: throw ConversationNotFoundException(command.conversationId.toString())
            
            conversation.updateTitle(command.newTitle)
            val savedConversation = conversationRepository.save(conversation)
            
            logger.info("成功更新对话标题: conversationId={}, newTitle={}", 
                       command.conversationId, command.newTitle.value)
            
            return ConversationDto.fromDomain(savedConversation)
        } catch (e: Exception) {
            logger.error("更新对话标题失败: conversationId={}, userId={}, error={}", 
                        command.conversationId, command.userId.value, e.message, e)
            throw e
        }
    }
}