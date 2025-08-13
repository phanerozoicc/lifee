package com.lifee.recommendation.app.adapters.persistence

import com.lifee.recommendation.domain.aggregates.Recommendation
import com.lifee.recommendation.domain.repositories.RecommendationRepository
import com.lifee.recommendation.domain.valueobjects.RecommendationId
import com.lifee.recommendation.domain.valueobjects.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

/**
 * Spring Data JPA 推荐仓储接口
 */
interface SpringDataRecommendationRepository : JpaRepository<RecommendationJpaEntity, UUID> {
    
    /**
     * 根据用户ID查找推荐
     */
    fun findByUserId(userId: String): RecommendationJpaEntity?
    
    /**
     * 根据用户ID分页查询推荐
     */
    fun findByUserId(userId: String, pageable: Pageable): Page<RecommendationJpaEntity>
    
    /**
     * 检查用户是否存在推荐
     */
    fun existsByUserId(userId: String): Boolean
    
    /**
     * 统计用户推荐数量
     */
    fun countByUserId(userId: UUID): Long
    
    /**
     * 根据用户ID删除推荐
     */
    fun deleteByUserId(userId: UUID)
    
    /**
     * 查询用户推荐项数量
     */
    @Query("""
        SELECT COUNT(ri) 
        FROM RecommendationJpaEntity r 
        JOIN r.items ri 
        WHERE r.userId = :userId
    """)
    fun countRecommendationItemsByUserId(@Param("userId") userId: UUID): Long
}

/**
 * 推荐仓储JPA实现
 */
@Repository
class RecommendationJpaRepositoryImpl(
    private val springDataRepository: SpringDataRecommendationRepository
) : RecommendationRepository {
    
    override fun save(recommendation: Recommendation): Recommendation {
        val entity = RecommendationJpaEntity.from(recommendation)
        val savedEntity = springDataRepository.save(entity)
        return savedEntity.toDomain()
    }
    
    override fun findById(id: RecommendationId): Recommendation? {
        return springDataRepository.findById(id.value)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override fun findByUserId(userId: UserId): Recommendation? {
        return springDataRepository.findByUserId(userId.value)
            ?.toDomain()
    }
    
    override fun findByUserId(userId: UserId, pageable: Pageable): Page<Recommendation> {
        val entityPage = springDataRepository.findByUserId(userId.value, pageable)
        val recommendations = entityPage.content.map { it.toDomain() }
        
        return PageImpl(
            recommendations,
            pageable,
            entityPage.totalElements
        )
    }
    
    override fun delete(recommendation: Recommendation) {
        springDataRepository.deleteById(recommendation.id.value)
    }
    
    override fun deleteById(id: RecommendationId) {
        springDataRepository.deleteById(id.value)
    }
    
    override fun existsById(id: RecommendationId): Boolean {
        return springDataRepository.existsById(id.value)
    }
    
    override fun existsByUserId(userId: UserId): Boolean {
        return springDataRepository.existsByUserId(userId.value)
    }
    
    override fun count(): Long {
        return springDataRepository.count()
    }
    
    override fun countByUserId(userId: UserId): Long {
        return springDataRepository.countByUserId(userId.value)
    }
}