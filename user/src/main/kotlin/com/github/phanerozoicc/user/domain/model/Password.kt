package com.github.phanerozoicc.user.domain.model

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.security.SecureRandom
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

/**
 * 密码值对象
 * 封装密码的加密、验证和安全策略
 */
data class Password(
    private val hashedValue: String,
    private val salt: String,
    private val algorithm: String = "BCrypt",
    private val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        private val passwordEncoder = BCryptPasswordEncoder()
        private val secureRandom = SecureRandom()
        
        // 密码策略常量
        private const val MIN_LENGTH = 8
        private const val MAX_LENGTH = 128
        private const val REQUIRE_UPPERCASE = true
        private const val REQUIRE_LOWERCASE = true
        private const val REQUIRE_DIGITS = true
        private const val REQUIRE_SPECIAL_CHARS = true
        private val MAX_AGE = Duration.ofDays(90)
        
        // 特殊字符集合
        private val SPECIAL_CHARS = setOf(
            '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '-', '_', 
            '=', '+', '[', ']', '{', '}', '|', '\\', ':', ';', '"', "'", 
            '<', '>', ',', '.', '?', '/', '~', '`'
        )
        
        /**
         * 从明文密码创建Password对象
         * @param plainText 明文密码
         * @return Password实例
         * @throws IllegalArgumentException 如果密码不符合策略要求
         */
        fun of(plainText: String): Password {
            validateStrength(plainText)
            val salt = generateSalt()
            val hashedValue = hashPassword(plainText, salt)
            return Password(hashedValue, salt)
        }
        
        /**
         * 从已有的哈希值创建Password对象（用于从数据库恢复）
         * @param hashedValue 哈希后的密码
         * @param salt 盐值
         * @param algorithm 加密算法
         * @param createdAt 创建时间
         * @return Password实例
         */
        fun fromHash(
            hashedValue: String, 
            salt: String, 
            algorithm: String = "BCrypt",
            createdAt: LocalDateTime = LocalDateTime.now()
        ): Password {
            require(hashedValue.isNotBlank()) { "哈希值不能为空" }
            require(salt.isNotBlank()) { "盐值不能为空" }
            return Password(hashedValue, salt, algorithm, createdAt)
        }
        
        /**
         * 验证密码强度
         */
        private fun validateStrength(plainText: String) {
            require(plainText.isNotBlank()) { "密码不能为空" }
            require(plainText.length >= MIN_LENGTH) { "密码长度不能少于${MIN_LENGTH}个字符" }
            require(plainText.length <= MAX_LENGTH) { "密码长度不能超过${MAX_LENGTH}个字符" }
            
            if (REQUIRE_UPPERCASE) {
                require(plainText.any { it.isUpperCase() }) { "密码必须包含至少一个大写字母" }
            }
            
            if (REQUIRE_LOWERCASE) {
                require(plainText.any { it.isLowerCase() }) { "密码必须包含至少一个小写字母" }
            }
            
            if (REQUIRE_DIGITS) {
                require(plainText.any { it.isDigit() }) { "密码必须包含至少一个数字" }
            }
            
            if (REQUIRE_SPECIAL_CHARS) {
                require(plainText.any { SPECIAL_CHARS.contains(it) }) { 
                    "密码必须包含至少一个特殊字符: ${SPECIAL_CHARS.joinToString("")}"
                }
            }
        }
        
        /**
         * 生成盐值
         */
        private fun generateSalt(): String {
            val saltBytes = ByteArray(16)
            secureRandom.nextBytes(saltBytes)
            return Base64.getEncoder().encodeToString(saltBytes)
        }
        
        /**
         * 哈希密码
         */
        private fun hashPassword(plainText: String, salt: String): String {
            // BCrypt会自动处理盐值，这里的salt主要用于额外的安全性
            return passwordEncoder.encode(plainText + salt)
        }
    }
    
    /**
     * 验证明文密码是否匹配
     * @param plainText 明文密码
     * @return 是否匹配
     */
    fun matches(plainText: String): Boolean {
        return try {
            passwordEncoder.matches(plainText + salt, hashedValue)
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 检查密码是否过期
     * @param maxAge 最大有效期，默认使用系统配置
     * @return 是否过期
     */
    fun isExpired(maxAge: Duration = MAX_AGE): Boolean {
        return Duration.between(createdAt, LocalDateTime.now()) > maxAge
    }
    
    /**
     * 检查是否需要重新哈希（算法升级等）
     * @return 是否需要重新哈希
     */
    fun needsRehash(): Boolean {
        // 如果算法不是当前推荐的算法，则需要重新哈希
        return algorithm != "BCrypt"
    }
    
    /**
     * 获取哈希值
     */
    fun getHashedValue(): String = hashedValue
    
    /**
     * 获取盐值
     */
    fun getSalt(): String = salt
    
    /**
     * 获取密码强度等级
     */
    fun getStrength(): PasswordStrength {
        // 这里简化实现，实际可以根据更复杂的规则计算
        return when {
            hashedValue.length > 60 -> PasswordStrength.STRONG
            hashedValue.length > 40 -> PasswordStrength.MEDIUM
            else -> PasswordStrength.WEAK
        }
    }
    
    /**
     * 获取创建时间
     */
    fun getCreatedAt(): LocalDateTime = createdAt
    
    /**
     * 获取算法
     */
    fun getAlgorithm(): String = algorithm
    
    init {
        require(hashedValue.isNotBlank()) { "哈希值不能为空" }
        require(salt.isNotBlank()) { "盐值不能为空" }
        require(algorithm.isNotBlank()) { "算法不能为空" }
    }
}

/**
 * 密码强度枚举
 */
enum class PasswordStrength {
    WEAK,    // 弱
    MEDIUM,  // 中等
    STRONG   // 强
}


/**
 * 密码策略
 * 定义密码相关的业务规则
 */
class PasswordSpecification {
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
