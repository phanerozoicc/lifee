package com.lifee.recommendation.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 推荐缓存完成事件
 */
data class RecommendationCachedEvent(
    val userId: UserId,
    val algorithm: String,
    val recommendationCount: Int,
    val cacheTimeMs: Long,
    val ttlSeconds: Long,
    override val aggregateId: String = userId.value,
    override val version: Long = 1L
) : DomainEvent() {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant
    ): DomainEvent {
        return copy(
            aggregateId = aggregateId,
            version = version
        )
    }
}