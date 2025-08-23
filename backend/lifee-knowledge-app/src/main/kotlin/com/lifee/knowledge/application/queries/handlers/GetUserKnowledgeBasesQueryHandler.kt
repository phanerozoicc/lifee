package com.lifee.knowledge.application.queries.handlers

import com.lifee.common.cqrs.queries.AsyncQueryHandler
import com.lifee.knowledge.application.dto.KnowledgeBaseDto
import com.lifee.knowledge.application.queries.GetUserKnowledgeBasesQuery
import com.lifee.knowledge.domain.repositories.KnowledgeBaseRepository
import com.lifee.common.domain.valueobjects.UserId
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory

/**
 * 获取用户知识库列表查询处理器
 */
@Component
class GetUserKnowledgeBasesQueryHandler(
    private val knowledgeBaseRepository: KnowledgeBaseRepository
) : AsyncQueryHandler<GetUserKnowledgeBasesQuery, List<KnowledgeBaseDto>> {
    
    private val logger = LoggerFactory.getLogger(GetUserKnowledgeBasesQueryHandler::class.java)
    
    override suspend fun handle(query: GetUserKnowledgeBasesQuery): List<KnowledgeBaseDto> {
        logger.info("Handling GetUserKnowledgeBasesQuery for userId: ${query.userId}, offset: ${query.offset}, limit: ${query.limit}")
        
        try {
            val userId = UserId(query.userId)
            logger.info("Created UserId: ${userId.value}")
            
            val knowledgeBases = knowledgeBaseRepository.findByOwnerIdWithPagination(
                ownerId = userId,
                offset = query.offset,
                limit = query.limit
            )
            logger.info("Found ${knowledgeBases.size} knowledge bases")
        
            return knowledgeBases.map { knowledgeBase ->
                KnowledgeBaseDto(
                    id = knowledgeBase.getId().toString(),
                    name = knowledgeBase.getName().value,
                    description = knowledgeBase.getDescription().value,
                    ownerId = knowledgeBase.getOwnerId().toString(),
                    documentCount = knowledgeBase.getDocumentCount(),
                    totalSize = knowledgeBase.getTotalSize().toLong(),
                    createdAt = knowledgeBase.getCreatedAt(),
                    updatedAt = knowledgeBase.getUpdatedAt()
                    // 不包含文档列表以提高性能
                )
            }
        } catch (e: Exception) {
            logger.error("Error handling GetUserKnowledgeBasesQuery", e)
            throw e
        }
    }
}