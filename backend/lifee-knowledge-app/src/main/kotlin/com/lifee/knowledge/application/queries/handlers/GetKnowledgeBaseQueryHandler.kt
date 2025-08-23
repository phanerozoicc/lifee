package com.lifee.knowledge.application.queries.handlers

import com.lifee.common.cqrs.queries.AsyncQueryHandler
import com.lifee.knowledge.application.dto.DocumentSummaryDto
import com.lifee.knowledge.application.dto.KnowledgeBaseDto
import com.lifee.knowledge.application.queries.GetKnowledgeBaseQuery
import com.lifee.knowledge.domain.exceptions.KnowledgeBaseNotFoundException
import com.lifee.knowledge.domain.exceptions.UnauthorizedAccessException
import com.lifee.knowledge.domain.repositories.KnowledgeBaseRepository
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.common.domain.valueobjects.UserId
import org.springframework.stereotype.Component

/**
 * 获取知识库查询处理器
 */
@Component
class GetKnowledgeBaseQueryHandler(
    private val knowledgeBaseRepository: KnowledgeBaseRepository
) : AsyncQueryHandler<GetKnowledgeBaseQuery, KnowledgeBaseDto?> {
    
    override suspend fun handle(query: GetKnowledgeBaseQuery): KnowledgeBaseDto? {
        val knowledgeBaseId = KnowledgeBaseId.fromString(query.knowledgeBaseId)
        val userId = UserId(query.userId)
        
        val knowledgeBase = knowledgeBaseRepository.findById(knowledgeBaseId)
            ?: throw KnowledgeBaseNotFoundException(query.knowledgeBaseId)
        
        // 验证用户权限
        if (!knowledgeBase.isOwnedBy(userId)) {
            throw UnauthorizedAccessException(query.userId, query.knowledgeBaseId)
        }
        
        // 转换为DTO
        return KnowledgeBaseDto(
            id = knowledgeBase.getId().toString(),
            name = knowledgeBase.getName().value,
            description = knowledgeBase.getDescription().value,
            ownerId = knowledgeBase.getOwnerId().toString(),
            documentCount = knowledgeBase.getDocumentCount(),
            totalSize = knowledgeBase.getTotalSize().toLong(),
            createdAt = knowledgeBase.getCreatedAt(),
            updatedAt = knowledgeBase.getUpdatedAt(),
            documents = knowledgeBase.getDocuments().map { document ->
                DocumentSummaryDto(
                    id = document.getId().toString(),
                    title = document.getTitle().value,
                    type = document.getType().value,
                    size = document.getSize(),
                    summary = document.getSummary(),
                    createdAt = document.getCreatedAt(),
                    updatedAt = document.getUpdatedAt()
                )
            }
        )
    }
}