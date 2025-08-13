package com.lifee.recommendation.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.recommendation.domain.valueobjects.RecommendationId
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 推荐清空事件
 */
class RecommendationClearedEvent(
    recommendationId: RecommendationId,
    val userId: UserId,
    occurredOn: Instant = Instant.now()
) : DomainEvent(recommendationId, occurredOn) {
    
    companion object {
        fun create(
            recommendationId: RecommendationId,
            userId: UserId
        ): RecommendationClearedEvent {
            return RecommendationClearedEvent(
                recommendationId = recommendationId,
                userId = userId
            )
        }
    }
}