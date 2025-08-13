package com.lifee.recommendation.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.recommendation.domain.valueobjects.RecommendationId
import com.lifee.recommendation.domain.valueobjects.ContentId
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 推荐项移除事件
 */
class RecommendationItemRemovedEvent(
    recommendationId: RecommendationId,
    val userId: UserId,
    val contentId: ContentId,
    occurredOn: Instant = Instant.now()
) : DomainEvent(recommendationId, occurredOn) {
    
    companion object {
        fun create(
            recommendationId: RecommendationId,
            userId: UserId,
            contentId: ContentId
        ): RecommendationItemRemovedEvent {
            return RecommendationItemRemovedEvent(
                recommendationId = recommendationId,
                userId = userId,
                contentId = contentId
            )
        }
    }
}