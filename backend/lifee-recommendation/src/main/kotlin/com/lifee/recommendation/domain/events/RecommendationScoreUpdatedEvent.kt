package com.lifee.recommendation.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.recommendation.domain.valueobjects.*
import java.time.Instant

/**
 * 推荐分数更新事件
 */
class RecommendationScoreUpdatedEvent(
    recommendationId: RecommendationId,
    val userId: UserId,
    val contentId: ContentId,
    val oldScore: RecommendationScore,
    val newScore: RecommendationScore,
    occurredOn: Instant = Instant.now()
) : DomainEvent(recommendationId, occurredOn) {
    
    companion object {
        fun create(
            recommendationId: RecommendationId,
            userId: UserId,
            contentId: ContentId,
            oldScore: RecommendationScore,
            newScore: RecommendationScore
        ): RecommendationScoreUpdatedEvent {
            return RecommendationScoreUpdatedEvent(
                recommendationId = recommendationId,
                userId = userId,
                contentId = contentId,
                oldScore = oldScore,
                newScore = newScore
            )
        }
    }
}