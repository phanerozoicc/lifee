package com.github.phanerozoicc.user.domain.model

import com.github.phanerozoicc.base.domain.EventSourcedAggregateRoot
import com.github.phanerozoicc.base.event.DomainEvent
import com.github.phanerozoicc.base.exception.BusinessRuleException
import com.github.phanerozoicc.user.application.event.*
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

/**
 * 用户聚合根
 * 封装用户相关的业务逻辑和不变性约束
 */
class User(
    id: UserId,
    private var email: Email,
    private var password: Password,
    private var profile: UserProfile,
    private var status: UserStatus = UserStatus.pending(),
    private var preferences: UserPreferences = UserPreferences.default(),
    private var emailVerified: Boolean = false,
    private var createdAt: LocalDateTime = LocalDateTime.now(),
    private var updatedAt: LocalDateTime = LocalDateTime.now(),
    private var lastLoginAt: LocalDateTime? = null,
    private var loginAttempts: Int = 0,
    private var lastFailedLoginAt: LocalDateTime? = null
) : EventSourcedAggregateRoot<UserId>(id) {
    
    companion object {
        private val passwordSpecification = PasswordSpecification()
        private val userSpecification = UserSpecification()

    }

    /**
     * 用户登录
     * @param plainPassword 明文密码
     * @param ipAddress 登录IP地址
     * @param userAgent 用户代理
     * @param sessionId 会话ID
     * @throws IllegalStateException 如果用户状态不允许登录
     * @throws IllegalArgumentException 如果密码不正确
     */
    fun login(
        plainPassword: String,
        ipAddress: String? = null,
        userAgent: String? = null,
        sessionId: String? = null
    ) {
        // 检查用户状态
        require(status.canLogin()) { "用户状态不允许登录: ${status.getDisplayName()}" }
        
        // 检查登录尝试次数
        require(userSpecification.canAttemptLogin(loginAttempts, lastFailedLoginAt)) {
            val remainingTime = lastFailedLoginAt?.let { 
                userSpecification.getRemainingLockoutTime(it)
            }
            "账户已被锁定，请在${remainingTime?.toMinutes()}分钟后重试"
        }
        
        // 验证密码
        if (!password.matches(plainPassword)) {
            handleLoginFailure("密码错误", ipAddress, userAgent)
            throw BusinessRuleException("用户名或密码错误")
        }

        // 登录成功
        loginAttempts = 0
        lastFailedLoginAt = null
        lastLoginAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
        
        // 发布登录事件
        recordEvent(
            UserLoggedInEvent(
                userId = id,
                email = email,
                ipAddress = ipAddress,
                userAgent = userAgent,
                sessionId = sessionId
            )
        )
    }
    
    /**
     * 处理登录失败
     * 登录失败计数和失败时间
     */
    private fun handleLoginFailure(
        reason: String,
        ipAddress: String? = null,
        userAgent: String? = null
    ) {
        loginAttempts++
        lastFailedLoginAt = LocalDateTime.now()
        updatedAt = LocalDateTime.now()
        
        // 发布登录失败事件
        recordEvent(
            UserLoginFailedEvent(
                userId = id,
                email = email,
                failureReason = reason,
                ipAddress = ipAddress,
                userAgent = userAgent
            )
        )
        
        // 如果达到最大尝试次数，锁定账户
        if (loginAttempts >= UserSpecification.MAX_LOGIN_ATTEMPTS) {
            lockAccount("登录失败次数过多")
        }
    }
    
    /**
     * 更新用户资料
     * @param newProfile 新的用户资料
     * @param updatedBy 更新操作者（如果是管理员操作）
     */
    fun updateProfile(newProfile: UserProfile, updatedBy: UserId? = null) {
        require(status.canPerformActions()) { "用户状态不允许更新资料" }
        
        // 检查更新频率限制
        require(userSpecification.canUpdateProfile(profile.updatedAt)) {
            "资料更新过于频繁，请稍后再试"
        }
        
        val oldProfile = profile
        val changedFields = UserProfile.detectProfileChanges(oldProfile, newProfile)
        
        if (changedFields.isNotEmpty()) {
            profile = newProfile
            updatedAt = LocalDateTime.now()
            
            // 发布资料更新事件
            recordEvent(
                UserProfileUpdatedEvent(
                    userId = id,
                    oldProfile = oldProfile,
                    newProfile = newProfile,
                    changedFields = changedFields,
                    updatedBy = updatedBy
                )
            )
        }
    }
    

    /**
     * 修改密码
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @param ipAddress 操作IP地址
     */
    fun changePassword(
        oldPassword: String,
        newPassword: String,
        ipAddress: String? = null
    ) {
        require(status.canPerformActions()) { "用户状态不允许修改密码" }
        
        // 验证旧密码
        require(password.matches(oldPassword)) { "原密码不正确" }
        
        // 检查修改频率限制
        require(userSpecification.canChangePassword(password.createdAt)) {
            "密码修改过于频繁，请稍后再试"
        }
        
        // 验证新密码策略
        passwordSpecification.validatePassword(newPassword)
        
        // 检查新密码不能与旧密码相同
        require(!password.matches(newPassword)) { "新密码不能与原密码相同" }
        
        password = Password.of(newPassword)
        updatedAt = LocalDateTime.now()
        
        // 发布密码变更事件
        recordEvent(
            PasswordChangedEvent(
                userId = id,
                ipAddress = ipAddress
            )
        )
    }
    
    /**
     * 管理员重置密码
     * @param newPassword 新密码
     * @param resetBy 重置操作者
     * @param ipAddress 操作IP地址
     */
    fun resetPassword(
        newPassword: String,
        resetBy: UserId,
        ipAddress: String? = null
    ) {
        // 验证新密码策略
        passwordSpecification.validatePassword(newPassword)
        
        password = Password.of(newPassword)
        updatedAt = LocalDateTime.now()
        
        // 重置登录尝试次数
        loginAttempts = 0
        lastFailedLoginAt = null
        
        // 发布密码变更事件
        recordEvent(
            PasswordChangedEvent(
                userId = id,
                ipAddress = ipAddress,
                isAdminReset = true,
                resetBy = resetBy
            )
        )
    }
    
    /**
     * 激活用户
     * 默认激活即为邮箱验证通过
     * @param activatedBy 激活操作者
     */
    fun activate(activatedBy: UserId? = null) {
        val oldStatus = status
        status = status.activate()
        emailVerified = true
        updatedAt = LocalDateTime.now()

        // 发布状态变更事件
        addDomainEvent(
            UserStatusChangedEvent(
                userId = id,
                oldStatus = oldStatus,
                newStatus = status,
                reason = "用户激活",
                changedBy = activatedBy
            )
        )
        // 发布激活事件
        recordEvent(
            UserActivatedEvent(
                userId = id
            )
        )
    }
    
    /**
     * 停用用户
     * @param reason 停用原因
     * @param deactivatedBy 停用操作者
     */
    fun deactivate(reason: String, deactivatedBy: UserId? = null) {
        val oldStatus = status
        status = status.deactivate(reason)
        updatedAt = LocalDateTime.now()
        
        // 发布状态变更事件
        recordEvent(
            UserStatusChangedEvent(
                userId = id,
                oldStatus = oldStatus,
                newStatus = status,
                reason = reason,
                changedBy = deactivatedBy
            )
        )
    }
    
    /**
     * 锁定账户
     * @param reason 锁定原因
     * @param lockedBy 锁定操作者
     */
    fun lockAccount(reason: String, lockedBy: UserId? = null) {
        val oldStatus = status
        status = status.lock(reason)
        updatedAt = LocalDateTime.now()
        
        // 发布状态变更事件
        recordEvent(
            UserStatusChangedEvent(
                userId = id,
                oldStatus = oldStatus,
                newStatus = status,
                reason = reason,
                changedBy = lockedBy
            )
        )
    }
    
    /**
     * 解锁账户
     * @param unlockedBy 解锁操作者
     */
    fun unlockAccount(unlockedBy: UserId) {
        val oldStatus = status
        status = status.unlock()
        updatedAt = LocalDateTime.now()
        
        // 重置登录尝试次数
        loginAttempts = 0
        lastFailedLoginAt = null
        
        // 发布状态变更事件
        recordEvent(
            UserStatusChangedEvent(
                userId = id,
                oldStatus = oldStatus,
                newStatus = status,
                reason = "管理员解锁",
                changedBy = unlockedBy
            )
        )
    }
    

    /**
     * 更新用户偏好设置
     * @param newPreferences 新的偏好设置
     */
    fun updatePreferences(newPreferences: UserPreferences) {
        require(status.canPerformActions()) { "用户状态不允许更新偏好设置" }
        
        val oldPreferences = preferences
        val changedSettings = UserPreferences.detectPreferencesChanges(oldPreferences, newPreferences)
        
        if (changedSettings.isNotEmpty()) {
            preferences = newPreferences
            updatedAt = LocalDateTime.now()
            
            // 发布偏好设置更新事件
            recordEvent(
                UserPreferencesUpdatedEvent(
                    userId = id,
                    oldPreferences = oldPreferences,
                    newPreferences = newPreferences,
                    changedSettings = changedSettings
                )
            )
        }
    }
    

    /**
     * 删除用户
     * @param reason 删除原因
     * @param deletedBy 删除操作者
     * @param isHardDelete 是否硬删除
     */
    fun delete(
        reason: String = "用户主动删除",
        deletedBy: UserId? = null,
        isHardDelete: Boolean = false
    ) {
        val oldStatus = status
        status = status.delete(reason)
        updatedAt = LocalDateTime.now()
        
        // 发布用户删除事件
        recordEvent(
            UserDeletedEvent(
                userId = id,
                email = email,
                reason = reason,
                deletedBy = deletedBy,
                isHardDelete = isHardDelete
            )
        )
        
        // 发布状态变更事件
        addDomainEvent(
            UserStatusChangedEvent(
                userId = id,
                oldStatus = oldStatus,
                newStatus = status,
                reason = reason,
                changedBy = deletedBy
            )
        )
    }
    
    // Getter方法
    fun getEmail(): Email = email
    fun getPassword(): Password = password
    fun getProfile(): UserProfile = profile
    fun getStatus(): UserStatus = status
    fun getPreferences(): UserPreferences = preferences
    fun isEmailVerified(): Boolean = emailVerified
    fun getCreatedAt(): LocalDateTime = createdAt
    fun getUpdatedAt(): LocalDateTime = updatedAt
    fun getLastLoginAt(): LocalDateTime? = lastLoginAt
    fun getLoginAttempts(): Int = loginAttempts
    fun getLastFailedLoginAt(): LocalDateTime? = lastFailedLoginAt
    
    /**
     * 检查用户是否可以执行操作
     */
    fun canPerformActions(): Boolean = status.canPerformActions()
    
    /**
     * 检查用户是否可以登录
     */
    fun canLogin(): Boolean = status.canLogin() && 
        userSpecification.canAttemptLogin(loginAttempts, lastFailedLoginAt)
    
    /**
     * 检查密码是否需要更新
     */
    fun needsPasswordUpdate(): Boolean {
        return passwordSpecification.isPasswordExpired(password) || password.needsRehash()
    }

    /**
     * 是否可以重新激活
     */
    fun canReactivate(lastVerificationSent: LocalDateTime?): Boolean {
        return !status.isActive() && userSpecification.canSendEmailVerification(lastVerificationSent)
    }

    override fun applyEvent(event: DomainEvent) {
        // TODO 从事件重建聚合
        TODO("Not yet implemented")
    }

    override fun serializeState(): Map<String, Any> {
        return mapOf(
            "id" to id.toString(),
            "email" to email.value,
            "password" to "${password.hashedValue}:${password.salt}",
            "profile" to mapOf(
                "nickname" to profile.nickname,
                "avatar" to profile.avatar,
                "bio" to profile.bio,
                "updatedAt" to profile.updatedAt.toString()
            ),
            "status" to mapOf(
                "status" to status.status,
                "reason" to status.reason,
                "createdAt" to status.changedAt
            ),
            "preferences" to mapOf(
                "language" to preferences.language,
                "timezone" to preferences.timezone,
                "theme" to preferences.theme,
                "emailNotification" to preferences.notificationSettings.isEmailNotificationEnabled(),
                "smsNotification" to preferences.notificationSettings.isSmsNotificationEnabled(),
                "pushNotification" to preferences.notificationSettings.isPushNotificationEnabled()
            ),
            "emailVerified" to emailVerified,
            "createdAt" to createdAt.toString(),
            "updatedAt" to updatedAt.toString(),
            "lastLoginAt" to (lastLoginAt?.toString() ?: ""),
            "loginAttempts" to loginAttempts,
            "lastFailedLoginAt" to (lastFailedLoginAt?.toString() ?: "")
        )
    }

    override fun deserializeState(state: Map<String, Any>): Any {
        val passwordSplit = (state["password"] as String).split(":")
        val password = Password.fromHash(passwordSplit[0], passwordSplit[1])
        val status = UserStatus(
            status = (state["status"] as Map<*, *>)["status"] as StatusEnum,
            reason = (state["status"] as Map<*, *>)["reason"] as? String,
            changedAt = LocalDateTime.parse((state["status"] as Map<*, *>)["createdAt"] as String)
        )
        return User(
            id = UserId(state["id"] as String),
            email = Email(state["email"] as String),
            password = password,
            profile = UserProfile(
                nickname = (state["profile"] as Map<*, *>)["nickname"] as String,
                avatar = (state["profile"] as Map<*, *>)["avatar"] as? String,
                bio = (state["profile"] as Map<*, *>)["bio"] as? String,
                updatedAt = LocalDateTime.parse((state["profile"] as Map<*, *>)["updatedAt"] as String)
            ),
            status = status,
            preferences = UserPreferences(
                language = (state["preferences"] as Map<String, Any>)["language"] as Locale,
                timezone = (state["preferences"] as Map<String, Any>)["timezone"] as ZoneId,
                theme = (state["preferences"] as Map<String, Any>)["theme"] as Theme,
                notificationSettings = NotificationSettings(
                    emailNotification = (state["preferences"] as Map<*, *>)["emailNotification"] as Boolean,
                    smsNotification = (state["preferences"] as Map<*, *>)["smsNotification"] as Boolean,
                    pushNotification = (state["preferences"] as Map<*, *>)["pushNotification"] as Boolean
                )
            ),
            emailVerified = state["emailVerified"] as Boolean,
            createdAt = LocalDateTime.parse(state["createdAt"] as String),
            updatedAt = LocalDateTime.parse(state["updatedAt"] as String),
            lastLoginAt = (state["lastLoginAt"] as String).takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) },
            loginAttempts = state["loginAttempts"] as Int,
            lastFailedLoginAt = (state["lastFailedLoginAt"] as String).takeIf { it.isNotEmpty() }?.let { LocalDateTime.parse(it) }
        )
    }
}


/**
 * 用户策略
 * 定义用户相关的业务规则
 */
class UserSpecification {
    companion object {
        // 登录相关
        const val MAX_LOGIN_ATTEMPTS = 5
        val ACCOUNT_LOCKOUT_DURATION: Duration = Duration.ofMinutes(30)
        val SESSION_TIMEOUT: Duration = Duration.ofHours(24)

        // 用户资料限制
        const val NICKNAME_MAX_LENGTH = 50
        const val BIO_MAX_LENGTH = 500

        // 注册限制
        val MIN_AGE_YEARS = 13
        val REGISTRATION_COOLDOWN: Duration = Duration.ofMinutes(5)

        // 操作频率限制
        val PASSWORD_CHANGE_COOLDOWN: Duration = Duration.ofHours(1)
        val PROFILE_UPDATE_COOLDOWN: Duration = Duration.ofMinutes(10)
        val EMAIL_VERIFICATION_COOLDOWN: Duration = Duration.ofMinutes(5)
    }

    /**
     * 检查是否可以尝试登录
     */
    fun canAttemptLogin(failedAttempts: Int, lastFailedAttempt: LocalDateTime?): Boolean {
        if (failedAttempts < MAX_LOGIN_ATTEMPTS) {
            return true
        }

        return lastFailedAttempt?.let { lastAttempt ->
            Duration.between(lastAttempt, LocalDateTime.now()) >= ACCOUNT_LOCKOUT_DURATION
        } ?: true
    }

    /**
     * 检查会话是否过期
     */
    fun isSessionExpired(sessionStartTime: LocalDateTime): Boolean {
        return Duration.between(sessionStartTime, LocalDateTime.now()) > SESSION_TIMEOUT
    }

    /**
     * 检查是否可以修改密码
     */
    fun canChangePassword(lastPasswordChange: LocalDateTime?): Boolean {
        return lastPasswordChange?.let { lastChange ->
            Duration.between(lastChange, LocalDateTime.now()) >= PASSWORD_CHANGE_COOLDOWN
        } ?: true
    }

    /**
     * 检查是否可以更新资料
     */
    fun canUpdateProfile(lastProfileUpdate: LocalDateTime?): Boolean {
        return lastProfileUpdate?.let { lastUpdate ->
            Duration.between(lastUpdate, LocalDateTime.now()) >= PROFILE_UPDATE_COOLDOWN
        } ?: true
    }

    /**
     * 检查是否可以发送邮箱验证
     */
    fun canSendEmailVerification(lastVerificationSent: LocalDateTime?): Boolean {
        return lastVerificationSent?.let { lastSent ->
            Duration.between(lastSent, LocalDateTime.now()) >= EMAIL_VERIFICATION_COOLDOWN
        } ?: true
    }

    /**
     * 检查是否可以注册新账户（基于IP或其他标识）
     */
    fun canRegisterNewAccount(lastRegistration: LocalDateTime?): Boolean {
        return lastRegistration?.let { lastReg ->
            Duration.between(lastReg, LocalDateTime.now()) >= REGISTRATION_COOLDOWN
        } ?: true
    }

    /**
     * 计算账户锁定剩余时间
     */
    fun getRemainingLockoutTime(lastFailedAttempt: LocalDateTime): Duration {
        val elapsed = Duration.between(lastFailedAttempt, LocalDateTime.now())
        val remaining = ACCOUNT_LOCKOUT_DURATION.minus(elapsed)
        return if (remaining.isNegative) Duration.ZERO else remaining
    }
}
