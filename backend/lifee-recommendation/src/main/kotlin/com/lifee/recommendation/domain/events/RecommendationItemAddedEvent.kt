package com.lifee.recommendation.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.recommendation.domain.valueobjects.*
import java.time.Instant

/**
 * 推荐项添加事件
 */
class RecommendationItemAddedEvent(
    recommendationId: RecommendationId,
    val userId: UserId,
    val contentId: ContentId,
    val score: RecommendationScore,
    val type: RecommendationType,
    occurredOn: Instant = Instant.now()
) : DomainEvent(recommendationId, occurredOn) {
    
    companion object {
        fun create(
            recommendationId: RecommendationId,
            userId: UserId,
            contentId: ContentId,
            score: RecommendationScore,
            type: RecommendationType
        ): RecommendationItemAddedEvent {
            return RecommendationItemAddedEvent(
                recommendationId = recommendationId,
                userId = userId,
                contentId = contentId,
                score = score,
                type = type
            )
        }
    }
}