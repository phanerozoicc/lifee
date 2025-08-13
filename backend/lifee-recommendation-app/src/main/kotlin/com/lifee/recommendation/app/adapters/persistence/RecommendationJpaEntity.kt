package com.lifee.recommendation.app.adapters.persistence

import com.lifee.recommendation.domain.aggregates.Recommendation
import com.lifee.recommendation.domain.entities.RecommendationItem
import com.lifee.recommendation.domain.valueobjects.RecommendationId
import com.lifee.recommendation.domain.valueobjects.ContentId
import com.lifee.recommendation.domain.valueobjects.RecommendationType
import com.lifee.recommendation.domain.valueobjects.RecommendationScore
import com.lifee.user.domain.UserId
import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

/**
 * 推荐JPA实体
 */
@Entity
@Table(name = "recommendations")
data class RecommendationJpaEntity(
    @Id
    @Column(name = "id", columnDefinition = "UUID")
    val id: UUID,
    
    @Column(name = "user_id", nullable = false, length = 9)
    val userId: String,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(name = "updated_at", nullable = false)
    val updatedAt: LocalDateTime,
    
    @OneToMany(
        mappedBy = "recommendation",
        cascade = [CascadeType.ALL],
        orphanRemoval = true,
        fetch = FetchType.LAZY
    )
    val items: MutableList<RecommendationItemJpaEntity> = mutableListOf()
) {
    
    companion object {
        /**
         * 从领域对象转换为JPA实体
         */
        fun from(recommendation: Recommendation): RecommendationJpaEntity {
            val entity = RecommendationJpaEntity(
                id = recommendation.id.value,
                userId = recommendation.userId.value,
                createdAt = recommendation.createdAt,
                updatedAt = recommendation.updatedAt
            )
            
            // 添加推荐项
            recommendation.items.forEach { item ->
                entity.items.add(RecommendationItemJpaEntity.from(item, entity))
            }
            
            return entity
        }
    }
    
    /**
     * 转换为领域对象
     */
    fun toDomain(): Recommendation {
        val domainItems = items.map { it.toDomain() }.toMutableList()
        
        return Recommendation(
            id = RecommendationId(id),
            userId = UserId(userId),
            items = domainItems,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }
}

/**
 * 推荐项JPA实体
 */
@Entity
@Table(name = "recommendation_items")
data class RecommendationItemJpaEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommendation_id", nullable = false)
    val recommendation: RecommendationJpaEntity,
    
    @Column(name = "content_id", nullable = false, columnDefinition = "UUID")
    val contentId: UUID,
    
    @Column(name = "score", nullable = false)
    val score: Double,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    val type: RecommendationType,
    
    @Column(name = "reason", nullable = false, length = 500)
    val reason: String,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime
) {
    
    companion object {
        /**
         * 从领域对象转换为JPA实体
         */
        fun from(
            item: RecommendationItem,
            recommendation: RecommendationJpaEntity
        ): RecommendationItemJpaEntity {
            return RecommendationItemJpaEntity(
                recommendation = recommendation,
                contentId = item.contentId.value,
                score = item.score.value,
                type = item.type,
                reason = item.reason,
                createdAt = item.createdAt
            )
        }
    }
    
    /**
     * 转换为领域对象
     */
    fun toDomain(): RecommendationItem {
        return RecommendationItem(
            contentId = ContentId(contentId),
            score = RecommendationScore(score),
            type = type,
            reason = reason,
            createdAt = createdAt
        )
    }
}