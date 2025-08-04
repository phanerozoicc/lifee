package com.github.phanerozoicc.user.domain.event

import com.github.phanerozoicc.domain.DomainEvent
import com.github.phanerozoicc.user.domain.model.*
import java.time.LocalDateTime
import java.time.Instant
import java.util.*

/**
 * 用户注册事件
 * 当新用户成功注册时发布
 */
data class UserRegistered(
    private val userId: UserId,
    private val email: Email,
    private val nickname: String,
    private val registrationTime: LocalDateTime = LocalDateTime.now(),
    private val ipAddress: String? = null,
    private val userAgent: String? = null,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserRegistered"
) : DomainEvent {
    
    fun getUserId(): UserId = userId
    fun getEmail(): Email = email
    fun getNickname(): String = nickname
    fun getRegistrationTime(): LocalDateTime = registrationTime
    fun getIpAddress(): String? = ipAddress
    fun getUserAgent(): String? = userAgent
}

/**
 * 用户登录事件
 * 当用户成功登录时发布
 */
data class UserLoggedIn(
    private val userId: UserId,
    private val email: Email,
    private val loginTime: LocalDateTime = LocalDateTime.now(),
    private val ipAddress: String? = null,
    private val userAgent: String? = null,
    private val sessionId: String? = null,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserLoggedIn"
) : DomainEvent {
    
    fun getUserId(): UserId = userId
    fun getEmail(): Email = email
    fun getLoginTime(): LocalDateTime = loginTime
    fun getIpAddress(): String? = ipAddress
    fun getUserAgent(): String? = userAgent
    fun getSessionId(): String? = sessionId
}

/**
 * 用户资料更新事件
 * 当用户资料信息发生变更时发布
 */
data class UserProfileUpdated(
    private val userId: UserId,
    private val oldProfile: UserProfile,
    private val newProfile: UserProfile,
    private val changedFields: Set<String>,
    private val updatedBy: UserId? = null, // 如果是管理员更新，记录操作者
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserProfileUpdated"
) : DomainEvent {
    
    fun getUserId(): UserId = userId
    fun getOldProfile(): UserProfile = oldProfile
    fun getNewProfile(): UserProfile = newProfile
    fun getChangedFields(): Set<String> = changedFields
    fun getUpdatedBy(): UserId? = updatedBy
    
    /**
     * 检查特定字段是否发生变更
     */
    fun isFieldChanged(fieldName: String): Boolean {
        return changedFields.contains(fieldName)
    }
}

/**
 * 用户状态变更事件
 * 当用户状态发生变更时发布
 */
data class UserStatusChanged(
    private val userId: UserId,
    private val oldStatus: UserStatus,
    private val newStatus: UserStatus,
    private val reason: String? = null,
    private val changedBy: UserId? = null, // 操作者ID
    private val ipAddress: String? = null,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserStatusChanged"
) : DomainEvent {
    
    fun getUserId(): UserId = userId
    fun getOldStatus(): UserStatus = oldStatus
    fun getNewStatus(): UserStatus = newStatus
    fun getReason(): String? = reason
    fun getChangedBy(): UserId? = changedBy
    fun getIpAddress(): String? = ipAddress
    
    /**
     * 检查是否为激活操作
     */
    fun isActivation(): Boolean {
        return newStatus.isActive() && !oldStatus.isActive()
    }
    
    /**
     * 检查是否为停用操作
     */
    fun isDeactivation(): Boolean {
        return !newStatus.isActive() && oldStatus.isActive()
    }
    
    /**
     * 检查是否为锁定操作
     */
    fun isLocking(): Boolean {
        return newStatus.isLocked() && !oldStatus.isLocked()
    }
    
    /**
     * 检查是否为解锁操作
     */
    fun isUnlocking(): Boolean {
        return !newStatus.isLocked() && oldStatus.isLocked()
    }
}

/**
 * 密码变更事件
 * 当用户密码发生变更时发布
 */
data class PasswordChanged(
    private val userId: UserId,
    private val changeTime: LocalDateTime = LocalDateTime.now(),
    private val ipAddress: String? = null,
    private val isAdminReset: Boolean = false,
    private val resetBy: UserId? = null, // 如果是管理员重置，记录操作者
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "PasswordChanged"
) : DomainEvent {
    
    fun getUserId(): UserId = userId
    fun getChangeTime(): LocalDateTime = changeTime
    fun getIpAddress(): String? = ipAddress
    fun isAdminReset(): Boolean = isAdminReset
    fun getResetBy(): UserId? = resetBy
}

/**
 * 用户偏好设置更新事件
 * 当用户偏好设置发生变更时发布
 */
data class UserPreferencesUpdated(
    private val userId: UserId,
    private val oldPreferences: UserPreferences,
    private val newPreferences: UserPreferences,
    private val changedSettings: Set<String>,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserPreferencesUpdated"
) : DomainEvent {
    
    fun getUserId(): UserId = userId
    fun getOldPreferences(): UserPreferences = oldPreferences
    fun getNewPreferences(): UserPreferences = newPreferences
    fun getChangedSettings(): Set<String> = changedSettings
    
    /**
     * 检查特定设置是否发生变更
     */
    fun isSettingChanged(settingName: String): Boolean {
        return changedSettings.contains(settingName)
    }
}

/**
 * 用户邮箱验证事件
 * 当用户邮箱验证状态发生变更时发布
 */
data class UserEmailVerified(
    private val userId: UserId,
    private val email: Email,
    private val verificationTime: LocalDateTime = LocalDateTime.now(),
    private val verificationToken: String? = null,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserEmailVerified"
) : DomainEvent {
    
    fun getUserId(): UserId = userId
    fun getEmail(): Email = email
    fun getVerificationTime(): LocalDateTime = verificationTime
    fun getVerificationToken(): String? = verificationToken
}

/**
 * 用户删除事件
 * 当用户账户被删除时发布
 */
data class UserDeleted(
    private val userId: UserId,
    private val email: Email,
    private val deletionTime: LocalDateTime = LocalDateTime.now(),
    private val reason: String? = null,
    private val deletedBy: UserId? = null, // 操作者ID
    private val isHardDelete: Boolean = false, // 是否为硬删除
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserDeleted"
) : DomainEvent {
    
    fun getUserId(): UserId = userId
    fun getEmail(): Email = email
    fun getDeletionTime(): LocalDateTime = deletionTime
    fun getReason(): String? = reason
    fun getDeletedBy(): UserId? = deletedBy
    fun isHardDelete(): Boolean = isHardDelete
}

/**
 * 用户登录失败事件
 * 当用户登录失败时发布（用于安全监控）
 */
data class UserLoginFailed(
    private val email: Email,
    private val failureReason: String,
    private val attemptTime: LocalDateTime = LocalDateTime.now(),
    private val ipAddress: String? = null,
    private val userAgent: String? = null,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "UserLoginFailed"
) : DomainEvent {
    
    fun getEmail(): Email = email
    fun getFailureReason(): String = failureReason
    fun getAttemptTime(): LocalDateTime = attemptTime
    fun getIpAddress(): String? = ipAddress
    fun getUserAgent(): String? = userAgent
}