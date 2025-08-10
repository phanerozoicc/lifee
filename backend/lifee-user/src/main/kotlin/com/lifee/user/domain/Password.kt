package com.lifee.user.domain

import com.lifee.common.domain.ValueObject
import com.lifee.common.exceptions.BusinessRuleException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.regex.Pattern

/**
 * 密码值对象
 */
data class Password(
    val hashedValue: String
) : ValueObject() {
    
    companion object {
        private val passwordEncoder = BCryptPasswordEncoder()
        
        // 密码强度要求：至少8位，包含大小写字母、数字和特殊字符
        private val PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
        )
        
        /**
         * 从明文密码创建密码值对象
         */
        fun fromPlainText(plainPassword: String): Password {
            validatePassword(plainPassword)
            val hashedPassword = passwordEncoder.encode(plainPassword)
            return Password(hashedPassword)
        }
        
        /**
         * 从已加密的密码创建密码值对象（用于从数据库加载）
         */
        fun fromHashedValue(hashedValue: String): Password {
            BusinessRuleException.throwIf(
                hashedValue.isBlank(),
                "加密密码不能为空"
            )
            return Password(hashedValue)
        }
        
        private fun validatePassword(plainPassword: String) {
            BusinessRuleException.throwIf(
                plainPassword.isBlank(),
                "密码不能为空"
            )
            
            BusinessRuleException.throwIf(
                plainPassword.length < 8,
                "密码长度不能少于8位"
            )
            
            BusinessRuleException.throwIf(
                plainPassword.length > 128,
                "密码长度不能超过128位"
            )
            
            BusinessRuleException.throwIf(
                !PASSWORD_PATTERN.matcher(plainPassword).matches(),
                "密码必须包含大小写字母、数字和特殊字符"
            )
        }
    }
    
    override fun getEqualityComponents(): List<Any> {
        return listOf(hashedValue)
    }
    
    /**
     * 验证明文密码是否匹配
     */
    fun matches(plainPassword: String): Boolean {
        return passwordEncoder.matches(plainPassword, hashedValue)
    }
    
    /**
     * 检查密码是否需要重新加密（用于密码强度升级）
     */
    fun needsRehash(): Boolean {
        return passwordEncoder.upgradeEncoding(hashedValue)
    }
    
    override fun toString(): String {
        return "[PROTECTED]"
    }
}