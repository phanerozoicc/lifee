package com.lifee.knowledge.application.queries.handlers

import com.lifee.common.cqrs.queries.QueryHandler
import com.lifee.knowledge.application.dto.DocumentDto
import com.lifee.knowledge.application.queries.GetDocumentQuery
import com.lifee.knowledge.domain.exceptions.DocumentNotFoundException
import com.lifee.knowledge.domain.exceptions.KnowledgeBaseNotFoundException
import com.lifee.knowledge.domain.exceptions.UnauthorizedAccessException
import com.lifee.knowledge.domain.repositories.KnowledgeBaseRepository
import com.lifee.knowledge.domain.valueobjects.DocumentId
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.knowledge.domain.valueobjects.UserId
import org.springframework.stereotype.Component

/**
 * 获取文档查询处理器
 */
@Component
class GetDocumentQueryHandler(
    private val knowledgeBaseRepository: KnowledgeBaseRepository
) : QueryHandler<GetDocumentQuery, DocumentDto?> {
    
    override suspend fun handle(query: GetDocumentQuery): DocumentDto? {
        val knowledgeBaseId = KnowledgeBaseId.fromString(query.knowledgeBaseId)
        val documentId = DocumentId.fromString(query.documentId)
        val userId = UserId.fromString(query.userId)
        
        val knowledgeBase = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw KnowledgeBaseNotFoundException(query.knowledgeBaseId)
        
        // 验证用户权限
        if (!knowledgeBase.isOwnedBy(userId)) {
            throw UnauthorizedAccessException(query.userId, query.knowledgeBaseId)
        }
        
        val document = knowledgeBase.getDocument(documentId)
            ?: throw DocumentNotFoundException(query.documentId)
        
        return DocumentDto(
            id = document.getId().toString(),
            title = document.getTitle().value,
            content = document.getContent().value,
            type = document.getType().value,
            size = document.getSize(),
            createdAt = document.getCreatedAt(),
            updatedAt = document.getUpdatedAt(),
            knowledgeBaseId = knowledgeBase.getId().toString()
        )
    }
}