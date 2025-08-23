package com.lifee.knowledge.application.commands.handlers

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.knowledge.application.commands.RemoveDocumentCommand
import com.lifee.knowledge.domain.exceptions.*
import com.lifee.knowledge.domain.repositories.KnowledgeBaseRepository
import com.lifee.knowledge.domain.valueobjects.*
import com.lifee.common.domain.valueobjects.UserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 删除文档命令处理器
 */
@Component
class RemoveDocumentCommandHandler(
    private val knowledgeBaseRepository: KnowledgeBaseRepository
) : AsyncCommandHandler<RemoveDocumentCommand, Unit> {
    
    private val logger = LoggerFactory.getLogger(RemoveDocumentCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: RemoveDocumentCommand) {
        logger.info("Processing RemoveDocumentCommand: ${command.documentId} from ${command.knowledgeBaseId}")
        
        val knowledgeBaseId = KnowledgeBaseId.fromString(command.knowledgeBaseId)
        val documentId = DocumentId.fromString(command.documentId)
        val userId = UserId(command.userId)
        
        // 查找知识库
        val knowledgeBase = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw KnowledgeBaseNotFoundException(command.knowledgeBaseId)
        
        // 验证用户权限
        if (!knowledgeBase.isOwnedBy(userId)) {
            throw UnauthorizedAccessException(command.userId, command.knowledgeBaseId)
        }
        
        // 检查文档是否存在
        if (!knowledgeBase.containsDocument(documentId)) {
            throw DocumentNotFoundException(command.documentId)
        }
        
        // 删除文档
        knowledgeBase.removeDocument(documentId)
        
        // 保存知识库
        knowledgeBaseRepository.save(knowledgeBase)
        
        logger.info("Document removed successfully: ${command.documentId}")
    }
}