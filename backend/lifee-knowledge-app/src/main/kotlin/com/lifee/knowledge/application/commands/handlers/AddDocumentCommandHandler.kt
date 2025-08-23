package com.lifee.knowledge.application.commands.handlers

import com.lifee.common.cqrs.commands.AsyncCommandHandler
import com.lifee.common.cqrs.events.EventBus
import com.lifee.common.domain.valueobjects.UserId
import com.lifee.knowledge.application.commands.AddDocumentCommand
import com.lifee.knowledge.domain.events.DocumentAddedEvent
import com.lifee.knowledge.domain.exceptions.*
import com.lifee.knowledge.domain.repositories.KnowledgeBaseRepository
import com.lifee.knowledge.domain.services.DocumentValidationService
import com.lifee.knowledge.domain.valueobjects.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 添加文档命令处理器
 */
@Component
class AddDocumentCommandHandler(
    private val knowledgeBaseRepository: KnowledgeBaseRepository,
    private val documentValidationService: DocumentValidationService,
    private val eventBus: EventBus
) : AsyncCommandHandler<AddDocumentCommand, Unit> {
    
    private val logger = LoggerFactory.getLogger(AddDocumentCommandHandler::class.java)
    
    @Transactional
    override suspend fun handle(command: AddDocumentCommand) {
        logger.debug("处理添加文档命令: documentId={}, knowledgeBaseId={}, type={}", 
            command.documentId, command.knowledgeBaseId, command.type)
        
        // 1. 创建值对象
        val knowledgeBaseId = KnowledgeBaseId.fromString(command.knowledgeBaseId)
        val documentId = DocumentId.fromString(command.documentId)
        val title = DocumentTitle(command.title)
        val content = DocumentContent(command.content)
        val type = DocumentType.fromString(command.type)
        val userId = UserId(command.userId)
        
        // 2. 验证文档
        documentValidationService.validateDocument(title, content, type)
        
        // 3. 清理文档内容
        val cleanedContent = documentValidationService.cleanDocumentContent(command.content, type)
        val finalContent = DocumentContent(cleanedContent)
        
        // 4. 查找知识库
        val knowledgeBase = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw KnowledgeBaseNotFoundException(command.knowledgeBaseId)
        
        // 5. 验证用户权限
        if (!knowledgeBase.isOwnedBy(userId)) {
            throw UnauthorizedAccessException(command.userId, command.knowledgeBaseId)
        }
        
        // 6. 检查文档是否已存在
        if (knowledgeBase.containsDocument(documentId)) {
            throw DocumentAlreadyExistsException(command.documentId)
        }
        
        // 7. 添加文档到知识库（使用清理后的内容）
        knowledgeBase.addDocument(
            documentId = documentId,
            title = title,
            content = finalContent,
            type = type
        )
        
        // 8. 保存知识库
        knowledgeBaseRepository.save(knowledgeBase)
        
        // 9. 发布文档添加事件
        val documentAddedEvent = DocumentAddedEvent(
            knowledgeBaseId = knowledgeBaseId,
            documentId = documentId,
            userId = userId,
            title = title,
            content = finalContent,
            type = type,
            contentLength = finalContent.getLength()
        )
        eventBus.publish(documentAddedEvent)
        
        logger.info("文档添加成功: documentId={}, contentLength={}, type={}", 
            command.documentId, finalContent.getLength(), command.type)
    }
}