package com.lifee.user.domain

import com.lifee.common.domain.EventSourcedAggregateRoot
import com.lifee.common.exceptions.BusinessRuleException
import com.lifee.user.domain.events.*
import com.lifee.user.domain.services.UserIdGenerator
import java.time.Instant

/**
 * 用户聚合根
 */
class User(
    id: UserId,
    private var email: Email,
    private var password: Password,
    private var profile: UserProfile,
    private var status: UserStatus = UserStatus.PENDING_ACTIVATION,
    private var emailVerified: Boolean = false,
    private var lastLoginAt: Instant? = null,
    private var createdAt: Instant = Instant.now(),
    private var updatedAt: Instant = Instant.now(),
    private var activatedAt: Instant? = null
) : EventSourcedAggregateRoot<UserId>(id) {
    
    companion object {
        /**
         * 创建新用户（指定ID）
         */
        fun create(
            id: UserId,
            email: Email,
            password: Password,
            firstName: String,
            lastName: String
        ): User {
            val profile = UserProfile.create(firstName, lastName)

            val user = User(
                id = id,
                email = email,
                password = password,
                profile = profile
            )
            
            // 发布用户注册事件
            user.addDomainEvent(
                UserRegisteredEvent(
                    userId = id,
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    registeredAt = user.createdAt
                )
            )
            
            return user
        }
    }
    
    // Getters
    fun getEmail(): Email = email
    fun getPassword(): Password = password
    fun getProfile(): UserProfile = profile
    fun getStatus(): UserStatus = status
    fun isEmailVerified(): Boolean = emailVerified
    fun getLastLoginAt(): Instant? = lastLoginAt
    fun getCreatedAt(): Instant = createdAt
    fun getUpdatedAt(): Instant = updatedAt
    fun getActivatedAt(): Instant? = activatedAt
    fun isActivated(): Boolean = status == UserStatus.ACTIVE && activatedAt != null
    
    /**
     * 激活用户
     */
    fun activate() {
        BusinessRuleException.throwIf(
            status != UserStatus.PENDING_ACTIVATION,
            "只有待激活状态的用户才能被激活"
        )
        
        val oldStatus = status
        val now = Instant.now()
        status = UserStatus.ACTIVE
        emailVerified = true
        activatedAt = now
        updatedAt = now
        
        addDomainEvent(
            UserActivatedEvent(getId())
        )
        
        addDomainEvent(
            UserStatusChangedEvent(
                userId = getId(),
                oldStatus = oldStatus,
                newStatus = status
            )
        )
    }
    
    /**
     * 停用用户
     */
    fun suspend(reason: String? = null) {
        BusinessRuleException.throwIf(
            status == UserStatus.DELETED,
            "已删除的用户不能被停用"
        )
        
        val oldStatus = status
        status = UserStatus.SUSPENDED
        updatedAt = Instant.now()
        
        addDomainEvent(
            UserStatusChangedEvent(
                userId = getId(),
                oldStatus = oldStatus,
                newStatus = status,
                reason = reason
            )
        )
    }
    
    /**
     * 恢复用户
     */
    fun reactivate() {
        BusinessRuleException.throwIf(
            status != UserStatus.SUSPENDED,
            "只有被停用的用户才能被恢复"
        )
        
        val oldStatus = status
        status = UserStatus.ACTIVE
        updatedAt = Instant.now()
        
        addDomainEvent(
            UserStatusChangedEvent(
                userId = getId(),
                oldStatus = oldStatus,
                newStatus = status
            )
        )
    }
    
    /**
     * 软删除用户
     */
    fun delete(reason: String? = null) {
        BusinessRuleException.throwIf(
            status == UserStatus.DELETED,
            "用户已经被删除"
        )
        
        val oldStatus = status
        status = UserStatus.DELETED
        updatedAt = Instant.now()
        
        addDomainEvent(
            UserStatusChangedEvent(
                userId = getId(),
                oldStatus = oldStatus,
                newStatus = status,
                reason = reason
            )
        )
    }
    
    /**
     * 更新用户档案
     */
    fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        dateOfBirth: java.time.LocalDate? = null,
        phoneNumber: String? = null,
        avatar: String? = null
    ) {
        BusinessRuleException.throwIf(
            status == UserStatus.DELETED,
            "已删除的用户不能更新档案"
        )
        
        val oldProfile = profile
        profile = profile.updateProfile(
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = dateOfBirth,
            phoneNumber = phoneNumber,
            avatar = avatar
        )
        updatedAt = Instant.now()
        
        addDomainEvent(
            UserProfileUpdatedEvent(
                userId = getId(),
                oldProfile = oldProfile,
                newProfile = profile
            )
        )
    }
    
    /**
     * 更改密码
     */
    fun changePassword(newPassword: Password) {
        BusinessRuleException.throwIf(
            status == UserStatus.DELETED,
            "已删除的用户不能更改密码"
        )
        
        BusinessRuleException.throwIf(
            password == newPassword,
            "新密码不能与当前密码相同"
        )
        
        password = newPassword
        updatedAt = Instant.now()
        
        addDomainEvent(
            UserPasswordChangedEvent(getId())
        )
    }
    
    /**
     * 验证密码
     */
    fun verifyPassword(plainPassword: String): Boolean {
        BusinessRuleException.throwIf(
            !status.canLogin(),
            "用户状态不允许登录"
        )
        
        return password.matches(plainPassword)
    }
    
    /**
     * 记录登录时间
     */
    fun recordLogin() {
        BusinessRuleException.throwIf(
            !status.canLogin(),
            "用户状态不允许登录"
        )
        
        lastLoginAt = Instant.now()
        updatedAt = Instant.now()
    }
    
    /**
     * 检查用户是否可以执行操作
     */
    fun canPerformAction(): Boolean {
        return status.isActive()
    }
    
    /**
     * 获取用户显示名称
     */
    fun getDisplayName(): String {
        return profile.getFullName()
    }
    
    override fun toString(): String {
        return "User(id=${getId()}, email=$email, status=$status, emailVerified=$emailVerified)"
    }
    
    /**
     * 序列化聚合根状态
     */
    override fun serializeState(): Map<String, Any> {
        return mapOf(
            "id" to getId().toString(),
            "email" to email.toString(),
            "password" to password.toString(),
            "profile" to mapOf(
                "firstName" to profile.firstName,
                "lastName" to profile.lastName,
                "fullName" to profile.getFullName(),
                "dateOfBirth" to (profile.dateOfBirth?.toString() ?: ""),
                "phoneNumber" to (profile.phoneNumber ?: ""),
                "avatar" to (profile.avatar ?: "")
            ),
            "status" to status.name,
            "emailVerified" to emailVerified,
            "lastLoginAt" to (lastLoginAt?.toString() ?: ""),
            "createdAt" to createdAt.toString(),
            "updatedAt" to updatedAt.toString(),
            "activatedAt" to (activatedAt?.toString() ?: "")
        )
    }
    
    /**
     * 反序列化聚合根状态
     */
    override fun deserializeState(stateData: Map<String, Any>) {
        try {
            // 恢复基本信息
            email = Email(stateData["email"] as String)
            password = Password(stateData["password"] as String)
            status = UserStatus.valueOf(stateData["status"] as String)
            emailVerified = stateData["emailVerified"] as Boolean
            
            // 恢复用户档案
            @Suppress("UNCHECKED_CAST")
            val profileData = stateData["profile"] as Map<String, Any>
            profile = UserProfile.create(
                firstName = profileData["firstName"] as String,
                lastName = profileData["lastName"] as String,
                dateOfBirth = (profileData["dateOfBirth"] as? String)?.takeIf { it.isNotEmpty() }?.let { java.time.LocalDate.parse(it) },
                phoneNumber = (profileData["phoneNumber"] as? String)?.takeIf { it.isNotEmpty() },
                avatar = (profileData["avatar"] as? String)?.takeIf { it.isNotEmpty() }
            )
            
            // 恢复时间戳
            val lastLoginAtStr = stateData["lastLoginAt"] as? String
            lastLoginAt = lastLoginAtStr?.takeIf { it.isNotEmpty() }?.let { Instant.parse(it) }
            
            val createdAtStr = stateData["createdAt"] as? String
            if (createdAtStr != null) {
                createdAt = Instant.parse(createdAtStr)
            }
            
            val updatedAtStr = stateData["updatedAt"] as? String
            if (updatedAtStr != null) {
                updatedAt = Instant.parse(updatedAtStr)
            }
            
            val activatedAtStr = stateData["activatedAt"] as? String
            activatedAt = activatedAtStr?.takeIf { it.isNotEmpty() }?.let { Instant.parse(it) }
            
        } catch (e: Exception) {
            // 在实际应用中需要更严格的错误处理
            throw IllegalStateException("Failed to deserialize User state", e)
        }
    }
    
    /**
     * 应用领域事件到聚合根
     */
    override fun applyEvent(event: com.lifee.common.domain.DomainEvent) {
        when (event) {
            is UserRegisteredEvent -> {
                // 用户注册事件已在构造函数中处理
            }
            is UserActivatedEvent -> {
                // 用户激活事件已在activate方法中处理
            }
            is UserStatusChangedEvent -> {
                // 用户状态变更事件已在相应方法中处理
            }
            is UserProfileUpdatedEvent -> {
                // 用户档案更新事件已在updateProfile方法中处理
            }
            is UserPasswordChangedEvent -> {
                // 密码变更事件已在changePassword方法中处理
            }
            is UserLoginSuccessEvent -> {
                // 登录成功事件已在recordLogin方法中处理
            }
            is UserLoginFailedEvent -> {
                // 登录失败事件处理（如果需要的话）
            }
            // 可以根据需要添加更多事件处理
        }
    }
}