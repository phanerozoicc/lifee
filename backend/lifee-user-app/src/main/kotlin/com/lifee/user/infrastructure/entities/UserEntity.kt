package com.lifee.user.infrastructure.entities

import com.lifee.user.domain.*
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.Instant

/**
 * 用户实体
 */
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener::class)
data class UserEntity(
    @Id
    @Column(name = "id", length = 36)
    val id: String,
    
    @Column(name = "email", unique = true, nullable = false, length = 255)
    val email: String,
    
    @Column(name = "password_hash", nullable = false, length = 255)
    val passwordHash: String,
    
    @Column(name = "first_name", nullable = false, length = 100)
    val firstName: String,
    
    @Column(name = "last_name", nullable = false, length = 100)
    val lastName: String,
    
    @Column(name = "date_of_birth")
    val dateOfBirth: LocalDate? = null,
    
    @Column(name = "phone_number", length = 20)
    val phoneNumber: String? = null,
    
    @Column(name = "avatar", length = 500)
    val avatar: String? = null,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    val status: UserStatus,
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime? = null,
    
    @LastModifiedDate
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime? = null,
    
    @Column(name = "activated_at")
    val activatedAt: LocalDateTime? = null,
    
    @Column(name = "last_login_at")
    val lastLoginAt: LocalDateTime? = null
) {
    
    /**
     * 转换为领域对象
     */
    fun toDomain(): User {
        val userId = UserId.fromString(id)
        val email = Email.of(email)
        val password = Password.fromHashedValue(passwordHash)
        val profile = UserProfile(
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dateOfBirth,
            phoneNumber = phoneNumber,
            avatar = avatar
        )
        
        return User(
            id = userId,
            email = email,
            password = password,
            profile = profile,
            status = status,
            emailVerified = status == UserStatus.ACTIVE,
            lastLoginAt = lastLoginAt?.toInstant(ZoneOffset.UTC),
            createdAt = (createdAt ?: LocalDateTime.now()).toInstant(ZoneOffset.UTC),
            updatedAt = (updatedAt ?: LocalDateTime.now()).toInstant(ZoneOffset.UTC),
            activatedAt = activatedAt?.toInstant(ZoneOffset.UTC)
        )
    }
    
    companion object {
        /**
         * 从领域对象创建实体
         */
        fun fromDomain(user: User): UserEntity {
            return UserEntity(
                id = user.getId().value.toString(),
                email = user.getEmail().value,
                passwordHash = user.getPassword().hashedValue,
                firstName = user.getProfile().firstName,
                lastName = user.getProfile().lastName,
                dateOfBirth = user.getProfile().dateOfBirth,
                phoneNumber = user.getProfile().phoneNumber,
                avatar = user.getProfile().avatar,
                status = user.getStatus(),
                createdAt = LocalDateTime.ofInstant(user.getCreatedAt(), ZoneOffset.UTC),
                updatedAt = LocalDateTime.ofInstant(user.getUpdatedAt(), ZoneOffset.UTC),
                activatedAt = user.getActivatedAt()?.let { LocalDateTime.ofInstant(it, ZoneOffset.UTC) },
                lastLoginAt = user.getLastLoginAt()?.let { LocalDateTime.ofInstant(it, ZoneOffset.UTC) }
            )
        }
    }
}