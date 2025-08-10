package com.lifee.user.infrastructure.entities

import com.lifee.user.domain.*
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * 用户登录日志JPA实体
 */
@Entity
@Table(name = "user_login_logs")
@EntityListeners(AuditingEntityListener::class)
data class UserLoginLogEntity(
    @Id
    @Column(name = "id")
    val id: String,
    
    @Column(name = "user_id", nullable = false)
    val userId: String,
    
    @Column(name = "email", nullable = false)
    val email: String,
    
    @Column(name = "ip_address")
    val ipAddress: String? = null,
    
    @Column(name = "user_agent", length = 1000)
    val userAgent: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "login_result", nullable = false)
    val loginResult: LoginResult,
    
    @Column(name = "failure_reason", length = 500)
    val failureReason: String? = null,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    
    /**
     * 转换为领域对象
     */
    fun toDomain(): UserLoginLog {
        return UserLoginLog(
            id = UserLoginLogId.of(id),
            userId = UserId.fromString(userId),
            email = email,
            ipAddress = ipAddress,
            userAgent = userAgent,
            loginResult = loginResult,
            failureReason = failureReason,
            createdAt = createdAt.toInstant(ZoneOffset.UTC)
        )
    }
    
    companion object {
        /**
         * 从领域对象创建实体
         */
        fun fromDomain(loginLog: UserLoginLog): UserLoginLogEntity {
            return UserLoginLogEntity(
                id = loginLog.getId().value,
                userId = loginLog.getUserId().value.toString(),
                email = loginLog.getEmail(),
                ipAddress = loginLog.getIpAddress(),
                userAgent = loginLog.getUserAgent(),
                loginResult = loginLog.getLoginResult(),
                failureReason = loginLog.getFailureReason(),
                createdAt = LocalDateTime.ofInstant(loginLog.getCreatedAt(), ZoneOffset.UTC)
            )
        }
    }
}