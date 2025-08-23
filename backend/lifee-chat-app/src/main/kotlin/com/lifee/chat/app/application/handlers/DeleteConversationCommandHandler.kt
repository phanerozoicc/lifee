package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.commands.DeleteConversationCommand
import com.lifee.chat.domain.exceptions.ConversationNotFoundException
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.domain.valueobjects.UserId as CommonUserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DeleteConversationCommandHandler(
    private val conversationRepository: ConversationRepository
) : AsyncCommandHandler<DeleteConversationCommand, Unit> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(DeleteConversationCommandHandler::class.java)
    }

    @Transactional
    override suspend fun handle(command: DeleteConversationCommand) {
        logger.info("处理删除对话命令: conversationId={}, userId={}", 
                   command.conversationId, command.userId.value)
        
        try {
            val conversationId = ConversationId(command.conversationId.toString())
            val commonUserId = CommonUserId(command.userId.value.toString())
            val conversation = conversationRepository.findByIdAndUserId(conversationId, commonUserId)
                ?: throw ConversationNotFoundException(command.conversationId.toString())
            
            conversationRepository.delete(conversation)
            
            logger.info("成功删除对话: conversationId={}, userId={}", 
                       command.conversationId, command.userId.value)
        } catch (e: Exception) {
            logger.error("删除对话失败: conversationId={}, userId={}, error={}", 
                        command.conversationId, command.userId.value, e.message, e)
            throw e
        }
    }
}