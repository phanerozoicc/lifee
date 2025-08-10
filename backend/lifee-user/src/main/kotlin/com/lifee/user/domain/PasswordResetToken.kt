package com.lifee.user.domain

import com.lifee.common.exceptions.BusinessRuleException
import java.time.Instant
import java.util.*

/**
 * 密码重置令牌
 */
data class PasswordResetToken(
    val value: String,
    val userId: UserId,
    val expiresAt: Instant,
    val createdAt: Instant = Instant.now(),
    val usedAt: Instant? = null
) {
    companion object {
        private const val EXPIRY_HOURS = 1L // 1小时过期
        
        /**
         * 生成新的密码重置令牌
         */
        fun generate(userId: UserId): PasswordResetToken {
            val tokenValue = UUID.randomUUID().toString().replace("-", "")
            val expiresAt = Instant.now().plusSeconds(EXPIRY_HOURS * 3600)
            
            return PasswordResetToken(
                value = tokenValue,
                userId = userId,
                expiresAt = expiresAt
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
     * 检查令牌是否已使用
     */
    fun isUsed(): Boolean {
        return usedAt != null
    }
    
    /**
     * 标记令牌为已使用
     */
    fun markAsUsed(): PasswordResetToken {
        BusinessRuleException.throwIf(
            isUsed(),
            "密码重置令牌已被使用"
        )
        
        BusinessRuleException.throwIf(
            isExpired(),
            "密码重置令牌已过期"
        )
        
        return copy(usedAt = Instant.now())
    }
    
    /**
     * 验证令牌是否有效
     */
    fun isValid(): Boolean {
        return !isExpired() && !isUsed()
    }
}