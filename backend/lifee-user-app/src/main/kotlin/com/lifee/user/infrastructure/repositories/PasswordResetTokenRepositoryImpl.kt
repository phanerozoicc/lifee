package com.lifee.user.infrastructure.repositories

import com.lifee.user.domain.PasswordResetToken
import com.lifee.user.domain.PasswordResetTokenRepository
import com.lifee.user.domain.UserId
import com.lifee.user.infrastructure.entities.PasswordResetTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * 密码重置令牌仓储实现
 */
@Repository
class PasswordResetTokenRepositoryImpl(
    private val jpaRepository: JpaPasswordResetTokenRepository
) : PasswordResetTokenRepository {
    
    @Transactional
    override suspend fun save(token: PasswordResetToken): PasswordResetToken = withContext(Dispatchers.IO) {
        val entity = PasswordResetTokenEntity.fromDomain(token)
        val savedEntity = jpaRepository.save(entity)
        savedEntity.toDomain()
    }
    
    override suspend fun findByToken(tokenValue: String): PasswordResetToken? = withContext(Dispatchers.IO) {
        jpaRepository.findByTokenValue(tokenValue)?.toDomain()
    }
    
    override suspend fun findValidTokenByUserId(userId: UserId): PasswordResetToken? = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        jpaRepository.findValidTokenByUserId(userId.value, now)?.toDomain()
    }
    
    @Transactional
    override suspend fun delete(token: PasswordResetToken) = withContext(Dispatchers.IO) {
        jpaRepository.deleteById(token.value)
    }
    
    @Transactional
    override suspend fun deleteAllByUserId(userId: UserId) = withContext(Dispatchers.IO) {
        jpaRepository.deleteAllByUserId(userId.value)
    }
    
    @Transactional
    override suspend fun deleteExpiredTokens() = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        jpaRepository.deleteExpiredTokens(now)
    }
    
    override suspend fun hasValidTokenForUser(userId: UserId): Boolean = withContext(Dispatchers.IO) {
        val now = LocalDateTime.now(ZoneOffset.UTC)
        jpaRepository.hasValidTokenForUser(userId.value, now)
    }
}