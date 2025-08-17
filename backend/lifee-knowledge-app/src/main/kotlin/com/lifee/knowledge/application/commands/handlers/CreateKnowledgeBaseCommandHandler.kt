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
 * 
 * 负责处理知识库创建业务逻辑，包括：
 * 1. 验证知识库名称唯一性（同一用户下）
 * 2. 创建知识库聚合根
 * 3. 持久化知识库数据
 * 
 * @param knowledgeBaseRepository 知识库仓储，用于知识库数据的持久化操作和查询
 */
@Component
class CreateKnowledgeBaseCommandHandler(
    private val knowledgeBaseRepository: KnowledgeBaseRepository
) : CommandHandler<CreateKnowledgeBaseCommand> {
    
    private val logger = LoggerFactory.getLogger(CreateKnowledgeBaseCommandHandler::class.java)
    
    /**
     * 处理创建知识库命令
     * 
     * 执行完整的知识库创建流程：
     * 1. 构建值对象，确保数据有效性
     * 2. 验证知识库名称在用户范围内的唯一性
     * 3. 创建知识库聚合根，应用业务规则
     * 4. 持久化知识库数据到数据库
     * 
     * @param command 创建知识库命令，包含知识库的基本信息
     * @throws DuplicateKnowledgeBaseNameException 当用户已有同名知识库时抛出
     * @throws ValidationException 当输入参数不符合业务规则时抛出
     */
    @Transactional
    override suspend fun handle(command: CreateKnowledgeBaseCommand) {
        logger.info("开始处理创建知识库命令: knowledgeBaseId={}, name={}", command.knowledgeBaseId, command.name)
        
        // 步骤1: 构建值对象，确保数据格式和业务规则的正确性
        val knowledgeBaseId = KnowledgeBaseId.fromString(command.knowledgeBaseId)
        val name = KnowledgeBaseName(command.name)
        val description = KnowledgeBaseDescription(command.description)
        val ownerId = UserId.fromString(command.ownerId)
        
        // 步骤2: 检查用户是否已存在同名知识库，确保名称唯一性
        if (knowledgeBaseRepository.existsByOwnerIdAndName(ownerId, command.name)) {
            logger.warn("知识库名称已存在: ownerId={}, name={}", command.ownerId, command.name)
            throw DuplicateKnowledgeBaseNameException(command.name)
        }
        
        // 步骤3: 创建知识库聚合根，应用领域业务规则
        val knowledgeBase = KnowledgeBase.create(
            id = knowledgeBaseId,
            name = name,
            description = description,
            ownerId = ownerId
        )
        
        // 步骤4: 持久化知识库数据，完成创建流程
        knowledgeBaseRepository.save(knowledgeBase)
        
        logger.info("知识库创建成功: knowledgeBaseId={}, ownerId={}", command.knowledgeBaseId, command.ownerId)
    }
}