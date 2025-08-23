package com.lifee.chat.infrastructure.persistence.repositories

import com.lifee.chat.domain.aggregates.Conversation
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.common.domain.valueobjects.UserId
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * 对话Repository实现
 */
@Repository
@Transactional
class ConversationRepositoryImpl(
    private val jpaRepository: JpaConversationRepository,
    private val mapper: ConversationMapper
) : ConversationRepository {
    
    private val logger = LoggerFactory.getLogger(ConversationRepositoryImpl::class.java)
    
    override suspend fun save(conversation: Conversation): Conversation {
        try {
            // 保存聚合根状态
            val entity = mapper.toEntity(conversation)
            val savedEntity = jpaRepository.save(entity)
            return mapper.toDomain(savedEntity)
        } catch (ex: Exception) {
            logger.error("对话保存时发生错误，聚合根ID: ${conversation.getConversationId()}", ex)
            throw ex
        }
    }
    
    @Transactional(readOnly = true)
    override suspend fun findById(id: ConversationId): Conversation? {
        return try {
            // 首先尝试从快照恢复
            // 直接从JPA加载，暂时不使用事件溯源
            val entity = jpaRepository.findByIdWithMessages(UUID.fromString(id.toString()))
            entity?.let { mapper.toDomain(it) }
        } catch (e: Exception) {
            logger.warn("Failed to load conversation {} from JPA: {}", 
                id, e.message)
            null
        }
    }
    
    @Transactional(readOnly = true)
    override suspend fun findByIdAndUserId(id: ConversationId, userId: UserId): Conversation? {
        val entity = jpaRepository.findByIdAndUserId(UUID.fromString(id.toString()), userId.toString())
        return entity?.let { mapper.toDomain(it) }
    }
    
    @Transactional(readOnly = true)
    override suspend fun findByUserId(userId: UserId): List<Conversation> {
        val entities = jpaRepository.findByUserId(userId.toString())
        return entities.map { mapper.toDomain(it) }
    }
    
    @Transactional(readOnly = true)
    override suspend fun findByUserId(userId: UserId, pageable: Pageable): Page<Conversation> {
        val entityPage = jpaRepository.findByUserId(userId.toString(), pageable)
        return entityPage.map { mapper.toDomain(it) }
    }
    
    override suspend fun delete(conversation: Conversation) {
        jpaRepository.deleteById(UUID.fromString(conversation.getConversationId().toString()))
    }
    
    override suspend fun deleteById(id: ConversationId) {
        jpaRepository.deleteById(UUID.fromString(id.toString()))
    }
    
    override suspend fun existsById(id: ConversationId): Boolean {
        return jpaRepository.existsById(UUID.fromString(id.toString()))
    }
    
    override suspend fun existsByIdAndUserId(id: ConversationId, userId: UserId): Boolean {
        return jpaRepository.existsByIdAndUserId(UUID.fromString(id.toString()), userId.toString())
    }
    
    override suspend fun countByUserId(userId: UserId): Long {
        return jpaRepository.countByUserId(userId.toString())
    }
    

}