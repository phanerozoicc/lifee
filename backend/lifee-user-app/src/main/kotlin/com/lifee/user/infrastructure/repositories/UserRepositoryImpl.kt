package com.lifee.user.infrastructure.repositories

import com.lifee.common.eventsourcing.ConcurrencyException
import com.lifee.common.eventsourcing.EventStore
import com.lifee.common.eventsourcing.SnapshotService
import com.lifee.user.domain.*
import com.lifee.user.infrastructure.entities.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 用户仓储实现
 * 将同步的JPA操作包装为异步接口
 */
@Repository
class UserRepositoryImpl(
    private val jpaUserRepository: JpaUserRepository,
    private val eventStore: EventStore,
    private val snapshotService: SnapshotService
) : UserRepository {
    
    private val logger = LoggerFactory.getLogger(UserRepositoryImpl::class.java)
    private val maxRetryAttempts = 3
    
    @Transactional
    override suspend fun save(user: User): User = withContext(Dispatchers.IO) {
        return@withContext saveWithRetry(user, 0)
    }
    
    /**
     * 带重试机制的保存方法
     */
    private suspend fun saveWithRetry(user: User, attemptCount: Int): User {
        try {
            // 保存聚合根状态
            val entity = UserEntity.fromDomain(user)
            val savedEntity = jpaUserRepository.save(entity)
            
            // 保存事件到事件存储
            if (user.hasUncommittedEvents()) {
                val events = user.getUncommittedEvents()
                eventStore.saveEvents(
                    user.getId().value,
                    events,
                    user.getVersion() - events.size
                )
                user.markEventsAsCommitted()
                
                // 检查是否需要创建快照
                try {
                    snapshotService.createSnapshot(user)
                } catch (e: Exception) {
                    logger.warn("Failed to create snapshot for user {}: {}", 
                        user.getId(), e.message)
                    // 快照创建失败不影响主流程
                }
            }
            
            return savedEntity.toDomain()
            
        } catch (e: ConcurrencyException) {
            logger.warn("User save conflict (attempt {}): aggregateId={}, expectedVersion={}, actualVersion={}", 
                attemptCount + 1, e.aggregateId, e.expectedVersion, e.actualVersion)
            
            if (attemptCount >= maxRetryAttempts - 1) {
                logger.error("User save failed after {} attempts: {}", maxRetryAttempts, e.message)
                throw e
            }
            
            // 重新加载最新版本的聚合根
            val latestUser = findById(user.getId())
                ?: throw IllegalStateException("User not found during retry: ${user.getId()}")
            
            logger.info("Retrying user save (attempt {}): {}", attemptCount + 1, user.getId())
            
            // 延迟重试
            delay((attemptCount + 1) * 100L)
            
            // 递归重试
            return saveWithRetry(latestUser, attemptCount + 1)
            
        } catch (e: Exception) {
            logger.error("Unexpected error saving user: {}", user.getId(), e)
            throw e
        }
    }
    
    override suspend fun findById(id: UserId): User? = withContext(Dispatchers.IO) {
        return@withContext try {
            // 首先尝试从快照恢复
            val snapshot = eventStore.getLatestSnapshot(id.value)
            if (snapshot != null) {
                val user = User.create(
                    id = id,
                    email = Email("temp@temp.com"), // 临时值，将从快照数据中恢复
                    password = Password("tempPassword"), // 临时值，将从快照数据中恢复
                    firstName = "temp", // 临时值，将从快照数据中恢复
                    lastName = "temp" // 临时值，将从快照数据中恢复
                )
                user.restoreFromSnapshot(snapshot as com.lifee.common.domain.AggregateSnapshot<Map<String, Any>>)
                
                // 应用快照之后的事件
                val eventsAfterSnapshot = eventStore.getEvents(
                    id.value
                ).filter { it.version > snapshot.version }
                if (eventsAfterSnapshot.isNotEmpty()) {
                    user.replayEvents(eventsAfterSnapshot)
                }
                
                logger.debug("User {} restored from snapshot at version {}", 
                    id, snapshot.version)
                user
            } else {
                // 如果没有快照，从JPA加载
                val idValue: String = id.value
                jpaUserRepository.findById(idValue).orElse(null)?.toDomain()
            }
        } catch (e: Exception) {
            logger.warn("Failed to restore user {} from snapshot, falling back to JPA: {}", 
                id, e.message)
            // 快照恢复失败时回退到JPA
            val idValue: String = id.value
            jpaUserRepository.findById(idValue).orElse(null)?.toDomain()
        }
    }
    
    override suspend fun findByEmail(email: Email): User? = withContext(Dispatchers.IO) {
        jpaUserRepository.findByEmail(email.value)?.toDomain()
    }
    
    override suspend fun existsByEmail(email: Email): Boolean = withContext(Dispatchers.IO) {
        jpaUserRepository.existsByEmail(email.value)
    }
    
    override suspend fun findByStatus(status: UserStatus, limit: Int, offset: Int): List<User> = withContext(Dispatchers.IO) {
        val pageable = PageRequest.of(offset / limit, limit)
        jpaUserRepository.findByStatusOrderByCreatedAtDesc(status, pageable)
            .map { it.toDomain() }
    }
    
    override suspend fun findPendingActivationUsers(olderThanHours: Int): List<User> = withContext(Dispatchers.IO) {
        val cutoffTime = LocalDateTime.now().minusHours(olderThanHours.toLong())
        jpaUserRepository.findPendingActivationUsers(cutoffTime)
            .map { it.toDomain() }
    }
    
    override suspend fun countByStatus(status: UserStatus): Long = withContext(Dispatchers.IO) {
        jpaUserRepository.countByStatus(status)
    }
    
    override suspend fun delete(user: User): Unit = withContext(Dispatchers.IO) {
        val idValue: String = user.getId().value
        jpaUserRepository.deleteById(idValue)
    }
    
    override suspend fun deleteById(id: UserId): Unit = withContext(Dispatchers.IO) {
        val idValue: String = id.value
        jpaUserRepository.deleteById(idValue)
    }
}