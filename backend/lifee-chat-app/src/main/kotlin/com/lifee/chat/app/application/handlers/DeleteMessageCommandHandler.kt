package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.commands.DeleteMessageCommand
import com.lifee.chat.domain.exceptions.ConversationNotFoundException
import com.lifee.chat.domain.exceptions.MessageNotFoundException
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.common.cqrs.CommandHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DeleteMessageCommandHandler(
    private val conversationRepository: ConversationRepository
) : CommandHandler<DeleteMessageCommand, Unit> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(DeleteMessageCommandHandler::class.java)
    }

    @Transactional
    override suspend fun handle(command: DeleteMessageCommand) {
        logger.info("处理删除消息命令: conversationId={}, messageId={}, userId={}", 
                   command.conversationId.value, command.messageId.value, command.userId.value)
        
        try {
            val conversation = conversationRepository.findByIdAndUserId(command.conversationId, command.userId)
                ?: throw ConversationNotFoundException(command.conversationId)
            
            val message = conversation.messages.find { it.id == command.messageId }
                ?: throw MessageNotFoundException(command.messageId)
            
            conversation.removeMessage(command.messageId, command.userId)
            conversationRepository.save(conversation)
            
            logger.info("成功删除消息: conversationId={}, messageId={}", 
                       command.conversationId.value, command.messageId.value)
        } catch (e: Exception) {
            logger.error("删除消息失败: conversationId={}, messageId={}, userId={}, error={}", 
                        command.conversationId.value, command.messageId.value, command.userId.value, e.message, e)
            throw e
        }
    }
}