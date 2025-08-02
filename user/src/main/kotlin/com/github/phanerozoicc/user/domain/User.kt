package com.github.phanerozoicc.user.domain

import com.github.phanerozoicc.domain.AggregateRoot
import com.github.phanerozoicc.domain.DomainEvent
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

/**
 * 用户聚合根
 */
class User private constructor(
    id: UserId,
    private var username: Username,
    private var email: Email,
    private var passwordHash: PasswordHash,
    private var profile: UserProfile,
    private var status: UserStatus,
    private val createdAt: LocalDateTime,
    private var updatedAt: LocalDateTime
) : AggregateRoot<UserId>(id) {

    companion object {
        fun create(
            username: Username,
            email: Email,
            passwordHash: PasswordHash,
            profile: UserProfile
        ): User {
            val userId = UserId.generate()
            val now = LocalDateTime.now()
            
            val user = User(
                id = userId,
                username = username,
                email = email,
                passwordHash = passwordHash,
                profile = profile,
                status = UserStatus.ACTIVE,
                createdAt = now,
                updatedAt = now
            )
            
            user.addDomainEvent(UserCreatedEvent(userId, username, email))
            return user
        }
    }

    fun updateProfile(newProfile: UserProfile) {
        if (this.profile != newProfile) {
            this.profile = newProfile
            this.updatedAt = LocalDateTime.now()
            addDomainEvent(UserProfileUpdatedEvent(id, newProfile))
        }
    }

    fun changePassword(newPasswordHash: PasswordHash) {
        this.passwordHash = newPasswordHash
        this.updatedAt = LocalDateTime.now()
        addDomainEvent(UserPasswordChangedEvent(id))
    }

    fun activate() {
        if (status != UserStatus.ACTIVE) {
            this.status = UserStatus.ACTIVE
            this.updatedAt = LocalDateTime.now()
            addDomainEvent(UserActivatedEvent(id))
        }
    }

    fun deactivate() {
        if (status != UserStatus.INACTIVE) {
            this.status = UserStatus.INACTIVE
            this.updatedAt = LocalDateTime.now()
            addDomainEvent(UserDeactivatedEvent(id))
        }
    }

    fun verifyPassword(passwordHash: PasswordHash): Boolean {
        return this.passwordHash == passwordHash
    }

    // Getters
    fun getUsername(): Username = username
    fun getEmail(): Email = email
    fun getPasswordHash(): PasswordHash = passwordHash
    fun getProfile(): UserProfile = profile
    fun getStatus(): UserStatus = status
    fun getCreatedAt(): LocalDateTime = createdAt
    fun getUpdatedAt(): LocalDateTime = updatedAt
    
    fun isActive(): Boolean = status == UserStatus.ACTIVE
}

/**
 * 用户ID值对象
 */
data class UserId(val value: UUID) {
    companion object {
        fun generate(): UserId = UserId(UUID.randomUUID())
        fun from(value: String): UserId = UserId(UUID.fromString(value))
    }
    
    override fun toString(): String = value.toString()
}

/**
 * 用户名值对象
 */
data class Username(val value: String) {
    init {
        require(value.isNotBlank()) { "Username cannot be blank" }
        require(value.length >= 3) { "Username must be at least 3 characters" }
        require(value.length <= 50) { "Username must be at most 50 characters" }
        require(value.matches(Regex("^[a-zA-Z0-9_]+$"))) { "Username can only contain letters, numbers and underscores" }
    }
}

/**
 * 邮箱值对象
 */
data class Email(val value: String) {
    init {
        require(value.isNotBlank()) { "Email cannot be blank" }
        require(value.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"))) { "Invalid email format" }
    }
}

/**
 * 密码哈希值对象
 */
data class PasswordHash(val value: String) {
    init {
        require(value.isNotBlank()) { "Password hash cannot be blank" }
    }
}

/**
 * 用户资料值对象
 */
data class UserProfile(
    val displayName: String,
    val avatar: String? = null,
    val bio: String? = null
) {
    init {
        require(displayName.isNotBlank()) { "Display name cannot be blank" }
        require(displayName.length <= 100) { "Display name must be at most 100 characters" }
        bio?.let {
            require(it.length <= 500) { "Bio must be at most 500 characters" }
        }
    }
}

/**
 * 用户状态枚举
 */
enum class UserStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}

// 领域事件
data class UserCreatedEvent(
    val userId: UserId,
    val username: Username,
    val email: Email,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserCreated"
) : DomainEvent

data class UserProfileUpdatedEvent(
    val userId: UserId,
    val profile: UserProfile,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserProfileUpdated"
) : DomainEvent

data class UserPasswordChangedEvent(
    val userId: UserId,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserPasswordChanged"
) : DomainEvent

data class UserActivatedEvent(
    val userId: UserId,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserActivated"
) : DomainEvent

data class UserDeactivatedEvent(
    val userId: UserId,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserDeactivated"
) : DomainEvent