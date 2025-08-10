package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.commands.CreateConversationCommand
import com.lifee.chat.app.application.dtos.ConversationDto
import com.lifee.chat.domain.aggregates.Conversation
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.common.cqrs.CommandHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class CreateConversationCommandHandler(
    private val conversationRepository: ConversationRepository
) : CommandHandler<CreateConversationCommand, ConversationDto> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(CreateConversationCommandHandler::class.java)
    }

    @Transactional
    override suspend fun handle(command: CreateConversationCommand): ConversationDto {
        logger.info("处理创建对话命令: userId={}, title={}", 
                   command.userId.value, command.title?.value ?: "默认标题")
        
        try {
            val conversation = if (command.title != null) {
                Conversation.create(command.title, command.userId)
            } else {
                Conversation.createWithDefaultTitle(command.userId)
            }
            
            val savedConversation = conversationRepository.save(conversation)
            
            logger.info("成功创建对话: conversationId={}, userId={}", 
                       savedConversation.getId().value, command.userId.value)
            
            return ConversationDto.fromDomain(savedConversation)
        } catch (e: Exception) {
            logger.error("创建对话失败: userId={}, error={}", command.userId.value, e.message, e)
            throw e
        }
    }
}