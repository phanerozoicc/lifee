package com.lifee.user.infrastructure.repositories

import com.lifee.user.domain.LoginResult
import com.lifee.user.infrastructure.entities.UserLoginLogEntity
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * 用户登录日志JPA仓储接口
 */
@Repository
interface JpaUserLoginLogRepository : JpaRepository<UserLoginLogEntity, String> {
    
    /**
     * 根据用户ID查找登录日志
     */
    fun findByUserIdOrderByCreatedAtDesc(
        userId: String,
        pageable: Pageable
    ): List<UserLoginLogEntity>
    
    /**
     * 根据邮箱查找登录日志
     */
    fun findByEmailOrderByCreatedAtDesc(
        email: String,
        pageable: Pageable
    ): List<UserLoginLogEntity>
    
    /**
     * 根据IP地址查找登录日志
     */
    fun findByIpAddressOrderByCreatedAtDesc(
        ipAddress: String,
        pageable: Pageable
    ): List<UserLoginLogEntity>
    
    /**
     * 根据登录结果查找登录日志
     */
    fun findByLoginResultOrderByCreatedAtDesc(
        loginResult: LoginResult,
        pageable: Pageable
    ): List<UserLoginLogEntity>
    
    /**
     * 根据时间范围查找登录日志
     */
    fun findByCreatedAtBetweenOrderByCreatedAtDesc(
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        pageable: Pageable
    ): List<UserLoginLogEntity>
    
    /**
     * 统计用户登录失败次数（在指定时间范围内）
     */
    @Query("""
        SELECT COUNT(l) FROM UserLoginLogEntity l 
        WHERE l.email = :email 
        AND l.loginResult IN :failureResults 
        AND l.createdAt >= :since
    """)
    fun countFailedLoginsByEmail(
        @Param("email") email: String,
        @Param("failureResults") failureResults: List<LoginResult>,
        @Param("since") since: LocalDateTime
    ): Long
    
    /**
     * 统计IP地址登录失败次数（在指定时间范围内）
     */
    @Query("""
        SELECT COUNT(l) FROM UserLoginLogEntity l 
        WHERE l.ipAddress = :ipAddress 
        AND l.loginResult IN :failureResults 
        AND l.createdAt >= :since
    """)
    fun countFailedLoginsByIpAddress(
        @Param("ipAddress") ipAddress: String,
        @Param("failureResults") failureResults: List<LoginResult>,
        @Param("since") since: LocalDateTime
    ): Long
    
    /**
     * 删除过期的登录日志
     */
    @Modifying
    @Query("DELETE FROM UserLoginLogEntity l WHERE l.createdAt < :before")
    fun deleteByCreatedAtBefore(@Param("before") before: LocalDateTime): Int
}