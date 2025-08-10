package com.lifee.user.domain

/**
 * 密码重置令牌仓储接口
 */
interface PasswordResetTokenRepository {
    
    /**
     * 保存密码重置令牌
     */
    suspend fun save(token: PasswordResetToken): PasswordResetToken
    
    /**
     * 根据令牌值查找
     */
    suspend fun findByToken(tokenValue: String): PasswordResetToken?
    
    /**
     * 根据用户ID查找有效的令牌
     */
    suspend fun findValidTokenByUserId(userId: UserId): PasswordResetToken?
    
    /**
     * 删除令牌
     */
    suspend fun delete(token: PasswordResetToken)
    
    /**
     * 删除用户的所有密码重置令牌
     */
    suspend fun deleteAllByUserId(userId: UserId)
    
    /**
     * 删除过期的令牌
     */
    suspend fun deleteExpiredTokens()
    
    /**
     * 检查用户是否有有效的密码重置令牌
     */
    suspend fun hasValidTokenForUser(userId: UserId): Boolean
}