package com.lifee.knowledge.infrastructure.persistence.repositories

import com.lifee.common.eventsourcing.EventStore
import com.lifee.common.eventsourcing.SnapshotService
import com.lifee.common.exceptions.ConcurrencyException
import com.lifee.knowledge.domain.aggregates.KnowledgeBase
import com.lifee.knowledge.domain.repositories.KnowledgeBaseRepository
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.knowledge.domain.valueobjects.UserId
import com.lifee.knowledge.infrastructure.persistence.mappers.KnowledgeBaseMapper
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * 知识库Repository实现
 */
@Repository
@Transactional
class KnowledgeBaseRepositoryImpl(
    private val jpaRepository: JpaKnowledgeBaseRepository,
    private val mapper: KnowledgeBaseMapper,
    private val eventStore: EventStore,
    private val snapshotService: SnapshotService
) : KnowledgeBaseRepository {
    
    private val logger = LoggerFactory.getLogger(KnowledgeBaseRepositoryImpl::class.java)
    
    override suspend fun save(knowledgeBase: KnowledgeBase): KnowledgeBase {
        return saveWithRetry(knowledgeBase, 0)
    }
    
    private suspend fun saveWithRetry(knowledgeBase: KnowledgeBase, retryCount: Int): KnowledgeBase {
        try {
            // 保存聚合根状态
            val entity = mapper.toEntity(knowledgeBase)
            val savedEntity = jpaRepository.save(entity)
            
            // 保存未提交的事件到事件存储
            val uncommittedEvents = knowledgeBase.getUncommittedEvents()
            if (uncommittedEvents.isNotEmpty()) {
                eventStore.saveEvents(knowledgeBase.id.value, uncommittedEvents, knowledgeBase.version)
                knowledgeBase.markEventsAsCommitted()
                
                // 检查是否需要创建快照
                try {
                    snapshotService.createSnapshotIfNeeded(knowledgeBase)
                } catch (e: Exception) {
                    logger.warn("Failed to create snapshot for knowledge base {}: {}", 
                        knowledgeBase.id, e.message)
                    // 快照创建失败不影响主流程
                }
            }
            
            return mapper.toDomain(savedEntity)
        } catch (ex: ConcurrencyException) {
            logger.warn("知识库保存时发生并发冲突，聚合根ID: ${knowledgeBase.id.value}, 重试次数: $retryCount", ex)
            
            if (retryCount >= 3) {
                logger.error("知识库保存重试次数已达上限，聚合根ID: ${knowledgeBase.id.value}")
                throw ex
            }
            
            // 重新加载最新的聚合根
            val latestKnowledgeBase = findById(knowledgeBase.id)
                ?: throw IllegalStateException("无法重新加载知识库聚合根: ${knowledgeBase.id.value}")
            
            // 延迟重试，使用指数退避
            val delayMs = (100L * (1 shl retryCount))
            delay(delayMs)
            
            return saveWithRetry(latestKnowledgeBase, retryCount + 1)
        } catch (ex: Exception) {
            logger.error("知识库保存时发生未知错误，聚合根ID: ${knowledgeBase.id.value}", ex)
            throw ex
        }
    }
    
    @Transactional(readOnly = true)
    override suspend fun findById(id: KnowledgeBaseId): KnowledgeBase? {
        return try {
            // 首先尝试从快照恢复
            val snapshot = eventStore.getLatestSnapshot(id.value)
            if (snapshot != null) {
                val knowledgeBase = KnowledgeBase.create(
                    name = com.lifee.knowledge.domain.valueobjects.KnowledgeBaseName.of("temp"), // 临时值，将从快照数据中恢复
                    description = com.lifee.knowledge.domain.valueobjects.KnowledgeBaseDescription.of("temp"), // 临时值，将从快照数据中恢复
                    ownerId = com.lifee.knowledge.domain.valueobjects.UserId.of("temp") // 临时值，将从快照数据中恢复
                )
                knowledgeBase.restoreFromSnapshot(snapshot)
                
                // 应用快照之后的事件
                val eventsAfterSnapshot = eventStore.getEventsAfterVersion(
                    id.value, 
                    snapshot.version
                )
                eventsAfterSnapshot.forEach { event ->
                    knowledgeBase.applyEvent(event)
                }
                
                logger.debug("KnowledgeBase {} restored from snapshot at version {}", 
                    id, snapshot.version)
                knowledgeBase
            } else {
                // 如果没有快照，从JPA加载
                val entity = jpaRepository.findByIdWithDocuments(id.value)
                entity?.let { mapper.toDomain(it) }
            }
        } catch (e: Exception) {
            logger.warn("Failed to restore knowledge base {} from snapshot, falling back to JPA: {}", 
                id, e.message)
            // 快照恢复失败时回退到JPA
            val entity = jpaRepository.findByIdWithDocuments(id.value)
            entity?.let { mapper.toDomain(it) }
        }
    }
    
    @Transactional(readOnly = true)
    override suspend fun findByOwnerId(ownerId: UserId): List<KnowledgeBase> {
        val entities = jpaRepository.findByOwnerId(ownerId.value)
        return entities.map { mapper.toDomain(it) }
    }
    
    @Transactional(readOnly = true)
    override suspend fun findByOwnerIdAndName(ownerId: UserId, name: String): KnowledgeBase? {
        val entity = jpaRepository.findByOwnerIdAndName(ownerId.value, name)
        return entity?.let { mapper.toDomain(it) }
    }
    
    @Transactional(readOnly = true)
    override suspend fun findByOwnerIdWithPagination(ownerId: UserId, offset: Int, limit: Int): List<KnowledgeBase> {
        val pageable = PageRequest.of(offset / limit, limit)
        val entities = jpaRepository.findByOwnerId(ownerId.value, pageable)
        return entities.map { mapper.toDomain(it) }
    }
    
    override suspend fun delete(id: KnowledgeBaseId) {
        jpaRepository.deleteById(id.value)
    }
    
    @Transactional(readOnly = true)
    override suspend fun existsById(id: KnowledgeBaseId): Boolean {
        return jpaRepository.existsById(id.value)
    }
    
    @Transactional(readOnly = true)
    override suspend fun existsByOwnerIdAndName(ownerId: UserId, name: String): Boolean {
        return jpaRepository.existsByOwnerIdAndName(ownerId.value, name)
    }
    
    @Transactional(readOnly = true)
    override suspend fun countByOwnerId(ownerId: UserId): Long {
        return jpaRepository.countByOwnerId(ownerId.value)
    }
}