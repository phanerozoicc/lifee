package com.lifee.knowledge.application.queries.handlers

import com.lifee.common.cqrs.queries.QueryHandler
import com.lifee.knowledge.application.dto.KnowledgeBaseDto
import com.lifee.knowledge.application.queries.GetUserKnowledgeBasesQuery
import com.lifee.knowledge.domain.repositories.KnowledgeBaseRepository
import com.lifee.knowledge.domain.valueobjects.UserId
import org.springframework.stereotype.Component

/**
 * 获取用户知识库列表查询处理器
 */
@Component
class GetUserKnowledgeBasesQueryHandler(
    private val knowledgeBaseRepository: KnowledgeBaseRepository
) : QueryHandler<GetUserKnowledgeBasesQuery, List<KnowledgeBaseDto>> {
    
    override suspend fun handle(query: GetUserKnowledgeBasesQuery): List<KnowledgeBaseDto> {
        val userId = UserId.fromString(query.userId)
        
        val knowledgeBases = knowledgeBaseRepository.findByOwnerIdWithPagination(
            ownerId = userId,
            offset = query.offset,
            limit = query.limit
        )
        
        return knowledgeBases.map { knowledgeBase ->
            KnowledgeBaseDto(
                id = knowledgeBase.getId().toString(),
                name = knowledgeBase.getName().value,
                description = knowledgeBase.getDescription().value,
                ownerId = knowledgeBase.getOwnerId().toString(),
                documentCount = knowledgeBase.getDocumentCount(),
                totalSize = knowledgeBase.getTotalSize(),
                createdAt = knowledgeBase.getCreatedAt(),
                updatedAt = knowledgeBase.getUpdatedAt()
                // 不包含文档列表以提高性能
            )
        }
    }
}