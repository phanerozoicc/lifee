package com.lifee.user.domain

import java.time.Instant

/**
 * 用户登录日志仓储接口
 */
interface UserLoginLogRepository {
    
    /**
     * 保存登录日志
     */
    suspend fun save(loginLog: UserLoginLog): UserLoginLog
    
    /**
     * 根据ID查找登录日志
     */
    suspend fun findById(id: UserLoginLogId): UserLoginLog?
    
    /**
     * 根据用户ID查找登录日志
     */
    suspend fun findByUserId(
        userId: UserId,
        limit: Int = 50,
        offset: Int = 0
    ): List<UserLoginLog>
    
    /**
     * 根据邮箱查找登录日志
     */
    suspend fun findByEmail(
        email: String,
        limit: Int = 50,
        offset: Int = 0
    ): List<UserLoginLog>
    
    /**
     * 根据IP地址查找登录日志
     */
    suspend fun findByIpAddress(
        ipAddress: String,
        limit: Int = 50,
        offset: Int = 0
    ): List<UserLoginLog>
    
    /**
     * 根据登录结果查找登录日志
     */
    suspend fun findByLoginResult(
        loginResult: LoginResult,
        limit: Int = 50,
        offset: Int = 0
    ): List<UserLoginLog>
    
    /**
     * 根据时间范围查找登录日志
     */
    suspend fun findByTimeRange(
        startTime: Instant,
        endTime: Instant,
        limit: Int = 50,
        offset: Int = 0
    ): List<UserLoginLog>
    
    /**
     * 统计用户登录失败次数（在指定时间范围内）
     */
    suspend fun countFailedLoginsByEmail(
        email: String,
        since: Instant
    ): Long
    
    /**
     * 统计IP地址登录失败次数（在指定时间范围内）
     */
    suspend fun countFailedLoginsByIpAddress(
        ipAddress: String,
        since: Instant
    ): Long
    
    /**
     * 删除过期的登录日志
     */
    suspend fun deleteOldLogs(before: Instant): Long
}