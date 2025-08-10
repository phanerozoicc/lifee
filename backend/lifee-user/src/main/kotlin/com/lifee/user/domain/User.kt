package com.lifee.user.domain

import com.lifee.common.domain.AggregateRoot
import com.lifee.common.exceptions.BusinessRuleException
import com.lifee.user.domain.events.*
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
) : AggregateRoot<UserId>(id) {
    
    companion object {
        /**
         * 创建新用户
         */
        fun create(
            email: Email,
            password: Password,
            firstName: String,
            lastName: String
        ): User {
            val userId = UserId.generate()
            val profile = UserProfile.create(firstName, lastName)
            
            val user = User(
                id = userId,
                email = email,
                password = password,
                profile = profile
            )
            
            // 发布用户注册事件
            user.addDomainEvent(
                UserRegisteredEvent(
                    userId = userId,
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
}