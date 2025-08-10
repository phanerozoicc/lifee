package com.lifee.user.domain.services

import com.lifee.user.domain.User
import java.time.Instant

/**
 * JWT服务接口
 */
interface JwtService {
    
    /**
 * 生成访问令牌
     */
    fun generateAccessToken(user: User): String
    
    /**
     * 生成刷新令牌
     */
    fun generateRefreshToken(user: User): String
    
    /**
     * 验证令牌
     */
    fun validateToken(token: String): Boolean
    
    /**
     * 从令牌中提取用户ID
     */
    fun extractUserId(token: String): String?
    
    /**
     * 从令牌中提取邮箱
     */
    fun extractEmail(token: String): String?
    
    /**
     * 获取令牌过期时间
     */
    fun getTokenExpiration(token: String): Instant?
    
    /**
     * 获取访问令牌过期时间（秒）
     */
    fun getAccessTokenExpirationSeconds(): Long
}