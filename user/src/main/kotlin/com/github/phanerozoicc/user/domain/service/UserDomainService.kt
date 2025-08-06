package com.github.phanerozoicc.user.domain.service

import com.github.phanerozoicc.user.domain.model.*
import com.github.phanerozoicc.user.domain.repository.UserRepository
import java.time.LocalDateTime

/**
 * 用户领域服务
 * 处理跨聚合根的业务逻辑和复杂的业务规则
 */
class UserDomainService(
    private val userRepository: UserRepository
) {
    private val emailSpecification = EmailSpecification()
    private val passwordSpecification = PasswordSpecification()
    private val userSpecification = UserSpecification()
    
    /**
     * 验证用户注册信息的唯一性
     * @param email 邮箱地址
     * @param nickname 昵称
     * @throws IllegalArgumentException 如果邮箱或昵称已存在
     */
    fun validateUserUniqueness(email: Email, nickname: String) {
         // 验证邮箱策略
         emailSpecification.validateEmail(email)
         
         // 检查邮箱唯一性
         if (userRepository.existsByEmail(email)) {
             throw IllegalArgumentException("邮箱地址已被注册")
         }
        
        // 检查昵称唯一性
        if (userRepository.existsByNickname(nickname)) {
            throw IllegalArgumentException("昵称已被使用")
        }
    }
    
    /**
     * 验证用户资料更新的唯一性
     * @param userId 用户ID
     * @param nickname 新昵称
     * @throws IllegalArgumentException 如果昵称已被其他用户使用
     */
    fun validateProfileUpdateUniqueness(userId: UserId, nickname: String) {
        if (userRepository.existsByNicknameExcluding(nickname, userId)) {
            throw IllegalArgumentException("昵称已被其他用户使用")
        }
    }
    
    /**
     * 检查用户是否可以执行敏感操作
     * @param user 用户实例
     * @param operationType 操作类型
     * @return 是否可以执行操作
     */
    fun canPerformSensitiveOperation(user: User, operationType: SensitiveOperationType): Boolean {
        // 检查用户状态
        if (!user.canPerformActions()) {
            return false
        }
        
        // 检查邮箱验证状态
        if (!user.isEmailVerified() && operationType.requiresEmailVerification()) {
            return false
        }
        
        // 检查操作频率限制
        return when (operationType) {
            SensitiveOperationType.CHANGE_PASSWORD -> {
                userSpecification.canChangePassword(user.getPassword().getCreatedAt())
            }
            SensitiveOperationType.UPDATE_PROFILE -> {
                userSpecification.canUpdateProfile(user.getProfile().getUpdatedAt())
            }
            SensitiveOperationType.CHANGE_EMAIL -> {
                // TODO: 实现邮箱变更权限检查逻辑
                true
            }
            SensitiveOperationType.DELETE_ACCOUNT -> {
                // TODO: 实现账户删除权限检查逻辑
                true
            }
        }
    }
    
    /**
     * 生成安全的临时密码
     * @return 临时密码
     */
    fun generateTemporaryPassword(): String {
        // TODO: 实现安全密码生成逻辑
        return "TempPassword123!"
    }
    
    /**
     * 检查密码强度
     * @param password 密码
     * @return 密码强度等级
     */
    fun checkPasswordStrength(password: String): PasswordStrength {
        // TODO: 实现密码强度检查逻辑
        return PasswordStrength.MEDIUM
    }
    
    /**
     * 查找需要密码更新提醒的用户
     * @param reminderDays 提醒天数（密码过期前多少天提醒）
     * @return 需要提醒的用户列表
     */
    fun findUsersNeedingPasswordReminder(reminderDays: Int = 7): List<User> {
        val reminderDate = LocalDateTime.now().minusDays(90L - reminderDays.toLong()) // 密码过期天数
        return userRepository.findUsersNeedingPasswordUpdate(reminderDate)
    }
    
    /**
     * 查找长期未登录的用户
     * @param inactiveDays 非活跃天数
     * @return 长期未登录的用户列表
     */
    fun findInactiveUsers(inactiveDays: Int = 90): List<User> {
        val inactiveDate = LocalDateTime.now().minusDays(inactiveDays.toLong())
        return userRepository.findLastLoginAfter(inactiveDate)
    }
    
    /**
     * 查找未验证邮箱的过期用户
     * @param expireDays 过期天数
     * @return 未验证邮箱的过期用户列表
     */
    fun findExpiredUnverifiedUsers(expireDays: Int = 7): List<User> {
        val expireDate = LocalDateTime.now().minusDays(expireDays.toLong())
        return userRepository.findUnverifiedUsers(expireDate)
    }
    
    /**
     * 批量清理过期的未验证用户
     * @param expireDays 过期天数
     * @return 清理的用户数量
     */
    fun cleanupExpiredUnverifiedUsers(expireDays: Int = 7): Int {
        val expiredUsers = findExpiredUnverifiedUsers(expireDays)
        val userIds = expiredUsers.map { it.id }
        
        if (userIds.isNotEmpty()) {
            userRepository.deleteAll(userIds)
        }
        
        return expiredUsers.size
    }
    
    /**
     * 检查用户账户安全状态
     * @param user 用户实例
     * @return 安全状态报告
     */
    fun checkAccountSecurity(user: User): AccountSecurityReport {
        val issues = mutableListOf<SecurityIssue>()
        
        // 检查密码安全性
        if (user.needsPasswordUpdate()) {
            issues.add(SecurityIssue.PASSWORD_EXPIRED)
        }
        
        val passwordStrength = checkPasswordStrength(
              "dummy_password" // TODO: 实现正确的密码强度检查逻辑
          )
        if (passwordStrength == PasswordStrength.WEAK) {
            issues.add(SecurityIssue.WEAK_PASSWORD)
        }
        
        // 检查邮箱验证状态
        if (!user.isEmailVerified()) {
            issues.add(SecurityIssue.EMAIL_NOT_VERIFIED)
        }
        
        // 检查登录异常
        if (user.getLoginAttempts() > 0) {
            issues.add(SecurityIssue.RECENT_LOGIN_FAILURES)
        }
        
        // 检查长期未登录
        val lastLogin = user.getLastLoginAt()
        if (lastLogin != null && lastLogin.isBefore(LocalDateTime.now().minusDays(90))) {
            issues.add(SecurityIssue.LONG_TIME_INACTIVE)
        }
        
        return AccountSecurityReport(
            userId = user.id,
            securityLevel = calculateSecurityLevel(issues),
            issues = issues,
            recommendations = generateSecurityRecommendations(issues)
        )
    }
    
    /**
     * 计算安全等级
     */
    private fun calculateSecurityLevel(issues: List<SecurityIssue>): SecurityLevel {
        val criticalIssues = issues.count { it.isCritical() }
        val warningIssues = issues.count { !it.isCritical() }
        
        return when {
            criticalIssues > 0 -> SecurityLevel.LOW
            warningIssues > 2 -> SecurityLevel.MEDIUM
            warningIssues > 0 -> SecurityLevel.HIGH
            else -> SecurityLevel.EXCELLENT
        }
    }
    
    /**
     * 生成安全建议
     */
    private fun generateSecurityRecommendations(issues: List<SecurityIssue>): List<String> {
        return issues.map { issue ->
            when (issue) {
                SecurityIssue.PASSWORD_EXPIRED -> "请及时更新您的密码"
                SecurityIssue.WEAK_PASSWORD -> "建议使用更强的密码，包含大小写字母、数字和特殊字符"
                SecurityIssue.EMAIL_NOT_VERIFIED -> "请验证您的邮箱地址以提高账户安全性"
                SecurityIssue.RECENT_LOGIN_FAILURES -> "检测到最近有登录失败记录，请确认账户安全"
                SecurityIssue.LONG_TIME_INACTIVE -> "账户长期未使用，建议定期登录以保持活跃状态"
            }
        }
    }
}

/**
 * 敏感操作类型
 */
enum class SensitiveOperationType {
    CHANGE_PASSWORD,
    UPDATE_PROFILE,
    CHANGE_EMAIL,
    DELETE_ACCOUNT;
    
    /**
     * 是否需要邮箱验证
     */
    fun requiresEmailVerification(): Boolean {
        return when (this) {
            CHANGE_PASSWORD, CHANGE_EMAIL, DELETE_ACCOUNT -> true
            UPDATE_PROFILE -> false
        }
    }
}

/**
 * 安全问题类型
 */
enum class SecurityIssue {
    PASSWORD_EXPIRED,
    WEAK_PASSWORD,
    EMAIL_NOT_VERIFIED,
    RECENT_LOGIN_FAILURES,
    LONG_TIME_INACTIVE;
    
    /**
     * 是否为严重问题
     */
    fun isCritical(): Boolean {
        return when (this) {
            PASSWORD_EXPIRED, EMAIL_NOT_VERIFIED -> true
            WEAK_PASSWORD, RECENT_LOGIN_FAILURES, LONG_TIME_INACTIVE -> false
        }
    }
}

/**
 * 安全等级
 */
enum class SecurityLevel {
    EXCELLENT,
    HIGH,
    MEDIUM,
    LOW
}

/**
 * 账户安全报告
 */
data class AccountSecurityReport(
    val userId: UserId,
    val securityLevel: SecurityLevel,
    val issues: List<SecurityIssue>,
    val recommendations: List<String>
)