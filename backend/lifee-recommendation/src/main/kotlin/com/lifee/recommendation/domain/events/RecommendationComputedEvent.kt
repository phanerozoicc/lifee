package com.lifee.recommendation.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 推荐计算完成事件
 */
data class RecommendationComputedEvent(
    val userId: UserId,
    val algorithm: String,
    val candidateCount: Int,
    val recommendationCount: Int,
    val computationTimeMs: Long,
    override val aggregateId: String = userId.value,
    override val version: Long = 1L
) : DomainEvent() {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: java.util.UUID
    ): DomainEvent {
        return this.copy(
            userId = UserId(aggregateId)
        )
    }
}