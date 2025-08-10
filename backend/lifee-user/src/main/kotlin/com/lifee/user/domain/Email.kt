package com.lifee.user.domain

import com.lifee.common.domain.ValueObject
import com.lifee.common.exceptions.BusinessRuleException
import java.util.regex.Pattern

/**
 * 邮箱值对象
 */
data class Email(
    val value: String
) : ValueObject() {
    
    companion object {
        private val EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
        )
        
        /**
         * 创建邮箱值对象
         */
        fun of(value: String): Email {
            validateEmail(value)
            return Email(value.lowercase().trim())
        }
        
        private fun validateEmail(value: String) {
            BusinessRuleException.throwIf(
                value.isBlank(),
                "邮箱不能为空"
            )
            
            BusinessRuleException.throwIf(
                value.length > 254,
                "邮箱长度不能超过254个字符"
            )
            
            BusinessRuleException.throwIf(
                !EMAIL_PATTERN.matcher(value).matches(),
                "邮箱格式不正确"
            )
        }
    }
    
    init {
        validateEmail(value)
    }
    
    override fun getEqualityComponents(): List<Any> {
        return listOf(value)
    }
    
    override fun toString(): String {
        return value
    }
    
    /**
     * 获取邮箱的域名部分
     */
    fun getDomain(): String {
        return value.substringAfter("@")
    }
    
    /**
     * 获取邮箱的用户名部分
     */
    fun getLocalPart(): String {
        return value.substringBefore("@")
    }
}