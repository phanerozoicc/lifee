package com.lifee.knowledge.infrastructure.persistence.repositories

import com.lifee.knowledge.domain.aggregates.KnowledgeBase
import com.lifee.knowledge.domain.repositories.KnowledgeBaseRepository
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.knowledge.domain.valueobjects.UserId
import com.lifee.knowledge.infrastructure.persistence.mappers.KnowledgeBaseMapper
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
    private val mapper: KnowledgeBaseMapper
) : KnowledgeBaseRepository {
    
    override suspend fun save(knowledgeBase: KnowledgeBase): KnowledgeBase {
        val entity = mapper.toEntity(knowledgeBase)
        val savedEntity = jpaRepository.save(entity)
        return mapper.toDomain(savedEntity)
    }
    
    @Transactional(readOnly = true)
    override suspend fun findById(id: KnowledgeBaseId): KnowledgeBase? {
        val entity = jpaRepository.findByIdWithDocuments(id.value)
        return entity?.let { mapper.toDomain(it) }
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