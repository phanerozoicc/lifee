package com.lifee.recommendation.app.application.dtos

import com.lifee.recommendation.domain.aggregates.Recommendation
import com.lifee.recommendation.domain.entities.RecommendationItem
import com.lifee.recommendation.domain.valueobjects.RecommendationType
import java.time.Instant

/**
 * 推荐DTO
 */
data class RecommendationDto(
    val id: String,
    val userId: String,
    val items: List<RecommendationItemDto>,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun fromDomain(recommendation: Recommendation): RecommendationDto {
            return RecommendationDto(
                id = recommendation.getId().toString(),
                userId = recommendation.getUserId().toString(),
                items = recommendation.getItems().map { RecommendationItemDto.fromDomain(it) },
                createdAt = recommendation.getCreatedAt(),
                updatedAt = recommendation.getUpdatedAt()
            )
        }
    }
}

/**
 * 推荐项DTO
 */
data class RecommendationItemDto(
    val contentId: String,
    val score: Double,
    val type: RecommendationType,
    val reason: String,
    val createdAt: Instant
) {
    companion object {
        fun fromDomain(item: RecommendationItem): RecommendationItemDto {
            return RecommendationItemDto(
                contentId = item.getContentId().toString(),
                score = item.getScore().value,
                type = item.getType(),
                reason = item.getReason(),
                createdAt = item.getCreatedAt()
            )
        }
    }
}

/**
 * 推荐详情DTO
 */
data class RecommendationDetailDto(
    val id: String,
    val userId: String,
    val items: List<RecommendationItemDto>,
    val highQualityItems: List<RecommendationItemDto>,
    val itemCount: Int,
    val isEmpty: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun fromDomain(recommendation: Recommendation): RecommendationDetailDto {
            return RecommendationDetailDto(
                id = recommendation.getId().toString(),
                userId = recommendation.getUserId().toString(),
                items = recommendation.getItems().map { RecommendationItemDto.fromDomain(it) },
                highQualityItems = recommendation.getHighQualityItems().map { RecommendationItemDto.fromDomain(it) },
                itemCount = recommendation.getItemCount(),
                isEmpty = recommendation.isEmpty(),
                createdAt = recommendation.getCreatedAt(),
                updatedAt = recommendation.getUpdatedAt()
            )
        }
    }
}

/**
 * 推荐统计DTO
 */
data class RecommendationStatsDto(
    val userId: String,
    val totalRecommendations: Long,
    val totalItems: Int,
    val highQualityItemsCount: Int,
    val averageScore: Double,
    val typeDistribution: Map<RecommendationType, Int>
) {
    companion object {
        fun fromDomain(
            userId: String,
            totalRecommendations: Long,
            recommendation: Recommendation?
        ): RecommendationStatsDto {
            if (recommendation == null) {
                return RecommendationStatsDto(
                    userId = userId,
                    totalRecommendations = totalRecommendations,
                    totalItems = 0,
                    highQualityItemsCount = 0,
                    averageScore = 0.0,
                    typeDistribution = emptyMap()
                )
            }
            
            val items = recommendation.getItems()
            val averageScore = if (items.isNotEmpty()) {
                items.map { it.getScore().value }.average()
            } else {
                0.0
            }
            
            val typeDistribution = items.groupBy { it.getType() }
                .mapValues { it.value.size }
            
            return RecommendationStatsDto(
                userId = userId,
                totalRecommendations = totalRecommendations,
                totalItems = items.size,
                highQualityItemsCount = recommendation.getHighQualityItems().size,
                averageScore = averageScore,
                typeDistribution = typeDistribution
            )
        }
    }
}

/**
 * 推荐页面DTO
 */
data class RecommendationPageDto(
    val content: List<RecommendationDto>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)