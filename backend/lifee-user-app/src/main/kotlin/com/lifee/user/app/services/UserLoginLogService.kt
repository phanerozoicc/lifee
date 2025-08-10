package com.lifee.user.app.services

import com.lifee.user.domain.*
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * 用户登录日志服务
 */
@Service
class UserLoginLogService(
    private val userLoginLogRepository: UserLoginLogRepository
) {
    
    /**
     * 获取用户登录历史
     */
    suspend fun getUserLoginHistory(
        userId: UserId,
        limit: Int = 20,
        offset: Int = 0
    ): List<UserLoginLog> {
        return userLoginLogRepository.findByUserId(userId, limit, offset)
    }
    
    /**
     * 获取邮箱登录历史
     */
    suspend fun getEmailLoginHistory(
        email: String,
        limit: Int = 20,
        offset: Int = 0
    ): List<UserLoginLog> {
        return userLoginLogRepository.findByEmail(email, limit, offset)
    }
    
    /**
     * 获取IP地址登录历史
     */
    suspend fun getIpLoginHistory(
        ipAddress: String,
        limit: Int = 20,
        offset: Int = 0
    ): List<UserLoginLog> {
        return userLoginLogRepository.findByIpAddress(ipAddress, limit, offset)
    }
    
    /**
     * 检查邮箱是否被锁定（基于失败次数）
     */
    suspend fun isEmailLocked(
        email: String,
        maxFailures: Int = 5,
        lockDurationMinutes: Long = 30
    ): Boolean {
        val since = Instant.now().minus(lockDurationMinutes, ChronoUnit.MINUTES)
        val failureCount = userLoginLogRepository.countFailedLoginsByEmail(email, since)
        return failureCount >= maxFailures
    }
    
    /**
     * 检查IP地址是否被锁定（基于失败次数）
     */
    suspend fun isIpAddressLocked(
        ipAddress: String,
        maxFailures: Int = 10,
        lockDurationMinutes: Long = 30
    ): Boolean {
        val since = Instant.now().minus(lockDurationMinutes, ChronoUnit.MINUTES)
        val failureCount = userLoginLogRepository.countFailedLoginsByIpAddress(ipAddress, since)
        return failureCount >= maxFailures
    }
    
    /**
     * 获取登录失败统计
     */
    suspend fun getFailureStatistics(
        email: String? = null,
        ipAddress: String? = null,
        hours: Long = 24
    ): LoginFailureStatistics {
        val since = Instant.now().minus(hours, ChronoUnit.HOURS)
        
        val emailFailures = email?.let {
            userLoginLogRepository.countFailedLoginsByEmail(it, since)
        } ?: 0L
        
        val ipFailures = ipAddress?.let {
            userLoginLogRepository.countFailedLoginsByIpAddress(it, since)
        } ?: 0L
        
        return LoginFailureStatistics(
            emailFailures = emailFailures,
            ipFailures = ipFailures,
            timeRangeHours = hours
        )
    }
    
    /**
     * 清理过期的登录日志
     */
    suspend fun cleanupOldLogs(retentionDays: Long = 90): Long {
        val cutoffTime = Instant.now().minus(retentionDays, ChronoUnit.DAYS)
        return userLoginLogRepository.deleteOldLogs(cutoffTime)
    }
}

/**
 * 登录失败统计
 */
data class LoginFailureStatistics(
    val emailFailures: Long,
    val ipFailures: Long,
    val timeRangeHours: Long
)