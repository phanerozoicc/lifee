package com.github.phanerozoicc.user.domain.model

import java.util.regex.Pattern

/**
 * 邮箱值对象
 * 封装邮箱地址的验证逻辑和操作
 */
data class Email(
    val value: String
) {
    companion object {
        // 邮箱格式正则表达式
        private val EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        
        // 允许的域名列表（可配置）
        private val ALLOWED_DOMAINS = setOf(
            "gmail.com", "yahoo.com", "outlook.com", "hotmail.com",
            "qq.com", "163.com", "126.com", "sina.com"
        )
        
        // 禁止的域名列表（可配置）
        private val BLOCKED_DOMAINS = setOf(
            "tempmail.com", "10minutemail.com", "guerrillamail.com"
        )
        
        // 邮箱最大长度
        private const val MAX_LENGTH = 254
        
        /**
         * 从字符串创建邮箱对象
         * @param value 邮箱地址字符串
         * @return Email实例
         * @throws IllegalArgumentException 如果邮箱格式不正确
         */
        fun of(value: String): Email {
            val trimmedValue = value.trim().lowercase()
            validateFormat(trimmedValue)
            validateLength(trimmedValue)
            validateDomain(getDomain(trimmedValue))
            return Email(trimmedValue)
        }
        
        /**
         * 验证邮箱格式
         */
        private fun validateFormat(email: String) {
            require(email.isNotBlank()) { "邮箱地址不能为空" }
            require(EMAIL_PATTERN.matcher(email).matches()) { "邮箱格式不正确" }
        }
        
        /**
         * 验证邮箱长度
         */
        private fun validateLength(email: String) {
            require(email.length <= MAX_LENGTH) { "邮箱地址长度不能超过${MAX_LENGTH}个字符" }
        }
        
        /**
         * 验证域名
         */
        private fun validateDomain(domain: String) {
            require(!BLOCKED_DOMAINS.contains(domain)) { "不允许使用临时邮箱域名: $domain" }
            // 注意：这里不强制要求在允许列表中，以支持企业邮箱等
        }
        
        /**
         * 获取域名部分
         */
        private fun getDomain(email: String): String {
            val atIndex = email.lastIndexOf('@')
            return if (atIndex > 0 && atIndex < email.length - 1) {
                email.substring(atIndex + 1)
            } else {
                throw IllegalArgumentException("无效的邮箱格式")
            }
        }
    }
    
    /**
     * 获取邮箱值
     */
    fun getValue(): String = value
    
    /**
     * 获取本地部分（@符号前的部分）
     */
    fun getLocalPart(): String {
        val atIndex = value.indexOf('@')
        return if (atIndex > 0) value.substring(0, atIndex) else ""
    }
    
    /**
     * 获取域名部分（@符号后的部分）
     */
    fun getDomain(): String {
        val atIndex = value.lastIndexOf('@')
        return if (atIndex >= 0 && atIndex < value.length - 1) {
            value.substring(atIndex + 1)
        } else {
            ""
        }
    }
    
    /**
     * 检查邮箱是否有效
     */
    fun isValid(): Boolean {
        return try {
            EMAIL_PATTERN.matcher(value).matches() && 
            value.length <= MAX_LENGTH &&
            !BLOCKED_DOMAINS.contains(getDomain())
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * 转换为字符串
     */
    override fun toString(): String = value
    
    init {
        require(value.isNotBlank()) { "邮箱地址不能为空" }
        require(EMAIL_PATTERN.matcher(value).matches()) { "邮箱格式不正确" }
        require(value.length <= MAX_LENGTH) { "邮箱地址长度不能超过${MAX_LENGTH}个字符" }
    }
}


/**
 * 邮箱规则
 * 定义邮箱相关的业务规则
 */
class EmailSpecification {
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
 * 邮箱风险等级枚举
 */
enum class EmailRiskLevel {
    LOW,     // 低风险
    MEDIUM,  // 中等风险
    HIGH     // 高风险
}
