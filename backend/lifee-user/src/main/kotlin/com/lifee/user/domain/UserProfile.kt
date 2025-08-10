package com.lifee.user.domain

import com.lifee.common.domain.ValueObject
import com.lifee.common.exceptions.BusinessRuleException
import java.time.LocalDate
import java.time.Period

/**
 * 用户档案值对象
 */
data class UserProfile(
    val firstName: String,
    val lastName: String,
    val dateOfBirth: LocalDate?,
    val phoneNumber: String?,
    val avatar: String?
) : ValueObject() {
    
    companion object {
        /**
         * 创建用户档案
         */
        fun create(
            firstName: String,
            lastName: String,
            dateOfBirth: LocalDate? = null,
            phoneNumber: String? = null,
            avatar: String? = null
        ): UserProfile {
            validateFirstName(firstName)
            validateLastName(lastName)
            dateOfBirth?.let { validateDateOfBirth(it) }
            phoneNumber?.let { validatePhoneNumber(it) }
            avatar?.let { validateAvatar(it) }
            
            return UserProfile(
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                dateOfBirth = dateOfBirth,
                phoneNumber = phoneNumber?.trim(),
                avatar = avatar?.trim()
            )
        }
        
        private fun validateFirstName(firstName: String) {
            BusinessRuleException.throwIf(
                firstName.isBlank(),
                "名字不能为空"
            )
            
            BusinessRuleException.throwIf(
                firstName.length > 50,
                "名字长度不能超过50个字符"
            )
        }
        
        private fun validateLastName(lastName: String) {
            BusinessRuleException.throwIf(
                lastName.isBlank(),
                "姓氏不能为空"
            )
            
            BusinessRuleException.throwIf(
                lastName.length > 50,
                "姓氏长度不能超过50个字符"
            )
        }
        
        private fun validateDateOfBirth(dateOfBirth: LocalDate) {
            val now = LocalDate.now()
            
            BusinessRuleException.throwIf(
                dateOfBirth.isAfter(now),
                "出生日期不能是未来日期"
            )
            
            val age = Period.between(dateOfBirth, now).years
            BusinessRuleException.throwIf(
                age > 150,
                "年龄不能超过150岁"
            )
        }
        
        private fun validatePhoneNumber(phoneNumber: String) {
            BusinessRuleException.throwIf(
                phoneNumber.isBlank(),
                "电话号码不能为空"
            )
            
            BusinessRuleException.throwIf(
                phoneNumber.length > 20,
                "电话号码长度不能超过20个字符"
            )
            
            // 简单的电话号码格式验证（只允许数字、+、-、空格、括号）
            val phonePattern = "^[+\\d\\s\\-()]+$".toRegex()
            BusinessRuleException.throwIf(
                !phonePattern.matches(phoneNumber),
                "电话号码格式不正确"
            )
        }
        
        private fun validateAvatar(avatar: String) {
            BusinessRuleException.throwIf(
                avatar.isBlank(),
                "头像URL不能为空"
            )
            
            BusinessRuleException.throwIf(
                avatar.length > 500,
                "头像URL长度不能超过500个字符"
            )
        }
    }
    
    override fun getEqualityComponents(): List<Any?> {
        return listOf(firstName, lastName, dateOfBirth, phoneNumber, avatar)
    }
    
    /**
     * 获取全名
     */
    fun getFullName(): String {
        return "$firstName $lastName"
    }
    
    /**
     * 获取年龄
     */
    fun getAge(): Int? {
        return dateOfBirth?.let { 
            Period.between(it, LocalDate.now()).years 
        }
    }
    
    /**
     * 检查是否有头像
     */
    fun hasAvatar(): Boolean {
        return !avatar.isNullOrBlank()
    }
    
    /**
     * 检查是否有电话号码
     */
    fun hasPhoneNumber(): Boolean {
        return !phoneNumber.isNullOrBlank()
    }
    
    /**
     * 更新档案信息
     */
    fun updateProfile(
        firstName: String? = null,
        lastName: String? = null,
        dateOfBirth: LocalDate? = null,
        phoneNumber: String? = null,
        avatar: String? = null
    ): UserProfile {
        return create(
            firstName = firstName ?: this.firstName,
            lastName = lastName ?: this.lastName,
            dateOfBirth = dateOfBirth ?: this.dateOfBirth,
            phoneNumber = phoneNumber ?: this.phoneNumber,
            avatar = avatar ?: this.avatar
        )
    }
}