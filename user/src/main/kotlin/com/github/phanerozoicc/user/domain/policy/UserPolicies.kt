package com.github.phanerozoicc.user.domain.policy

import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.Password
import java.time.Duration
import java.time.LocalDateTime

/**
 * 密码策略
 * 定义密码相关的业务规则
 */
class PasswordPolicy {
    companion object {
        // 密码长度限制
        const val MIN_LENGTH = 8
        const val MAX_LENGTH = 128
        
        // 字符要求
        const val REQUIRE_UPPERCASE = true
        const val REQUIRE_LOWERCASE = true
        const val REQUIRE_DIGITS = true
        const val REQUIRE_SPECIAL_CHARS = true
        
        // 密码有效期
        val PASSWORD_MAX_AGE: Duration = Duration.ofDays(90)
        
        // 密码历史限制
        const val PASSWORD_HISTORY_COUNT = 5
        
        // 密码复杂度要求
        const val MIN_UNIQUE_CHARS = 6
        
        // 禁用的弱密码模式
        private val WEAK_PATTERNS = listOf(
            "123456", "password", "123456789", "12345678", "12345",
            "1234567", "1234567890", "qwerty", "abc123", "111111",
            "123123", "admin", "letmein", "welcome", "monkey"
        )
        
        // 特殊字符集合
        private val SPECIAL_CHARS = setOf(
            '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_',
            '=', '+', '[', ']', '{', '}', '|', '\\', ':', ';', '"', "'",
            '<', '>', ',', '.', '?', '/', '~', '`'
        )
    }
    
    /**
     * 验证密码是否符合策略
     * @param plainPassword 明文密码
     * @throws IllegalArgumentException 如果密码不符合策略
     */
    fun validatePassword(plainPassword: String) {
        require(plainPassword.isNotBlank()) { "密码不能为空" }
        
        // 长度检查
        require(plainPassword.length >= MIN_LENGTH) {
            "密码长度不能少于${MIN_LENGTH}个字符"
        }
        require(plainPassword.length <= MAX_LENGTH) {
            "密码长度不能超过${MAX_LENGTH}个字符"
        }
        
        // 字符类型检查
        if (REQUIRE_UPPERCASE) {
            require(plainPassword.any { it.isUpperCase() }) {
                "密码必须包含至少一个大写字母"
            }
        }
        
        if (REQUIRE_LOWERCASE) {
            require(plainPassword.any { it.isLowerCase() }) {
                "密码必须包含至少一个小写字母"
            }
        }
        
        if (REQUIRE_DIGITS) {
            require(plainPassword.any { it.isDigit() }) {
                "密码必须包含至少一个数字"
            }
        }
        
        if (REQUIRE_SPECIAL_CHARS) {
            require(plainPassword.any { SPECIAL_CHARS.contains(it) }) {
                "密码必须包含至少一个特殊字符: ${SPECIAL_CHARS.joinToString("")}"
            }
        }
        
        // 唯一字符检查
        require(plainPassword.toSet().size >= MIN_UNIQUE_CHARS) {
            "密码必须包含至少${MIN_UNIQUE_CHARS}个不同的字符"
        }
        
        // 弱密码检查
        val lowerPassword = plainPassword.lowercase()
        WEAK_PATTERNS.forEach { pattern ->
            require(!lowerPassword.contains(pattern)) {
                "密码不能包含常见的弱密码模式"
            }
        }
        
        // 重复字符检查
        require(!hasRepeatingChars(plainPassword)) {
            "密码不能包含连续重复的字符（如：aaa、111）"
        }
        
        // 连续字符检查
        require(!hasSequentialChars(plainPassword)) {
            "密码不能包含连续的字符序列（如：abc、123）"
        }
    }
    
    /**
     * 检查密码是否过期
     */
    fun isPasswordExpired(password: Password): Boolean {
        return password.isExpired(PASSWORD_MAX_AGE)
    }
    
    /**
     * 检查是否有重复字符
     */
    private fun hasRepeatingChars(password: String): Boolean {
        for (i in 0 until password.length - 2) {
            if (password[i] == password[i + 1] && password[i + 1] == password[i + 2]) {
                return true
            }
        }
        return false
    }
    
    /**
     * 检查是否有连续字符
     */
    private fun hasSequentialChars(password: String): Boolean {
        for (i in 0 until password.length - 2) {
            val char1 = password[i].code
            val char2 = password[i + 1].code
            val char3 = password[i + 2].code
            
            if ((char2 == char1 + 1 && char3 == char2 + 1) ||
                (char2 == char1 - 1 && char3 == char2 - 1)) {
                return true
            }
        }
        return false
    }
    
    /**
     * 计算密码强度分数（0-100）
     */
    fun calculatePasswordStrength(plainPassword: String): Int {
        var score = 0
        
        // 长度分数
        score += when {
            plainPassword.length >= 12 -> 25
            plainPassword.length >= 10 -> 20
            plainPassword.length >= 8 -> 15
            else -> 0
        }
        
        // 字符类型分数
        if (plainPassword.any { it.isLowerCase() }) score += 15
        if (plainPassword.any { it.isUpperCase() }) score += 15
        if (plainPassword.any { it.isDigit() }) score += 15
        if (plainPassword.any { SPECIAL_CHARS.contains(it) }) score += 15
        
        // 复杂度分数
        val uniqueChars = plainPassword.toSet().size
        score += when {
            uniqueChars >= 10 -> 15
            uniqueChars >= 8 -> 10
            uniqueChars >= 6 -> 5
            else -> 0
        }
        
        // 扣分项
        if (hasRepeatingChars(plainPassword)) score -= 10
        if (hasSequentialChars(plainPassword)) score -= 10
        
        val lowerPassword = plainPassword.lowercase()
        WEAK_PATTERNS.forEach { pattern ->
            if (lowerPassword.contains(pattern)) score -= 20
        }
        
        return maxOf(0, minOf(100, score))
    }
}

/**
 * 邮箱策略
 * 定义邮箱相关的业务规则
 */
class EmailPolicy {
    companion object {
        // 邮箱最大长度
        const val MAX_LENGTH = 254
        
        // 允许的域名（可配置）
        private val ALLOWED_DOMAINS = setOf(
            "gmail.com", "yahoo.com", "outlook.com", "hotmail.com",
            "qq.com", "163.com", "126.com", "sina.com", "sohu.com"
        )
        
        // 禁止的域名（临时邮箱等）
        private val BLOCKED_DOMAINS = setOf(
            "tempmail.com", "10minutemail.com", "guerrillamail.com",
            "mailinator.com", "throwaway.email", "temp-mail.org"
        )
        
        // 企业邮箱域名模式
        private val CORPORATE_DOMAIN_PATTERNS = listOf(
            ".edu", ".gov", ".org"
        )
    }
    
    /**
     * 验证邮箱是否符合策略
     */
    fun validateEmail(email: Email) {
        val domain = email.getDomain()
        
        // 检查是否在禁止列表中
        require(!BLOCKED_DOMAINS.contains(domain)) {
            "不允许使用临时邮箱域名: $domain"
        }
        
        // 长度检查
        require(email.getValue().length <= MAX_LENGTH) {
            "邮箱地址长度不能超过${MAX_LENGTH}个字符"
        }
    }
    
    /**
     * 检查是否为企业邮箱
     */
    fun isCorporateEmail(email: Email): Boolean {
        val domain = email.getDomain().lowercase()
        return CORPORATE_DOMAIN_PATTERNS.any { pattern ->
            domain.endsWith(pattern)
        } || !ALLOWED_DOMAINS.contains(domain)
    }
    
    /**
     * 检查是否为可信域名
     */
    fun isTrustedDomain(email: Email): Boolean {
        val domain = email.getDomain().lowercase()
        return ALLOWED_DOMAINS.contains(domain) || isCorporateEmail(email)
    }
    
    /**
     * 获取邮箱风险等级
     */
    fun getEmailRiskLevel(email: Email): EmailRiskLevel {
        val domain = email.getDomain().lowercase()
        
        return when {
            BLOCKED_DOMAINS.contains(domain) -> EmailRiskLevel.HIGH
            ALLOWED_DOMAINS.contains(domain) -> EmailRiskLevel.LOW
            isCorporateEmail(email) -> EmailRiskLevel.LOW
            else -> EmailRiskLevel.MEDIUM
        }
    }
}

/**
 * 用户策略
 * 定义用户相关的业务规则
 */
class UserPolicy {
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

/**
 * 邮箱风险等级枚举
 */
enum class EmailRiskLevel {
    LOW,     // 低风险
    MEDIUM,  // 中等风险
    HIGH     // 高风险
}