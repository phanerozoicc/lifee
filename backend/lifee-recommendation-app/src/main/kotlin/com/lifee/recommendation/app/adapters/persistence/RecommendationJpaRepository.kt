package com.lifee.recommendation.app.adapters.persistence

import com.lifee.common.eventsourcing.EventStore
import com.lifee.common.eventsourcing.SnapshotService
import com.lifee.common.exceptions.ConcurrencyException
import com.lifee.recommendation.domain.aggregates.Recommendation
import com.lifee.recommendation.domain.repositories.RecommendationRepository
import com.lifee.recommendation.domain.valueobjects.RecommendationId
import com.lifee.recommendation.domain.valueobjects.UserId
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
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
    private val springDataRepository: SpringDataRecommendationRepository,
    private val eventStore: EventStore,
    private val snapshotService: SnapshotService
) : RecommendationRepository {
    
    private val logger = LoggerFactory.getLogger(RecommendationJpaRepositoryImpl::class.java)
    
    override fun save(recommendation: Recommendation): Recommendation {
        return runBlocking {
            saveWithRetry(recommendation, 0)
        }
    }
    
    private suspend fun saveWithRetry(recommendation: Recommendation, retryCount: Int): Recommendation {
        try {
            // 保存聚合根状态
            val entity = RecommendationJpaEntity.from(recommendation)
            val savedEntity = springDataRepository.save(entity)
            
            // 保存未提交的事件到事件存储
            val uncommittedEvents = recommendation.getUncommittedEvents()
            if (uncommittedEvents.isNotEmpty()) {
                eventStore.saveEvents(recommendation.id.value.toString(), uncommittedEvents, recommendation.version)
                recommendation.markEventsAsCommitted()
                
                // 检查是否需要创建快照
                try {
                    snapshotService.createSnapshotIfNeeded(recommendation)
                } catch (e: Exception) {
                    logger.warn("Failed to create snapshot for recommendation {}: {}", 
                        recommendation.id, e.message)
                    // 快照创建失败不影响主流程
                }
            }
            
            return savedEntity.toDomain()
        } catch (ex: ConcurrencyException) {
            logger.warn("推荐保存时发生并发冲突，聚合根ID: ${recommendation.id.value}, 重试次数: $retryCount", ex)
            
            if (retryCount >= 3) {
                logger.error("推荐保存重试次数已达上限，聚合根ID: ${recommendation.id.value}")
                throw ex
            }
            
            // 重新加载最新的聚合根
            val latestRecommendation = findById(recommendation.id)
                ?: throw IllegalStateException("无法重新加载推荐聚合根: ${recommendation.id.value}")
            
            // 延迟重试，使用指数退避
            val delayMs = (100L * (1 shl retryCount))
            delay(delayMs)
            
            return saveWithRetry(latestRecommendation, retryCount + 1)
        } catch (ex: Exception) {
            logger.error("推荐保存时发生未知错误，聚合根ID: ${recommendation.id.value}", ex)
            throw ex
        }
    }
    
    override fun findById(id: RecommendationId): Recommendation? {
        return try {
            // 首先尝试从快照恢复
            val snapshot = eventStore.getLatestSnapshot(id.value.toString())
            if (snapshot != null) {
                val recommendation = Recommendation.create(
                    recommendationId = id,
                    userId = com.lifee.recommendation.domain.valueobjects.UserId.of("temp") // 临时值，将从快照数据中恢复
                )
                recommendation.restoreFromSnapshot(snapshot)
                
                // 应用快照之后的事件
                val eventsAfterSnapshot = eventStore.getEventsAfterVersion(
                    id.value.toString(), 
                    snapshot.version
                )
                eventsAfterSnapshot.forEach { event ->
                    recommendation.applyEvent(event)
                }
                
                logger.debug("Recommendation {} restored from snapshot at version {}", 
                    id, snapshot.version)
                recommendation
            } else {
                // 如果没有快照，从JPA加载
                springDataRepository.findById(id.value)
                    .map { it.toDomain() }
                    .orElse(null)
            }
        } catch (e: Exception) {
            logger.warn("Failed to restore recommendation {} from snapshot, falling back to JPA: {}", 
                id, e.message)
            // 快照恢复失败时回退到JPA
            springDataRepository.findById(id.value)
                .map { it.toDomain() }
                .orElse(null)
        }
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