package com.lifee.knowledge.infrastructure.persistence.repositories


import com.lifee.knowledge.domain.aggregates.KnowledgeBase
import com.lifee.knowledge.domain.repositories.KnowledgeBaseRepository
import com.lifee.knowledge.domain.valueobjects.KnowledgeBaseId
import com.lifee.common.domain.valueobjects.UserId
import com.lifee.knowledge.infrastructure.persistence.mappers.KnowledgeBaseMapper
import com.lifee.knowledge.infrastructure.persistence.repositories.JpaKnowledgeBaseRepository

import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * 知识库Repository实现
 */
@Repository
@Transactional
class KnowledgeBaseRepositoryImpl(
    private val jpaRepository: JpaKnowledgeBaseRepository,
    private val mapper: KnowledgeBaseMapper
) : KnowledgeBaseRepository {
    
    private val logger = LoggerFactory.getLogger(KnowledgeBaseRepositoryImpl::class.java)
    
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
        val ownerUuid = UUID.fromString("00000000-0000-0000-0000-" + ownerId.value.substring(1).padEnd(12, '0'))
        val entities = jpaRepository.findByOwnerId(ownerUuid)
        return entities.map { mapper.toDomain(it) }
    }
    
    @Transactional(readOnly = true)
    override suspend fun findByOwnerIdAndName(ownerId: UserId, name: String): KnowledgeBase? {
        val ownerUuid = UUID.fromString("00000000-0000-0000-0000-" + ownerId.value.substring(1).padEnd(12, '0'))
        val entity = jpaRepository.findByOwnerIdAndName(ownerUuid, name)
        return entity?.let { mapper.toDomain(it) }
    }
    
    @Transactional(readOnly = true)
    override suspend fun findByOwnerIdWithPagination(ownerId: UserId, offset: Int, limit: Int): List<KnowledgeBase> {
        val pageable = PageRequest.of(offset / limit, limit)
        val ownerUuid = UUID.fromString("00000000-0000-0000-0000-" + ownerId.value.substring(1).padEnd(12, '0'))
        val entities = jpaRepository.findByOwnerId(ownerUuid, pageable)
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
        val ownerUuid = UUID.fromString("00000000-0000-0000-0000-" + ownerId.value.substring(1).padEnd(12, '0'))
        return jpaRepository.existsByOwnerIdAndName(ownerUuid, name)
    }
    
    @Transactional(readOnly = true)
    override suspend fun countByOwnerId(ownerId: UserId): Long {
        val ownerUuid = UUID.fromString("00000000-0000-0000-0000-" + ownerId.value.substring(1).padEnd(12, '0'))
        return jpaRepository.countByOwnerId(ownerUuid)
    }
}