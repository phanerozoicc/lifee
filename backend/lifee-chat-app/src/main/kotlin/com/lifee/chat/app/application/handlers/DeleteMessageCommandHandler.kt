package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.commands.DeleteMessageCommand
import com.lifee.chat.domain.exceptions.ConversationNotFoundException
import com.lifee.chat.domain.exceptions.MessageNotFoundException
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.chat.domain.valueobjects.MessageId
import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.domain.valueobjects.UserId as CommonUserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DeleteMessageCommandHandler(
    private val conversationRepository: ConversationRepository
) : AsyncCommandHandler<DeleteMessageCommand, Unit> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(DeleteMessageCommandHandler::class.java)
    }

    @Transactional
    override suspend fun handle(command: DeleteMessageCommand) {
        logger.info("处理删除消息命令: conversationId={}, messageId={}, userId={}", 
                   command.conversationId, command.messageId, command.userId.value)
        
        try {
            val conversationId = ConversationId(command.conversationId.toString())
            val commonUserId = CommonUserId(command.userId.value.toString())
            val conversation = conversationRepository.findByIdAndUserId(conversationId, commonUserId)
                ?: throw ConversationNotFoundException(command.conversationId.toString())
            
            val messageId = MessageId(command.messageId.toString())
            val message = conversation.getMessages().find { it.id.value.toString() == command.messageId.toString() }
                 ?: throw MessageNotFoundException(command.messageId.toString())
            
            conversation.removeMessage(messageId)
            conversationRepository.save(conversation)
            
            logger.info("成功删除消息: conversationId={}, messageId={}", 
                       command.conversationId, command.messageId)
        } catch (e: Exception) {
            logger.error("删除消息失败: conversationId={}, messageId={}, userId={}, error={}", 
                        command.conversationId, command.messageId, command.userId.value, e.message, e)
            throw e
        }
    }
}