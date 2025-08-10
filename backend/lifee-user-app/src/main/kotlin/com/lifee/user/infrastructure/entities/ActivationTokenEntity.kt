package com.lifee.user.infrastructure.entities

import com.lifee.user.domain.ActivationToken
import com.lifee.user.domain.UserId
import jakarta.persistence.*
import java.time.Instant

/**
 * 激活令牌JPA实体
 */
@Entity
@Table(name = "activation_tokens")
data class ActivationTokenEntity(
    @Id
    @Column(name = "token_value", length = 32)
    val tokenValue: String,
    
    @Column(name = "user_id", nullable = false, length = 36)
    val userId: String,
    
    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant,
    
    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
) {
    
    /**
     * 转换为领域对象
     */
    fun toDomain(): ActivationToken {
        return ActivationToken.fromString(
            token = tokenValue,
            userId = UserId.fromString(userId),
            expiresAt = expiresAt,
            createdAt = createdAt
        )
    }
    
    companion object {
        /**
         * 从领域对象创建实体
         */
        fun fromDomain(token: ActivationToken): ActivationTokenEntity {
            return ActivationTokenEntity(
                tokenValue = token.value,
                userId = token.userId.value.toString(),
                expiresAt = token.expiresAt,
                createdAt = token.createdAt
            )
        }
    }
}