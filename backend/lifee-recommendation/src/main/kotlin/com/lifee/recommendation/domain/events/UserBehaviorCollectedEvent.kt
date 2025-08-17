package com.lifee.recommendation.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 用户行为收集事件
 */
data class UserBehaviorCollectedEvent(
    val userId: UserId,
    val behaviorType: String,
    val targetId: String,
    val targetType: String,
    val metadata: Map<String, Any>,
    val timestamp: Instant,
    override val aggregateId: String = userId.value,
    override val version: Long = 1L
) : DomainEvent() {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return UserBehaviorCollectedEvent(
            userId = UserId(aggregateId),
            behaviorType = this.behaviorType,
            targetId = this.targetId,
            targetType = this.targetType,
            metadata = this.metadata,
            timestamp = this.timestamp,
            aggregateId = aggregateId,
            version = version
        )
    }
}