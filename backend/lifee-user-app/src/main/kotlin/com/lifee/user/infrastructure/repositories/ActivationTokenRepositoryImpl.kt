package com.lifee.user.infrastructure.repositories

import com.lifee.user.domain.ActivationToken
import com.lifee.user.domain.ActivationTokenRepository
import com.lifee.user.domain.UserId
import com.lifee.user.infrastructure.entities.ActivationTokenEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * 激活令牌仓储实现
 */
@Repository
class ActivationTokenRepositoryImpl(
    private val jpaActivationTokenRepository: JpaActivationTokenRepository
) : ActivationTokenRepository {
    
    override suspend fun save(token: ActivationToken): ActivationToken = withContext(Dispatchers.IO) {
        val entity = ActivationTokenEntity.fromDomain(token)
        val savedEntity = jpaActivationTokenRepository.save(entity)
        savedEntity.toDomain()
    }
    
    override suspend fun findByToken(tokenValue: String): ActivationToken? = withContext(Dispatchers.IO) {
        jpaActivationTokenRepository.findByTokenValue(tokenValue)?.toDomain()
    }
    
    override suspend fun findValidTokenByUserId(userId: UserId): ActivationToken? = withContext(Dispatchers.IO) {
        jpaActivationTokenRepository.findValidTokenByUserId(userId.value.toString())?.toDomain()
    }
    
    override suspend fun delete(token: ActivationToken) = withContext(Dispatchers.IO) {
        jpaActivationTokenRepository.deleteById(token.value)
    }
    
    @Transactional
    override suspend fun deleteByUserId(userId: UserId) = withContext(Dispatchers.IO) {
        jpaActivationTokenRepository.deleteByUserId(userId.value.toString())
    }
    
    @Transactional
    override suspend fun deleteExpiredTokens() = withContext(Dispatchers.IO) {
        jpaActivationTokenRepository.deleteExpiredTokens()
    }
}