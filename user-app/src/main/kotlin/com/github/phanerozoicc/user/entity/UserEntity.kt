package com.github.phanerozoicc.infrastructure.persistence.entity

import com.github.phanerozoicc.user.domain.*
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime

/**
 * 用户实体
 */
@Entity
@Table(
    name = "users",
    indexes = [
        Index(name = "idx_username", columnList = "username", unique = true),
        Index(name = "idx_email", columnList = "email", unique = true),
        Index(name = "idx_status", columnList = "status"),
        Index(name = "idx_created_at", columnList = "created_at")
    ]
)
class UserEntity {
    
    @Id
    @Column(name = "id", length = 36)
    var id: String = ""
    
    @Column(name = "username", length = 50, nullable = false, unique = true)
    var username: String = ""
    
    @Column(name = "email", length = 100, nullable = false, unique = true)
    var email: String = ""
    
    @Column(name = "password_hash", length = 255, nullable = false)
    var passwordHash: String = ""
    
    @Column(name = "display_name", length = 100, nullable = false)
    var displayName: String = ""
    
    @Column(name = "avatar", length = 500)
    var avatar: String? = null
    
    @Column(name = "bio", length = 500)
    var bio: String? = null
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    var status: UserStatus = UserStatus.INACTIVE
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
    
    @Version
    @Column(name = "version")
    var version: Long = 0

    /**
     * 转换为领域对象
     */
    fun toDomain(): User {
        val userId = UserId(id)
        val username = Username(username)
        val email = Email(email)
        val passwordHash = PasswordHash(passwordHash)
        val profile = UserProfile(
            displayName = displayName,
            avatar = avatar,
            bio = bio
        )
        
        return User.reconstruct(
            id = userId,
            username = username,
            email = email,
            passwordHash = passwordHash,
            profile = profile,
            status = status,
            createdAt = createdAt,
            updatedAt = updatedAt,
            version = version
        )
    }

    companion object {
        /**
         * 从领域对象创建实体
         */
        fun fromDomain(user: User): UserEntity {
            val entity = UserEntity()
            entity.id = user.id.toString()
            entity.username = user.getUsername().value
            entity.email = user.getEmail().value
            entity.passwordHash = user.getPasswordHash().value
            entity.displayName = user.getProfile().displayName
            entity.avatar = user.getProfile().avatar
            entity.bio = user.getProfile().bio
            entity.status = user.getStatus()
            entity.createdAt = user.getCreatedAt()
            entity.updatedAt = user.getUpdatedAt()
            entity.version = user.getVersion()
            return entity
        }
    }
}