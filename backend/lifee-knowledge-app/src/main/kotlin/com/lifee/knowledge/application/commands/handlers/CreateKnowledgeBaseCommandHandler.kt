package com.lifee.knowledge.application.commands.handlers

import com.lifee.common.cqrs.commands.CommandHandler
import com.lifee.knowledge.application.commands.CreateKnowledgeBaseCommand
import com.lifee.knowledge.domain.aggregates.KnowledgeBase
import com.lifee.knowledge.domain.exceptions.DuplicateKnowledgeBaseNameException
import com.lifee.knowledge.domain.repositories.KnowledgeBaseRepository
import com.lifee.knowledge.domain.valueobjects.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 创建知识库命令处理器
 */
@Component
class CreateKnowledgeBaseCommandHandler(
    private val knowledgeBaseRepository: KnowledgeBaseRepository
) : CommandHandler<CreateKnowledgeBaseCommand> {
    
    private val logger = LoggerFactory.getLogger(CreateKnowledgeBaseCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: CreateKnowledgeBaseCommand) {
        logger.info("Processing CreateKnowledgeBaseCommand: ${command.knowledgeBaseId}")
        
        val knowledgeBaseId = KnowledgeBaseId.fromString(command.knowledgeBaseId)
        val name = KnowledgeBaseName(command.name)
        val description = KnowledgeBaseDescription(command.description)
        val ownerId = UserId.fromString(command.ownerId)
        
        // 检查是否已存在同名知识库
        if (knowledgeBaseRepository.existsByOwnerIdAndName(ownerId, command.name)) {
            throw DuplicateKnowledgeBaseNameException(command.name)
        }
        
        // 创建知识库聚合根
        val knowledgeBase = KnowledgeBase.create(
            id = knowledgeBaseId,
            name = name,
            description = description,
            ownerId = ownerId
        )
        
        // 保存知识库
        knowledgeBaseRepository.save(knowledgeBase)
        
        logger.info("Knowledge base created successfully: ${command.knowledgeBaseId}")
    }
}