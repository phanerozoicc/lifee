package com.lifee.chat.app.application.handlers

import com.lifee.chat.app.application.commands.CreateConversationCommand
import com.lifee.chat.app.application.dtos.ConversationDto
import com.lifee.chat.domain.aggregates.Conversation
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.common.cqrs.CommandHandler
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 创建对话命令处理器
 * 
 * 负责处理对话创建业务逻辑，包括：
 * 1. 根据用户输入创建对话聚合根
 * 2. 支持自定义标题或默认标题
 * 3. 持久化对话数据
 * 4. 返回对话DTO给调用方
 * 
 * @param conversationRepository 对话仓储，用于对话数据的持久化操作
 */
@Component
class CreateConversationCommandHandler(
    private val conversationRepository: ConversationRepository
) : CommandHandler<CreateConversationCommand, ConversationDto> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(CreateConversationCommandHandler::class.java)
    }

    /**
     * 处理创建对话命令
     * 
     * 执行完整的对话创建流程：
     * 1. 根据是否提供标题选择不同的创建策略
     * 2. 创建对话聚合根，应用领域业务规则
     * 3. 持久化对话数据到数据库
     * 4. 转换为DTO返回给调用方
     * 
     * @param command 创建对话命令，包含用户ID和可选的对话标题
     * @return ConversationDto 创建成功的对话数据传输对象
     * @throws Exception 当对话创建过程中发生错误时抛出
     */
    @Transactional
    override suspend fun handle(command: CreateConversationCommand): ConversationDto {
        logger.info("开始处理创建对话命令: userId={}, title={}", 
                   command.userId.value, command.title?.value ?: "默认标题")
        
        try {
            // 步骤1: 根据是否提供标题选择不同的创建策略
            val conversation = if (command.title != null) {
                // 使用用户提供的自定义标题创建对话
                Conversation.create(command.title, command.userId)
            } else {
                // 使用系统默认标题创建对话
                Conversation.createWithDefaultTitle(command.userId)
            }
            
            // 步骤2: 持久化对话数据，获取完整的对话实体
            val savedConversation = conversationRepository.save(conversation)
            
            logger.info("对话创建成功: conversationId={}, userId={}, title={}", 
                       savedConversation.getId().value, command.userId.value, 
                       savedConversation.getTitle().value)
            
            // 步骤3: 转换为DTO返回给调用方
            return ConversationDto.fromDomain(savedConversation)
        } catch (e: Exception) {
            logger.error("创建对话失败: userId={}, title={}, error={}", 
                        command.userId.value, command.title?.value, e.message, e)
            throw e
        }
    }
}