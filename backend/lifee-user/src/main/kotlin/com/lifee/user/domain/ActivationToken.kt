package com.lifee.user.domain

import com.lifee.common.exceptions.BusinessRuleException
import java.time.Instant
import java.util.*

/**
 * 激活令牌值对象
 */
data class ActivationToken(
    val value: String,
    val userId: UserId,
    val expiresAt: Instant,
    val createdAt: Instant = Instant.now()
) {
    
    companion object {
        private const val TOKEN_VALIDITY_HOURS = 24L
        
        /**
         * 生成新的激活令牌
         */
        fun generate(userId: UserId): ActivationToken {
            val token = UUID.randomUUID().toString().replace("-", "")
            val expiresAt = Instant.now().plusSeconds(TOKEN_VALIDITY_HOURS * 3600)
            
            return ActivationToken(
                value = token,
                userId = userId,
                expiresAt = expiresAt
            )
        }
        
        /**
         * 从字符串创建令牌
         */
        fun fromString(token: String, userId: UserId, expiresAt: Instant, createdAt: Instant): ActivationToken {
            BusinessRuleException.throwIf(
                token.isBlank(),
                "激活令牌不能为空"
            )
            
            return ActivationToken(
                value = token,
                userId = userId,
                expiresAt = expiresAt,
                createdAt = createdAt
            )
        }
    }
    
    /**
     * 检查令牌是否已过期
     */
    fun isExpired(): Boolean {
        return Instant.now().isAfter(expiresAt)
    }
    
    /**
     * 检查令牌是否有效
     */
    fun isValid(): Boolean {
        return !isExpired()
    }
    
    /**
     * 验证令牌是否属于指定用户
     */
    fun belongsTo(userId: UserId): Boolean {
        return this.userId == userId
    }
}