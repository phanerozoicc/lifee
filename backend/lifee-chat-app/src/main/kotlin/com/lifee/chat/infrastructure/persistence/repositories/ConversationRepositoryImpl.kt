package com.lifee.chat.infrastructure.persistence.repositories

import com.lifee.common.eventsourcing.EventStore
import com.lifee.common.eventsourcing.SnapshotService
import com.lifee.common.exceptions.ConcurrencyException
import com.lifee.chat.domain.aggregates.Conversation
import com.lifee.chat.domain.repositories.ConversationRepository
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.user.domain.UserId
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * 对话Repository实现
 */
@Repository
@Transactional
class ConversationRepositoryImpl(
    private val jpaRepository: JpaConversationRepository,
    private val mapper: ConversationMapper,
    private val eventStore: EventStore,
    private val snapshotService: SnapshotService
) : ConversationRepository {
    
    private val logger = LoggerFactory.getLogger(ConversationRepositoryImpl::class.java)
    
    override suspend fun save(conversation: Conversation): Conversation {
        return saveWithRetry(conversation, 0)
    }
    
    private suspend fun saveWithRetry(conversation: Conversation, retryCount: Int): Conversation {
        try {
            // 保存聚合根状态
            val entity = mapper.toEntity(conversation)
            val savedEntity = jpaRepository.save(entity)
            
            // 保存未提交的事件到事件存储
            val uncommittedEvents = conversation.getUncommittedEvents()
            if (uncommittedEvents.isNotEmpty()) {
                eventStore.saveEvents(conversation.id.value, uncommittedEvents, conversation.version)
                conversation.markEventsAsCommitted()
                
                // 检查是否需要创建快照
                try {
                    snapshotService.createSnapshotIfNeeded(conversation)
                } catch (e: Exception) {
                    logger.warn("Failed to create snapshot for conversation {}: {}", 
                        conversation.id, e.message)
                    // 快照创建失败不影响主流程
                }
            }
            
            return mapper.toDomain(savedEntity)
        } catch (ex: ConcurrencyException) {
            logger.warn("对话保存时发生并发冲突，聚合根ID: ${conversation.id.value}, 重试次数: $retryCount", ex)
            
            if (retryCount >= 3) {
                logger.error("对话保存重试次数已达上限，聚合根ID: ${conversation.id.value}")
                throw ex
            }
            
            // 重新加载最新的聚合根
            val latestConversation = findById(conversation.id)
                ?: throw IllegalStateException("无法重新加载对话聚合根: ${conversation.id.value}")
            
            // 延迟重试，使用指数退避
            val delayMs = (100L * (1 shl retryCount))
            delay(delayMs)
            
            return saveWithRetry(latestConversation, retryCount + 1)
        } catch (ex: Exception) {
            logger.error("对话保存时发生未知错误，聚合根ID: ${conversation.id.value}", ex)
            throw ex
        }
    }
    
    @Transactional(readOnly = true)
    override suspend fun findById(id: ConversationId): Conversation? {
        return try {
            // 首先尝试从快照恢复
            val snapshot = eventStore.getLatestSnapshot(id.value)
            if (snapshot != null) {
                val conversation = Conversation.create(
                    title = com.lifee.chat.domain.valueobjects.ConversationTitle.of("temp"), // 临时值，将从快照数据中恢复
                    userId = UserId.of("temp") // 临时值，将从快照数据中恢复
                )
                conversation.restoreFromSnapshot(snapshot)
                
                // 应用快照之后的事件
                val eventsAfterSnapshot = eventStore.getEventsAfterVersion(
                    id.value, 
                    snapshot.version
                )
                eventsAfterSnapshot.forEach { event ->
                    conversation.applyEvent(event)
                }
                
                logger.debug("Conversation {} restored from snapshot at version {}", 
                    id, snapshot.version)
                conversation
            } else {
                // 如果没有快照，从JPA加载
                val entity = jpaRepository.findByIdWithMessages(id.value)
                entity?.let { mapper.toDomain(it) }
            }
        } catch (e: Exception) {
            logger.warn("Failed to restore conversation {} from snapshot, falling back to JPA: {}", 
                id, e.message)
            // 快照恢复失败时回退到JPA
            val entity = jpaRepository.findByIdWithMessages(id.value)
            entity?.let { mapper.toDomain(it) }
        }
    }
    
    @Transactional(readOnly = true)
    override suspend fun findByIdAndUserId(id: ConversationId, userId: UserId): Conversation? {
        val entity = jpaRepository.findByIdAndUserId(id.value, userId.value)
        return entity?.let { mapper.toDomain(it) }
    }
    
    @Transactional(readOnly = true)
    override suspend fun findByUserId(userId: UserId): List<Conversation> {
        val entities = jpaRepository.findByUserId(userId.value)
        return entities.map { mapper.toDomain(it) }
    }
    
    @Transactional(readOnly = true)
    override suspend fun findByUserIdWithPagination(userId: UserId, pageable: Pageable): Page<Conversation> {
        val entityPage = jpaRepository.findByUserId(userId.value, pageable)
        return entityPage.map { mapper.toDomain(it) }
    }
    
    override suspend fun delete(id: ConversationId) {
        jpaRepository.deleteById(id.value)
    }
    
    @Transactional(readOnly = true)
    override suspend fun existsById(id: ConversationId): Boolean {
        return jpaRepository.existsById(id.value)
    }
    
    @Transactional(readOnly = true)
    override suspend fun existsByIdAndUserId(id: ConversationId, userId: UserId): Boolean {
        return jpaRepository.existsByIdAndUserId(id.value, userId.value)
    }
    
    @Transactional(readOnly = true)
    override suspend fun countByUserId(userId: UserId): Long {
        return jpaRepository.countByUserId(userId.value)
    }
}