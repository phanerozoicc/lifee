package com.lifee.knowledge.application.commands.handlers

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.knowledge.application.commands.UpdateDocumentCommand
import com.lifee.knowledge.domain.exceptions.*
import com.lifee.knowledge.domain.repositories.KnowledgeBaseRepository
import com.lifee.knowledge.domain.services.DocumentValidationService
import com.lifee.knowledge.domain.valueobjects.*
import com.lifee.common.domain.valueobjects.UserId
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 更新文档命令处理器
 */
@Component
class UpdateDocumentCommandHandler(
    private val knowledgeBaseRepository: KnowledgeBaseRepository,
    private val documentValidationService: DocumentValidationService
) : AsyncCommandHandler<UpdateDocumentCommand, Unit> {
    
    private val logger = LoggerFactory.getLogger(UpdateDocumentCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: UpdateDocumentCommand) {
        logger.debug("处理更新文档命令: documentId={}, knowledgeBaseId={}", 
            command.documentId, command.knowledgeBaseId)
        
        // 1. 创建值对象
        val knowledgeBaseId = KnowledgeBaseId.fromString(command.knowledgeBaseId)
        val documentId = DocumentId.fromString(command.documentId)
        val newTitle = DocumentTitle(command.newTitle)
        val newContent = DocumentContent(command.newContent)
        val userId = UserId(command.userId)
        
        // 2. 查找知识库
        val knowledgeBase = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw KnowledgeBaseNotFoundException(command.knowledgeBaseId)
        
        // 3. 验证用户权限
        if (!knowledgeBase.isOwnedBy(userId)) {
            throw UnauthorizedAccessException(command.userId, command.knowledgeBaseId)
        }
        
        // 4. 检查文档是否存在并获取现有文档类型
        if (!knowledgeBase.containsDocument(documentId)) {
            throw DocumentNotFoundException(command.documentId)
        }
        
        val existingDocument = knowledgeBase.getDocument(documentId)
            ?: throw DocumentNotFoundException(command.documentId)
        val documentType = existingDocument.getType()
        
        // 5. 验证新的文档内容
        documentValidationService.validateDocument(newTitle, newContent, documentType)
        
        // 6. 清理文档内容
        val cleanedContent = documentValidationService.cleanDocumentContent(command.newContent, documentType)
        val finalContent = DocumentContent(cleanedContent)
        
        // 7. 更新文档
        knowledgeBase.updateDocument(
            documentId = documentId,
            newTitle = newTitle,
            newContent = finalContent
        )
        
        // 8. 保存知识库
        knowledgeBaseRepository.save(knowledgeBase)
        
        logger.info("文档更新成功: documentId={}, contentLength={}, type={}", 
            command.documentId, finalContent.getLength(), documentType.value)
    }
}