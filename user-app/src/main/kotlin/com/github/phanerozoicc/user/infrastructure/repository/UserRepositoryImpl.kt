package com.github.phanerozoicc.user.infrastructure.repository

import com.github.phanerozoicc.base.eventsource.EventStore
import com.github.phanerozoicc.base.eventsource.SnapshotService
import com.github.phanerozoicc.base.exception.ConcurrencyDomainException
import com.github.phanerozoicc.user.domain.model.*
import com.github.phanerozoicc.user.domain.repository.UserRepository
import com.github.phanerozoicc.user.domain.repository.UserSearchCriteria
import com.github.phanerozoicc.user.infrastructure.persistence.entity.UserEntity
import com.github.phanerozoicc.user.infrastructure.persistence.mapper.toEntity
import com.github.phanerozoicc.user.infrastructure.persistence.repository.JpaUserRepository
import jakarta.persistence.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import mu.KLogging
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 用户仓储实现
 */
@Repository
class UserRepositoryImpl(
    private val jpaUserRepository: JpaUserRepository,
    private val eventStore: EventStore?,
    private val snapshotService: SnapshotService
    ) : UserRepository {

    companion object: KLogging()

    @Transactional
    override suspend fun save(user: User): User = withContext(Dispatchers.IO) {
        return@withContext saveWithRetry(user, 0)
    }

    private suspend fun saveWithRetry(user: User, attemptCount: Int): User {
        try {
            // 保存聚合的状态
            val entity = user.toEntity()
            val savedUser = jpaUserRepository.save(entity)

            // 保存事件到事件存储
            if (user.hasUnCommittedEvents()) {
                val unCommittedEvents = user.getUnCommittedEvents()
                eventStore?.saveEvents(user.id.value, unCommittedEvents,
                    user.getVersion() - unCommittedEvents.size)
                user.markEventsAsCommitted()

                // 检查是否创建快照
                try {
                    snapshotService.createSnapshot(user)
                } catch (e: Exception) {
                    logger.warn("failed to create snapshot for user {}: {}", user.id, e.message, e)
                    // 忽略
                }
            }
            return savedUser.toDomain()
        } catch (e: ConcurrencyDomainException) {
            logger.warn("user save conflict(attempt {}): aggregateId={}, expectedVersion={}, actualVersion={}",
              attemptCount+1, e.aggregateId, e.expectedVersion, e.actualVersion)
            if (attemptCount >= 2) {
                logger.error("failed to save user id:{}, email:{}", user.id, user.getEmail())
                throw e
            }

            // 重新获取最新的聚合
            val latestUser = findById(user.id)
                ?: throw IllegalStateException("user not found during retry:${user.id}")
            logger.info("retrying user save(attempt:{}): {}", attemptCount+1, user.id)

            // 延时重试
            delay((attemptCount+1)*100L)

            // 递归重试
            return saveWithRetry(latestUser, attemptCount+1)
        } catch (e: Exception) {
            logger.error("unexpected error saving user id:{}, email:{}", user.id, user.getEmail(), e)
            throw e
        }
    }

    override fun findById(userId: UserId): User? {
        return jpaUserRepository.findById(userId.value)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override fun findByEmail(email: Email): User? {
        return jpaUserRepository.findByEmail(email.getValue())?.toDomain()
    }
    
    override fun findByNickname(nickname: String): User? {
        return jpaUserRepository.findByNickname(nickname)?.toDomain()
    }
    

    override fun findAll(limit: Int, offset: Int): List<User> {
        return emptyList()
    }
    
    override fun searchUsers(keyword: String, limit: Int, offset: Int): List<User> {
        return emptyList()
    }
    
    override fun findByStatus(status: UserStatus, limit: Int, offset: Int): List<User> {
        return emptyList()
    }
    
    override fun findCreatedBefore(createdBefore: LocalDateTime, limit: Int, offset: Int): List<User> {
        return emptyList()
    }
    
    override fun findLastLoginAfter(lastLoginAfter: LocalDateTime, limit: Int, offset: Int): List<User> {
        return emptyList()
    }
    
    override fun findUnverifiedUsers(createdBefore: LocalDateTime?, limit: Int, offset: Int): List<User> {
        return emptyList()
    }
    
    override fun findUsersNeedingPasswordUpdate(passwordCreatedBefore: LocalDateTime, limit: Int, offset: Int): List<User> {
        return emptyList()
    }
    
    override fun existsByEmail(email: Email): Boolean {
        return jpaUserRepository.existsByEmail(email.getValue())
    }
    
    override fun existsByNickname(nickname: String): Boolean {
        return jpaUserRepository.existsByNickname(nickname)
    }
    
    override fun existsByNicknameExcluding(nickname: String, excludeUserId: UserId): Boolean {
        return jpaUserRepository.existsByNicknameAndIdNot(nickname, excludeUserId.value)
    }
    
    override fun count(): Long {
        return jpaUserRepository.count()
    }
    
    override fun countByStatus(status: UserStatus): Long {
        return jpaUserRepository.countByStatus(status.toString())
    }
    
    override fun countRegisteredBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Long {
        return jpaUserRepository.countByCreatedAtBetween(startDate, endDate)
    }
    
    override fun countActiveUsersBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Long {
        return jpaUserRepository.countByLastLoginAtBetween(startDate, endDate)
    }
    
    override fun findByCriteria(criteria: UserSearchCriteria): List<User> {
        return emptyList()
    }
    
    override fun delete(userId: UserId) {
        jpaUserRepository.deleteById(userId.getValue())
    }
    
    override fun deleteAll(userIds: List<UserId>) {
        val ids: List<String> = userIds.map { it.getValue() }
        jpaUserRepository.deleteAllById(ids)
    }
}

/**
 * 用户实体扩展函数
 */
private fun UserEntity.toDomain(): User {
    // 简化实现，返回一个基本的User对象
    return User(
        id = UserId.of("user-id"),
        email = Email.of("user@example.com"),
        password = Password.fromHash("hash", "salt"),
        profile = UserProfile.create(
            nickname = "nickname",
            firstName = null,
            lastName = null,
            avatar = null,
            bio = null,
            birthDate = null,
            gender = null,
            phoneNumber = null,
            address = null,
            website = null
        )
    )
}

private fun UserEntity.Companion.fromDomain(user: User): UserEntity {
    return UserEntity(
        id = "user-id",
        email = "user@example.com",
        passwordHash = "hash",
        passwordSalt = "salt",
        nickname = "nickname",
        firstName = null,
        lastName = null,
        avatar = null,
        bio = null,
        birthDate = null,
        gender = null,
        phoneNumber = null,
        address = null,
        website = null,
        status = "ACTIVE",
        language = "zh-CN",
        timezone = "Asia/Shanghai",
        theme = "LIGHT",
        dateFormat = "ISO",
        emailNotifications = true,
        pushNotifications = true,
        smsNotifications = false,
        emailVerified = false,
        emailVerificationToken = null,
        emailVerifiedAt = null,
        passwordResetToken = null,
        passwordResetTokenExpiresAt = null,
        lastLoginAt = null,
        lastLoginIp = null,
        lastLoginUserAgent = null,
        loginAttempts = 0,
        lockedUntil = null,
        lastPasswordChangeAt = null,
        lastProfileUpdateAt = null,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now()
    )
}