package com.lifee.user.infrastructure.repositories

import com.lifee.user.domain.*
import com.lifee.user.infrastructure.entities.UserLoginLogEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Repository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * 用户登录日志仓储实现
 */
@Repository
class UserLoginLogRepositoryImpl(
    private val jpaRepository: JpaUserLoginLogRepository
) : UserLoginLogRepository {
    
    override suspend fun save(loginLog: UserLoginLog): UserLoginLog = withContext(Dispatchers.IO) {
        val entity = UserLoginLogEntity.fromDomain(loginLog)
        val savedEntity = jpaRepository.save(entity)
        savedEntity.toDomain()
    }
    
    override suspend fun findById(id: UserLoginLogId): UserLoginLog? = withContext(Dispatchers.IO) {
        jpaRepository.findById(id.value)
            .map { it.toDomain() }
            .orElse(null)
    }
    
    override suspend fun findByUserId(
        userId: UserId,
        limit: Int,
        offset: Int
    ): List<UserLoginLog> = withContext(Dispatchers.IO) {
        val pageable = PageRequest.of(offset / limit, limit)
        jpaRepository.findByUserIdOrderByCreatedAtDesc(userId.value, pageable)
            .map { it.toDomain() }
    }
    
    override suspend fun findByEmail(
        email: String,
        limit: Int,
        offset: Int
    ): List<UserLoginLog> = withContext(Dispatchers.IO) {
        val pageable = PageRequest.of(offset / limit, limit)
        jpaRepository.findByEmailOrderByCreatedAtDesc(email, pageable)
            .map { it.toDomain() }
    }
    
    override suspend fun findByIpAddress(
        ipAddress: String,
        limit: Int,
        offset: Int
    ): List<UserLoginLog> = withContext(Dispatchers.IO) {
        val pageable = PageRequest.of(offset / limit, limit)
        jpaRepository.findByIpAddressOrderByCreatedAtDesc(ipAddress, pageable)
            .map { it.toDomain() }
    }
    
    override suspend fun findByLoginResult(
        loginResult: LoginResult,
        limit: Int,
        offset: Int
    ): List<UserLoginLog> = withContext(Dispatchers.IO) {
        val pageable = PageRequest.of(offset / limit, limit)
        jpaRepository.findByLoginResultOrderByCreatedAtDesc(loginResult, pageable)
            .map { it.toDomain() }
    }
    
    override suspend fun findByTimeRange(
        startTime: Instant,
        endTime: Instant,
        limit: Int,
        offset: Int
    ): List<UserLoginLog> = withContext(Dispatchers.IO) {
        val pageable = PageRequest.of(offset / limit, limit)
        val startDateTime = LocalDateTime.ofInstant(startTime, ZoneOffset.UTC)
        val endDateTime = LocalDateTime.ofInstant(endTime, ZoneOffset.UTC)
        
        jpaRepository.findByCreatedAtBetweenOrderByCreatedAtDesc(
            startDateTime,
            endDateTime,
            pageable
        ).map { it.toDomain() }
    }
    
    override suspend fun countFailedLoginsByEmail(
        email: String,
        since: Instant
    ): Long = withContext(Dispatchers.IO) {
        val sinceDateTime = LocalDateTime.ofInstant(since, ZoneOffset.UTC)
        val failureResults = listOf(
            LoginResult.FAILED_INVALID_CREDENTIALS,
            LoginResult.FAILED_ACCOUNT_LOCKED,
            LoginResult.FAILED_ACCOUNT_DISABLED
        )
        
        jpaRepository.countFailedLoginsByEmail(email, failureResults, sinceDateTime)
    }
    
    override suspend fun countFailedLoginsByIpAddress(
        ipAddress: String,
        since: Instant
    ): Long = withContext(Dispatchers.IO) {
        val sinceDateTime = LocalDateTime.ofInstant(since, ZoneOffset.UTC)
        val failureResults = listOf(
            LoginResult.FAILED_INVALID_CREDENTIALS,
            LoginResult.FAILED_ACCOUNT_LOCKED,
            LoginResult.FAILED_ACCOUNT_DISABLED
        )
        
        jpaRepository.countFailedLoginsByIpAddress(ipAddress, failureResults, sinceDateTime)
    }
    
    override suspend fun deleteOldLogs(before: Instant): Long = withContext(Dispatchers.IO) {
        val beforeDateTime = LocalDateTime.ofInstant(before, ZoneOffset.UTC)
        jpaRepository.deleteByCreatedAtBefore(beforeDateTime).toLong()
    }
}