package com.lifee.user.infrastructure.repositories

import com.lifee.user.domain.*
import com.lifee.user.infrastructure.entities.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * 用户仓储实现
 * 将同步的JPA操作包装为异步接口
 */
@Repository
class UserRepositoryImpl(
    private val jpaUserRepository: JpaUserRepository
) : UserRepository {
    
    override suspend fun save(user: User): User = withContext(Dispatchers.IO) {
        val entity = UserEntity.fromDomain(user)
        val savedEntity = jpaUserRepository.save(entity)
        savedEntity.toDomain()
    }
    
    override suspend fun findById(id: UserId): User? = withContext(Dispatchers.IO) {
        val idValue: String = id.value
        jpaUserRepository.findById(idValue).orElse(null)?.toDomain()
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