package com.lifee.user.domain

/**
 * 激活令牌仓储接口
 */
interface ActivationTokenRepository {
    
    /**
     * 保存激活令牌
     */
    suspend fun save(token: ActivationToken): ActivationToken
    
    /**
     * 根据令牌值查找
     */
    suspend fun findByToken(tokenValue: String): ActivationToken?
    
    /**
     * 根据用户ID查找有效令牌
     */
    suspend fun findValidTokenByUserId(userId: UserId): ActivationToken?
    
    /**
     * 删除令牌
     */
    suspend fun delete(token: ActivationToken)
    
    /**
     * 删除用户的所有令牌
     */
    suspend fun deleteByUserId(userId: UserId)
    
    /**
     * 删除过期令牌
     */
    suspend fun deleteExpiredTokens()
}