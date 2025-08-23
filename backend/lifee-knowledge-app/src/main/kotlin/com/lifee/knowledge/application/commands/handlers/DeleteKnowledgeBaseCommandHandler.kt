package com.lifee.knowledge.application.commands.handlers

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.domain.valueobjects.UserId
import com.lifee.knowledge.application.commands.DeleteKnowledgeBaseCommand
import com.lifee.knowledge.domain.exceptions.*
import com.lifee.knowledge.domain.repositories.KnowledgeBaseRepository
import com.lifee.knowledge.domain.valueobjects.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 删除知识库命令处理器
 */
@Component
class DeleteKnowledgeBaseCommandHandler(
    private val knowledgeBaseRepository: KnowledgeBaseRepository
) : AsyncCommandHandler<DeleteKnowledgeBaseCommand, Unit> {
    
    private val logger = LoggerFactory.getLogger(DeleteKnowledgeBaseCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: DeleteKnowledgeBaseCommand) {
        logger.info("Processing DeleteKnowledgeBaseCommand: ${command.knowledgeBaseId}")
        
        val knowledgeBaseId = KnowledgeBaseId.fromString(command.knowledgeBaseId)
        val userId = UserId(command.userId)
        
        // 查找知识库
        val knowledgeBase = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw KnowledgeBaseNotFoundException(command.knowledgeBaseId)
        
        // 验证用户权限
        if (!knowledgeBase.isOwnedBy(userId)) {
            throw UnauthorizedAccessException(command.userId, command.knowledgeBaseId)
        }
        
        // 删除知识库
        knowledgeBaseRepository.delete(knowledgeBaseId)
        
        logger.info("Knowledge base deleted successfully: ${command.knowledgeBaseId}")
    }
}