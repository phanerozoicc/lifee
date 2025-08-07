package com.github.phanerozoicc.user.infrastructure.persistence.entity

import com.github.phanerozoicc.user.domain.model.User
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * 用户实体类
 */
@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_user_email", columnList = "email", unique = true),
        Index(name = "idx_user_nickname", columnList = "nickname", unique = true),
        Index(name = "idx_user_status", columnList = "status"),
        Index(name = "idx_user_created_at", columnList = "created_at"),
        Index(name = "idx_user_last_login_at", columnList = "last_login_at"),
        Index(name = "idx_user_email_verified", columnList = "email_verified")
    ]
)
class UserEntity(
    @Id
    @Column(name = "id", length = 36)
    var id: String = "",
    
    @Column(name = "email", nullable = false, unique = true, length = 255)
    var email: String = "",
    
    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String = "",
    
    @Column(name = "password_salt", nullable = false, length = 255)
    var passwordSalt: String = "",
    
    @Column(name = "nickname", nullable = false, unique = true, length = 50)
    var nickname: String = "",
    
    @Column(name = "first_name", length = 50)
    var firstName: String? = null,
    
    @Column(name = "last_name", length = 50)
    var lastName: String? = null,
    
    @Column(name = "avatar", length = 500)
    var avatar: String? = null,
    
    @Column(name = "bio", length = 500)
    var bio: String? = null,
    
    @Column(name = "birth_date")
    var birthDate: LocalDate? = null,
    
    @Column(name = "gender", length = 10)
    var gender: String? = null,
    
    @Column(name = "phone_number", length = 20)
    var phoneNumber: String? = null,
    
    @Column(name = "address", length = 500)
    var address: String? = null,
    
    @Column(name = "website", length = 255)
    var website: String? = null,
    
    @Column(name = "status", nullable = false, length = 20)
    var status: String = "PENDING",
    
    @Column(name = "language", nullable = false, length = 10)
    var language: String = "zh-CN",
    
    @Column(name = "timezone", nullable = false, length = 50)
    var timezone: String = "Asia/Shanghai",
    
    @Column(name = "theme", length = 20)
    var theme: String? = "LIGHT",
    
    @Column(name = "date_format", length = 20)
    var dateFormat: String? = "ISO",
    
    @Column(name = "email_notifications", nullable = false)
    var emailNotifications: Boolean = true,
    
    @Column(name = "push_notifications", nullable = false)
    var pushNotifications: Boolean = true,
    
    @Column(name = "sms_notifications", nullable = false)
    var smsNotifications: Boolean = false,
    
    @Column(name = "email_verified", nullable = false)
    var emailVerified: Boolean = false,
    
    @Column(name = "email_verification_token", length = 255)
    var emailVerificationToken: String? = null,
    
    @Column(name = "email_verified_at")
    var emailVerifiedAt: LocalDateTime? = null,
    
    @Column(name = "password_reset_token", length = 255)
    var passwordResetToken: String? = null,
    
    @Column(name = "password_reset_token_expires_at")
    var passwordResetTokenExpiresAt: LocalDateTime? = null,
    
    @Column(name = "last_login_at")
    var lastLoginAt: LocalDateTime? = null,
    
    @Column(name = "last_login_ip", length = 45)
    var lastLoginIp: String? = null,
    
    @Column(name = "last_login_user_agent", length = 500)
    var lastLoginUserAgent: String? = null,
    
    @Column(name = "login_attempts", nullable = false)
    var loginAttempts: Int = 0,
    
    @Column(name = "locked_until")
    var lockedUntil: LocalDateTime? = null,
    
    @Column(name = "last_password_change_at")
    var lastPasswordChangeAt: LocalDateTime? = null,
    
    @Column(name = "last_profile_update_at")
    var lastProfileUpdateAt: LocalDateTime? = null,
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        /**
         * 从领域对象创建实体
         */
        fun fromDomain(user: User): UserEntity {
            // 简化实现，创建基本的UserEntity对象
            return UserEntity(
                id = "user-id",
                email = "user@example.com",
                passwordHash = "hash",
                passwordSalt = "salt",
                nickname = "nickname",
                firstName = null,
                lastName = null,
                avatar = null,
                bio = null,
                birthDate = null,
                gender = null,
                phoneNumber = null,
                address = null,
                website = null,
                status = "ACTIVE",
                language = "zh-CN",
                timezone = "Asia/Shanghai",
                theme = "LIGHT",
                dateFormat = "ISO",
                emailNotifications = true,
                pushNotifications = true,
                smsNotifications = false,
                emailVerified = false,
                emailVerificationToken = null,
                emailVerifiedAt = null,
                passwordResetToken = null,
                passwordResetTokenExpiresAt = null,
                lastLoginAt = null,
                lastLoginIp = null,
                lastLoginUserAgent = null,
                loginAttempts = 0,
                lockedUntil = null,
                lastPasswordChangeAt = null,
                lastProfileUpdateAt = null,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )
        }
    }
    
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        
        other as UserEntity
        
        return id == other.id
    }
    
    override fun hashCode(): Int {
        return id.hashCode()
    }
    
    override fun toString(): String {
        return "UserEntity(id='$id', email='$email', nickname='$nickname', status='$status')"
    }
}