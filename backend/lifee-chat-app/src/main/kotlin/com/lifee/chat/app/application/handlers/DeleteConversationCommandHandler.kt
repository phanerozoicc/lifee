package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.commands.DeleteConversationCommand
import com.lifee.chat.domain.exceptions.ConversationNotFoundException
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.common.cqrs.CommandHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DeleteConversationCommandHandler(
    private val conversationRepository: ConversationRepository
) : CommandHandler<DeleteConversationCommand, Unit> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(DeleteConversationCommandHandler::class.java)
    }

    @Transactional
    override suspend fun handle(command: DeleteConversationCommand) {
        logger.info("处理删除对话命令: conversationId={}, userId={}", 
                   command.conversationId.value, command.userId.value)
        
        try {
            val conversation = conversationRepository.findByIdAndUserId(command.conversationId, command.userId)
                ?: throw ConversationNotFoundException(command.conversationId)
            
            conversationRepository.delete(conversation)
            
            logger.info("成功删除对话: conversationId={}, userId={}", 
                       command.conversationId.value, command.userId.value)
        } catch (e: Exception) {
            logger.error("删除对话失败: conversationId={}, userId={}, error={}", 
                        command.conversationId.value, command.userId.value, e.message, e)
            throw e
        }
    }
}