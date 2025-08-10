package com.lifee.recommendation.app.application.queries.handlers

import com.lifee.common.cqrs.queries.QueryHandler
import com.lifee.recommendation.app.application.dtos.RecommendationDetailDto
import com.lifee.recommendation.app.application.queries.GetRecommendationQuery
import com.lifee.recommendation.domain.exceptions.RecommendationNotFoundException
import com.lifee.recommendation.domain.repositories.RecommendationRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 获取推荐查询处理器
 */
@Component
class GetRecommendationQueryHandler(
    private val recommendationRepository: RecommendationRepository
) : QueryHandler<GetRecommendationQuery, RecommendationDetailDto> {
    
    @Transactional(readOnly = true)
    override suspend fun handle(query: GetRecommendationQuery): RecommendationDetailDto {
        val recommendation = recommendationRepository.findById(query.recommendationId)
            ?: throw RecommendationNotFoundException(query.recommendationId)
        
        return RecommendationDetailDto.fromDomain(recommendation)
    }
}