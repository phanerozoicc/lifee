package com.lifee.user.infrastructure.entities

import com.lifee.user.domain.PasswordResetToken
import com.lifee.user.domain.UserId
import jakarta.persistence.*
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * 密码重置令牌实体
 */
@Entity
@Table(name = "password_reset_tokens")
data class PasswordResetTokenEntity(
    @Id
    @Column(name = "token_value", length = 32)
    val tokenValue: String,
    
    @Column(name = "user_id", nullable = false, length = 36)
    val userId: String,
    
    @Column(name = "expires_at", nullable = false)
    val expiresAt: LocalDateTime,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime,
    
    @Column(name = "used_at")
    val usedAt: LocalDateTime? = null
) {
    
    /**
     * 转换为领域对象
     */
    fun toDomain(): PasswordResetToken {
        return PasswordResetToken(
            value = tokenValue,
            userId = UserId.fromString(userId),
            expiresAt = expiresAt.toInstant(ZoneOffset.UTC),
            createdAt = createdAt.toInstant(ZoneOffset.UTC),
            usedAt = usedAt?.toInstant(ZoneOffset.UTC)
        )
    }
    
    companion object {
        /**
         * 从领域对象创建实体
         */
        fun fromDomain(token: PasswordResetToken): PasswordResetTokenEntity {
            return PasswordResetTokenEntity(
                tokenValue = token.value,
                userId = token.userId.value.toString(),
                expiresAt = LocalDateTime.ofInstant(token.expiresAt, ZoneOffset.UTC),
                createdAt = LocalDateTime.ofInstant(token.createdAt, ZoneOffset.UTC),
                usedAt = token.usedAt?.let { LocalDateTime.ofInstant(it, ZoneOffset.UTC) }
            )
        }
    }
}