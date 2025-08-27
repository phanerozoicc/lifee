package com.github.phanerozoicc.user.infrastructure.persistence.repository

import com.github.phanerozoicc.user.infrastructure.persistence.entity.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * JPA用户仓储接口
 */
@Repository
interface JpaUserRepository : JpaRepository<UserEntity, String>, JpaSpecificationExecutor<UserEntity> {
    
    /**
     * 根据邮箱查找用户
     */
    fun findByEmail(email: String): UserEntity?
    
    /**
     * 根据昵称查找用户
     */
    fun findByNickname(nickname: String): UserEntity?
    
    /**
     * 根据状态查找用户
     */
    fun findByStatus(status: String, pageable: Pageable): Page<UserEntity>
    
    /**
     * 根据创建时间范围查找用户
     */
    fun findByCreatedAtBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable
    ): Page<UserEntity>
    
    /**
     * 根据最后登录时间范围查找用户
     */
    fun findByLastLoginAtBetween(
        startDate: LocalDateTime,
        endDate: LocalDateTime,
        pageable: Pageable
    ): Page<UserEntity>
    
    /**
     * 查找最后登录时间早于指定时间的用户
     */
    fun findByLastLoginAtBefore(
        date: LocalDateTime,
        pageable: Pageable
    ): Page<UserEntity>
    
    /**
     * 根据邮箱验证状态查找用户
     */
    fun findByEmailVerified(verified: Boolean, pageable: Pageable): Page<UserEntity>
    
    /**
     * 检查邮箱是否存在
     */
    fun existsByEmail(email: String): Boolean
    
    /**
     * 检查昵称是否存在
     */
    fun existsByNickname(nickname: String): Boolean
    
    /**
     * 根据状态统计用户数量
     */
    fun countByStatus(status: String): Long
    
    /**
     * 根据创建时间范围统计用户数量
     */
    fun countByCreatedAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): Long
    
    /**
     * 统计指定时间范围内的活跃用户数量
     */
    fun countByLastLoginAtBetween(startDate: LocalDateTime, endDate: LocalDateTime): Long
    
    /**
     * 查找需要密码更新提醒的用户
     */
    @Query("""
        SELECT u FROM UserEntity u 
        WHERE u.status = 'ACTIVE' 
        AND u.lastPasswordChangeAt < :thresholdDate
        ORDER BY u.lastPasswordChangeAt ASC
    """)
    fun findUsersNeedingPasswordUpdate(
        @Param("thresholdDate") thresholdDate: LocalDateTime,
        pageable: Pageable
    ): Page<UserEntity>
    
    /**
     * 查找长期未登录的用户
     */
    @Query("""
        SELECT u FROM UserEntity u 
        WHERE u.status = 'ACTIVE' 
        AND (u.lastLoginAt IS NULL OR u.lastLoginAt < :thresholdDate)
        ORDER BY u.lastLoginAt ASC NULLS FIRST
    """)
    fun findInactiveUsers(
        @Param("thresholdDate") thresholdDate: LocalDateTime,
        pageable: Pageable
    ): Page<UserEntity>
    
    /**
     * 查找过期未验证的用户
     */
    @Query("""
        SELECT u FROM UserEntity u 
        WHERE u.emailVerified = false 
        AND u.status = 'PENDING' 
        AND u.createdAt < :thresholdDate
        ORDER BY u.createdAt ASC
    """)
    fun findExpiredUnverifiedUsers(
        @Param("thresholdDate") thresholdDate: LocalDateTime,
        pageable: Pageable
    ): Page<UserEntity>
    
    /**
     * 查找被锁定的用户
     */
    @Query("""
        SELECT u FROM UserEntity u 
        WHERE u.status = 'LOCKED' 
        OR (u.lockedUntil IS NOT NULL AND u.lockedUntil > :currentTime)
        ORDER BY u.lockedUntil DESC
    """)
    fun findLockedUsers(
        @Param("currentTime") currentTime: LocalDateTime,
        pageable: Pageable
    ): Page<UserEntity>
    
    /**
     * 查找有安全问题的用户
     */
    @Query("""
        SELECT u FROM UserEntity u 
        WHERE u.loginAttempts >= :maxAttempts 
        OR (u.lastPasswordChangeAt IS NOT NULL AND u.lastPasswordChangeAt < :passwordThreshold)
        OR (u.lastLoginAt IS NOT NULL AND u.lastLoginAt < :inactivityThreshold)
        ORDER BY u.loginAttempts DESC, u.lastPasswordChangeAt ASC
    """)
    fun findUsersWithSecurityIssues(
        @Param("maxAttempts") maxAttempts: Int,
        @Param("passwordThreshold") passwordThreshold: LocalDateTime,
        @Param("inactivityThreshold") inactivityThreshold: LocalDateTime,
        pageable: Pageable
    ): Page<UserEntity>
    
    /**
     * 根据邮箱验证令牌查找用户
     */
    fun findByEmailVerificationToken(token: String): UserEntity?
    
    /**
     * 根据密码重置令牌查找用户
     */
    fun findByPasswordResetToken(token: String): UserEntity?
    
    /**
     * 查找密码重置令牌已过期的用户
     */
    @Query("""
        SELECT u FROM UserEntity u 
        WHERE u.passwordResetToken IS NOT NULL 
        AND u.passwordResetTokenExpiresAt < :currentTime
    """)
    fun findUsersWithExpiredPasswordResetTokens(
        @Param("currentTime") currentTime: LocalDateTime
    ): List<UserEntity>
    
    /**
     * 批量更新用户状态
     */
    @Query("""
        UPDATE UserEntity u 
        SET u.status = :newStatus, u.updatedAt = :updateTime 
        WHERE u.id IN :userIds
    """)
    fun batchUpdateStatus(
        @Param("userIds") userIds: List<String>,
        @Param("newStatus") newStatus: String,
        @Param("updateTime") updateTime: LocalDateTime
    ): Int
    
    /**
     * 清理过期的邮箱验证令牌
     */
    @Query("""
        UPDATE UserEntity u 
        SET u.emailVerificationToken = NULL, u.updatedAt = :updateTime 
        WHERE u.emailVerificationToken IS NOT NULL 
        AND u.createdAt < :thresholdDate 
        AND u.emailVerified = false
    """)
    fun clearExpiredEmailVerificationTokens(
        @Param("thresholdDate") thresholdDate: LocalDateTime,
        @Param("updateTime") updateTime: LocalDateTime
    ): Int
    
    /**
     * 清理过期的密码重置令牌
     */
    @Query("""
        UPDATE UserEntity u 
        SET u.passwordResetToken = NULL, 
            u.passwordResetTokenExpiresAt = NULL, 
            u.updatedAt = :updateTime 
        WHERE u.passwordResetTokenExpiresAt < :currentTime
    """)
    fun clearExpiredPasswordResetTokens(
        @Param("currentTime") currentTime: LocalDateTime,
        @Param("updateTime") updateTime: LocalDateTime
    ): Int
    
    /**
     * 重置登录尝试次数
     */
    @Query("""
        UPDATE UserEntity u 
        SET u.loginAttempts = 0, u.updatedAt = :updateTime 
        WHERE u.id = :userId
    """)
    fun resetLoginAttempts(
        @Param("userId") userId: String,
        @Param("updateTime") updateTime: LocalDateTime
    ): Int
    
    /**
     * 解锁用户
     */
    @Query("""
        UPDATE UserEntity u 
        SET u.lockedUntil = NULL, 
            u.loginAttempts = 0, 
            u.status = 'ACTIVE', 
            u.updatedAt = :updateTime 
        WHERE u.id = :userId
    """)
    fun unlockUser(
        @Param("userId") userId: String,
        @Param("updateTime") updateTime: LocalDateTime
    ): Int

    /**
     * 检查邮箱在排除指定用户ID外是否存在
     */
    @Query("""
        select count(1) > 0 from UserEntity u 
        where u.nickname = :nickname and u.id <> :uid
    """)
    fun existsByNicknameAndIdNot(
        @Param("nickname") nickname: String,
        @Param("uid") uid: String
    ): Boolean
}