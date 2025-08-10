package com.lifee.user.infrastructure.repositories

import com.lifee.user.infrastructure.entities.PasswordResetTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * JPA密码重置令牌仓储接口
 */
@Repository
interface JpaPasswordResetTokenRepository : JpaRepository<PasswordResetTokenEntity, String> {
    
    /**
     * 根据令牌值查找
     */
    fun findByTokenValue(tokenValue: String): PasswordResetTokenEntity?
    
    /**
     * 根据用户ID查找有效的令牌（未使用且未过期）
     */
    @Query("""
        SELECT t FROM PasswordResetTokenEntity t 
        WHERE t.userId = :userId 
        AND t.usedAt IS NULL 
        AND t.expiresAt > :now
        ORDER BY t.createdAt DESC
    """)
    fun findValidTokenByUserId(
        @Param("userId") userId: String,
        @Param("now") now: LocalDateTime
    ): PasswordResetTokenEntity?
    
    /**
     * 根据用户ID删除所有令牌
     */
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.userId = :userId")
    fun deleteAllByUserId(@Param("userId") userId: String)
    
    /**
     * 删除过期的令牌
     */
    @Modifying
    @Query("DELETE FROM PasswordResetTokenEntity t WHERE t.expiresAt < :now")
    fun deleteExpiredTokens(@Param("now") now: LocalDateTime)
    
    /**
     * 检查用户是否有有效的密码重置令牌
     */
    @Query("""
        SELECT COUNT(t) > 0 FROM PasswordResetTokenEntity t 
        WHERE t.userId = :userId 
        AND t.usedAt IS NULL 
        AND t.expiresAt > :now
    """)
    fun hasValidTokenForUser(
        @Param("userId") userId: String,
        @Param("now") now: LocalDateTime
    ): Boolean
}