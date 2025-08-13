package com.lifee.recommendation.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.recommendation.domain.valueobjects.RecommendationId
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 推荐创建事件
 */
class RecommendationCreatedEvent(
    recommendationId: RecommendationId,
    val userId: UserId,
    occurredOn: Instant = Instant.now()
) : DomainEvent(recommendationId, occurredOn) {
    
    companion object {
        fun create(
            recommendationId: RecommendationId,
            userId: UserId
        ): RecommendationCreatedEvent {
            return RecommendationCreatedEvent(
                recommendationId = recommendationId,
                userId = userId
            )
        }
    }
}