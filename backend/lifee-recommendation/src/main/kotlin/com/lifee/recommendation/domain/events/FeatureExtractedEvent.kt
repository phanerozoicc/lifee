package com.lifee.recommendation.domain.events

import com.lifee.common.domain.DomainEvent
import com.lifee.user.domain.UserId
import java.time.Instant
import java.util.*

/**
 * 特征提取完成事件
 */
data class FeatureExtractedEvent(
    val userId: UserId,
    val featureType: String, // USER_FEATURES, ITEM_FEATURES
    val featureCount: Int,
    val extractionTimeMs: Long,
    override val aggregateId: String = userId.value,
    override val version: Long = 1L
) : DomainEvent() {
    
    override fun copy(
        aggregateId: String,
        version: Long,
        occurredOn: Instant,
        eventId: UUID
    ): DomainEvent {
        return FeatureExtractedEvent(
            userId = UserId(aggregateId),
            featureType = this.featureType,
            featureCount = this.featureCount,
            extractionTimeMs = this.extractionTimeMs,
            aggregateId = aggregateId,
            version = version
        )
    }
}