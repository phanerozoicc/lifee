package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.commands.AddMessageCommand
import com.lifee.chat.app.application.dtos.MessageDto
import com.lifee.chat.app.services.ConversationResponseService
import com.lifee.chat.domain.entities.Message
import com.lifee.chat.domain.exceptions.ConversationNotFoundException
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.chat.domain.services.MessageValidationService
import com.lifee.chat.domain.valueobjects.*
import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class AddMessageCommandHandler(
    private val conversationRepository: ConversationRepository,
    private val messageValidationService: MessageValidationService,
    private val conversationResponseService: ConversationResponseService
) : AsyncCommandHandler<AddMessageCommand, MessageDto> {
    
    private val logger = LoggerFactory.getLogger(AddMessageCommandHandler::class.java)

    @Transactional
    override suspend fun handle(command: AddMessageCommand): MessageDto {
        logger.debug("处理添加消息命令: conversationId={}, userId={}, type={}", 
            command.conversationId.value, command.userId.value, command.type)
        
        // 1. 验证消息内容
        messageValidationService.validateMessage(command.content, command.type)
        
        // 2. 清理消息内容
        val cleanedContent = messageValidationService.cleanMessageContent(command.content.value)
        val finalContent = MessageContent(cleanedContent)
        
        // 3. 查找对话
        val conversation = conversationRepository.findByIdAndUserId(command.conversationId, command.userId)
            ?: throw ConversationNotFoundException(command.conversationId)
        
        // 4. 创建消息
        val message = Message.create(
            content = finalContent,
            type = command.type,
            userId = command.userId
        )
        
        // 5. 添加消息到对话
        conversation.addMessage(message)
        conversationRepository.save(conversation)
        
        logger.info("消息添加成功: messageId={}, conversationId={}, contentLength={}", 
            message.id.value, command.conversationId.value, finalContent.getLength())
        
        // 6. 如果是用户消息，生成AI响应
        if (command.type == "USER") {
            try {
                val response = conversationResponseService.generateResponse(
                    conversationId = command.conversationId,
                    userMessage = command.content,
                    userId = command.userId,
                    knowledgeBaseIds = command.knowledgeBaseIds ?: emptyList(),
                    useRAG = command.useRAG ?: true
                )
                
                // 添加AI响应消息
                val assistantMessage = Message.create(
                    content = MessageContent(response.content),
                    type = command.type,
                    userId = command.userId
                )
                
                conversation.addMessage(assistantMessage)
                conversationRepository.save(conversation)
                
                logger.info("AI响应生成成功: messageId={}, tokensUsed={}", 
                    assistantMessage.id.value, response.tokensUsed)
                    
            } catch (e: Exception) {
                logger.error("AI响应生成失败: conversationId={}", command.conversationId.value, e)
                // 不抛出异常，确保用户消息仍然被保存
            }
        }
        
        return MessageDto.fromDomain(message)
    }
}