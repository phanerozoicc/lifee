package com.lifee.recommendation.app.application.queries.handlers

import com.lifee.common.cqrs.queries.QueryHandler
import com.lifee.recommendation.app.application.dtos.RecommendationDetailDto
import com.lifee.recommendation.app.application.queries.GetUserRecommendationQuery
import com.lifee.recommendation.domain.exceptions.UserRecommendationNotFoundException
import com.lifee.recommendation.domain.repositories.RecommendationRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

/**
 * 获取用户推荐查询处理器
 */
@Component
class GetUserRecommendationQueryHandler(
    private val recommendationRepository: RecommendationRepository
) : QueryHandler<GetUserRecommendationQuery, RecommendationDetailDto> {
    
    @Transactional(readOnly = true)
    override suspend fun handle(query: GetUserRecommendationQuery): RecommendationDetailDto {
        val recommendation = recommendationRepository.findByUserId(query.userId)
            ?: throw UserRecommendationNotFoundException(query.userId)
        
        return RecommendationDetailDto.fromDomain(recommendation)
    }
}