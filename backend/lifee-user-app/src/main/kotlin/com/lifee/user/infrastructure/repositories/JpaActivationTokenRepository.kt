package com.lifee.user.infrastructure.repositories

import com.lifee.user.infrastructure.entities.ActivationTokenEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

/**
 * JPA激活令牌仓储接口
 */
@Repository
interface JpaActivationTokenRepository : JpaRepository<ActivationTokenEntity, String> {
    
    /**
     * 根据令牌值查找
     */
    fun findByTokenValue(tokenValue: String): ActivationTokenEntity?
    
    /**
     * 根据用户ID查找有效令牌
     */
    @Query("""
        SELECT t FROM ActivationTokenEntity t 
        WHERE t.userId = :userId 
        AND t.expiresAt > :now 
        ORDER BY t.createdAt DESC
    """)
    fun findValidTokenByUserId(
        @Param("userId") userId: String,
        @Param("now") now: Instant = Instant.now()
    ): ActivationTokenEntity?
    
    /**
     * 删除用户的所有令牌
     */
    @Modifying
    @Query("DELETE FROM ActivationTokenEntity t WHERE t.userId = :userId")
    fun deleteByUserId(@Param("userId") userId: String)
    
    /**
     * 删除过期令牌
     */
    @Modifying
    @Query("DELETE FROM ActivationTokenEntity t WHERE t.expiresAt <= :now")
    fun deleteExpiredTokens(@Param("now") now: Instant = Instant.now())
}