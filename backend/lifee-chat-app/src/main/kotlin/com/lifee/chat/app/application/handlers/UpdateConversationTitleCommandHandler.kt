package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.commands.UpdateConversationTitleCommand
import com.lifee.chat.app.application.dtos.ConversationDto
import com.lifee.chat.domain.exceptions.ConversationNotFoundException
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.common.cqrs.CommandHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class UpdateConversationTitleCommandHandler(
    private val conversationRepository: ConversationRepository
) : CommandHandler<UpdateConversationTitleCommand, ConversationDto> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(UpdateConversationTitleCommandHandler::class.java)
    }

    @Transactional
    override suspend fun handle(command: UpdateConversationTitleCommand): ConversationDto {
        logger.info("处理更新对话标题命令: conversationId={}, userId={}, newTitle={}", 
                   command.conversationId.value, command.userId.value, command.newTitle.value)
        
        try {
            val conversation = conversationRepository.findByIdAndUserId(command.conversationId, command.userId)
                ?: throw ConversationNotFoundException(command.conversationId)
            
            conversation.updateTitle(command.newTitle, command.userId)
            val savedConversation = conversationRepository.save(conversation)
            
            logger.info("成功更新对话标题: conversationId={}, newTitle={}", 
                       command.conversationId.value, command.newTitle.value)
            
            return ConversationDto.fromDomain(savedConversation)
        } catch (e: Exception) {
            logger.error("更新对话标题失败: conversationId={}, userId={}, error={}", 
                        command.conversationId.value, command.userId.value, e.message, e)
            throw e
        }
    }
}