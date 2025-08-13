package com.lifee.recommendation.domain.repositories

import com.lifee.recommendation.domain.aggregates.Recommendation
import com.lifee.recommendation.domain.valueobjects.RecommendationId
import com.lifee.user.domain.UserId
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

/**
 * 推荐仓储接口
 */
interface RecommendationRepository {
    
    /**
     * 保存推荐
     */
    suspend fun save(recommendation: Recommendation): Recommendation
    
    /**
     * 根据ID查找推荐
     */
    suspend fun findById(id: RecommendationId): Recommendation?
    
    /**
     * 根据用户ID查找推荐
     */
    suspend fun findByUserId(userId: UserId): Recommendation?
    
    /**
     * 根据用户ID分页查找推荐历史
     */
    suspend fun findByUserIdWithPagination(userId: UserId, pageable: Pageable): Page<Recommendation>
    
    /**
     * 删除推荐
     */
    suspend fun delete(recommendation: Recommendation)
    
    /**
     * 根据ID删除推荐
     */
    suspend fun deleteById(id: RecommendationId)
    
    /**
     * 检查推荐是否存在
     */
    suspend fun existsById(id: RecommendationId): Boolean
    
    /**
     * 检查用户是否有推荐
     */
    suspend fun existsByUserId(userId: UserId): Boolean
    
    /**
     * 统计用户的推荐数量
     */
    suspend fun countByUserId(userId: UserId): Long
}