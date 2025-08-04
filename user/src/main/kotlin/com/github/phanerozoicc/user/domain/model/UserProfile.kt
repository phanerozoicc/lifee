package com.github.phanerozoicc.user.domain.model

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period

/**
 * 用户资料值对象
 * 封装用户的个人信息
 */
data class UserProfile(
    private val nickname: String,
    private val firstName: String? = null,
    private val lastName: String? = null,
    private val avatar: String? = null,
    private val bio: String? = null,
    private val birthDate: LocalDate? = null,
    private val gender: Gender? = null,
    private val phoneNumber: String? = null,
    private val address: String? = null,
    private val website: String? = null,
    private val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        // 昵称长度限制
        private const val NICKNAME_MIN_LENGTH = 2
        private const val NICKNAME_MAX_LENGTH = 50
        
        // 简介长度限制
        private const val BIO_MAX_LENGTH = 500
        
        // 姓名长度限制
        private const val NAME_MAX_LENGTH = 50
        
        // 地址长度限制
        private const val ADDRESS_MAX_LENGTH = 200
        
        // 网站URL长度限制
        private const val WEBSITE_MAX_LENGTH = 200
        
        // 手机号正则表达式（简化版，支持中国大陆手机号）
        private val PHONE_PATTERN = Regex("^1[3-9]\\d{9}$")
        
        // 网站URL正则表达式（简化版）
        private val WEBSITE_PATTERN = Regex("^https?://[\\w.-]+\\.[a-zA-Z]{2,}(/.*)?$")
        
        /**
         * 创建用户资料
         * @param nickname 昵称（必填）
         * @return UserProfile实例
         */
        fun of(nickname: String): UserProfile {
            validateNickname(nickname)
            return UserProfile(nickname = nickname.trim())
        }
        
        /**
         * 创建完整用户资料
         */
        fun create(
            nickname: String,
            firstName: String? = null,
            lastName: String? = null,
            avatar: String? = null,
            bio: String? = null,
            birthDate: LocalDate? = null,
            gender: Gender? = null,
            phoneNumber: String? = null,
            address: String? = null,
            website: String? = null
        ): UserProfile {
            validateNickname(nickname)
            firstName?.let { validateName(it, "名字") }
            lastName?.let { validateName(it, "姓氏") }
            bio?.let { validateBio(it) }
            birthDate?.let { validateBirthDate(it) }
            phoneNumber?.let { validatePhoneNumber(it) }
            address?.let { validateAddress(it) }
            website?.let { validateWebsite(it) }
            
            return UserProfile(
                nickname = nickname.trim(),
                firstName = firstName?.trim(),
                lastName = lastName?.trim(),
                avatar = avatar?.trim(),
                bio = bio?.trim(),
                birthDate = birthDate,
                gender = gender,
                phoneNumber = phoneNumber?.trim(),
                address = address?.trim(),
                website = website?.trim()
            )
        }
        
        /**
         * 验证昵称
         */
        private fun validateNickname(nickname: String) {
            require(nickname.isNotBlank()) { "昵称不能为空" }
            val trimmed = nickname.trim()
            require(trimmed.length >= NICKNAME_MIN_LENGTH) { 
                "昵称长度不能少于${NICKNAME_MIN_LENGTH}个字符" 
            }
            require(trimmed.length <= NICKNAME_MAX_LENGTH) { 
                "昵称长度不能超过${NICKNAME_MAX_LENGTH}个字符" 
            }
        }
        
        /**
         * 验证姓名
         */
        private fun validateName(name: String, fieldName: String) {
            require(name.isNotBlank()) { "${fieldName}不能为空" }
            require(name.trim().length <= NAME_MAX_LENGTH) { 
                "${fieldName}长度不能超过${NAME_MAX_LENGTH}个字符" 
            }
        }
        
        /**
         * 验证简介
         */
        private fun validateBio(bio: String) {
            require(bio.trim().length <= BIO_MAX_LENGTH) { 
                "简介长度不能超过${BIO_MAX_LENGTH}个字符" 
            }
        }
        
        /**
         * 验证出生日期
         */
        private fun validateBirthDate(birthDate: LocalDate) {
            val now = LocalDate.now()
            require(!birthDate.isAfter(now)) { "出生日期不能是未来日期" }
            
            val age = Period.between(birthDate, now).years
            require(age <= 150) { "年龄不能超过150岁" }
        }
        
        /**
         * 验证手机号
         */
        private fun validatePhoneNumber(phoneNumber: String) {
            require(phoneNumber.isNotBlank()) { "手机号不能为空" }
            require(PHONE_PATTERN.matches(phoneNumber.trim())) { 
                "手机号格式不正确" 
            }
        }
        
        /**
         * 验证地址
         */
        private fun validateAddress(address: String) {
            require(address.trim().length <= ADDRESS_MAX_LENGTH) { 
                "地址长度不能超过${ADDRESS_MAX_LENGTH}个字符" 
            }
        }
        
        /**
         * 验证网站URL
         */
        private fun validateWebsite(website: String) {
            require(website.isNotBlank()) { "网站URL不能为空" }
            require(website.trim().length <= WEBSITE_MAX_LENGTH) { 
                "网站URL长度不能超过${WEBSITE_MAX_LENGTH}个字符" 
            }
            require(WEBSITE_PATTERN.matches(website.trim())) { 
                "网站URL格式不正确" 
            }
        }



        /**
         * 检测资料变更字段
         */
        fun detectProfileChanges(oldProfile: UserProfile, newProfile: UserProfile): Set<String> {
            val changes = mutableSetOf<String>()

            if (oldProfile.getNickname() != newProfile.getNickname()) changes.add("nickname")
            if (oldProfile.getFirstName() != newProfile.getFirstName()) changes.add("firstName")
            if (oldProfile.getLastName() != newProfile.getLastName()) changes.add("lastName")
            if (oldProfile.getAvatar() != newProfile.getAvatar()) changes.add("avatar")
            if (oldProfile.getBio() != newProfile.getBio()) changes.add("bio")
            if (oldProfile.getBirthDate() != newProfile.getBirthDate()) changes.add("birthDate")
            if (oldProfile.getGender() != newProfile.getGender()) changes.add("gender")
            if (oldProfile.getPhoneNumber() != newProfile.getPhoneNumber()) changes.add("phoneNumber")
            if (oldProfile.getAddress() != newProfile.getAddress()) changes.add("address")
            if (oldProfile.getWebsite() != newProfile.getWebsite()) changes.add("website")

            return changes
        }
    }
    
    /**
     * 更新昵称
     */
    fun updateNickname(nickname: String): UserProfile {
        validateNickname(nickname)
        return copy(nickname = nickname.trim(), updatedAt = LocalDateTime.now())
    }
    
    /**
     * 更新姓名
     */
    fun updateName(firstName: String?, lastName: String?): UserProfile {
        firstName?.let { validateName(it, "名字") }
        lastName?.let { validateName(it, "姓氏") }
        return copy(
            firstName = firstName?.trim(),
            lastName = lastName?.trim(),
            updatedAt = LocalDateTime.now()
        )
    }
    
    /**
     * 更新头像
     */
    fun updateAvatar(avatar: String?): UserProfile {
        return copy(avatar = avatar?.trim(), updatedAt = LocalDateTime.now())
    }
    
    /**
     * 更新简介
     */
    fun updateBio(bio: String?): UserProfile {
        bio?.let { validateBio(it) }
        return copy(bio = bio?.trim(), updatedAt = LocalDateTime.now())
    }
    
    /**
     * 更新出生日期
     */
    fun updateBirthDate(birthDate: LocalDate?): UserProfile {
        birthDate?.let { validateBirthDate(it) }
        return copy(birthDate = birthDate, updatedAt = LocalDateTime.now())
    }
    
    /**
     * 更新性别
     */
    fun updateGender(gender: Gender?): UserProfile {
        return copy(gender = gender, updatedAt = LocalDateTime.now())
    }
    
    /**
     * 更新手机号
     */
    fun updatePhoneNumber(phoneNumber: String?): UserProfile {
        phoneNumber?.let { validatePhoneNumber(it) }
        return copy(phoneNumber = phoneNumber?.trim(), updatedAt = LocalDateTime.now())
    }
    
    /**
     * 更新地址
     */
    fun updateAddress(address: String?): UserProfile {
        address?.let { validateAddress(it) }
        return copy(address = address?.trim(), updatedAt = LocalDateTime.now())
    }
    
    /**
     * 更新网站
     */
    fun updateWebsite(website: String?): UserProfile {
        website?.let { validateWebsite(it) }
        return copy(website = website?.trim(), updatedAt = LocalDateTime.now())
    }
    
    /**
     * 获取完整姓名
     */
    fun getFullName(): String {
        return when {
            firstName != null && lastName != null -> "$lastName $firstName"
            firstName != null -> firstName
            lastName != null -> lastName
            else -> nickname
        }
    }
    
    /**
     * 获取显示名称（优先使用完整姓名，否则使用昵称）
     */
    fun getDisplayName(): String {
        return if (firstName != null || lastName != null) {
            getFullName()
        } else {
            nickname
        }
    }
    
    /**
     * 计算年龄
     */
    fun getAge(): Int? {
        return birthDate?.let { 
            Period.between(it, LocalDate.now()).years
        }
    }
    
    /**
     * 检查资料是否完整
     */
    fun isComplete(): Boolean {
        return nickname.isNotBlank() && 
               firstName != null && 
               lastName != null && 
               birthDate != null && 
               gender != null
    }
    
    /**
     * 获取资料完整度百分比
     */
    fun getCompletionPercentage(): Int {
        var completed = 0
        var total = 0
        
        // 必填字段
        total++
        if (nickname.isNotBlank()) completed++
        
        // 可选字段
        total++
        if (firstName != null) completed++
        
        total++
        if (lastName != null) completed++
        
        total++
        if (avatar != null) completed++
        
        total++
        if (bio != null) completed++
        
        total++
        if (birthDate != null) completed++
        
        total++
        if (gender != null) completed++
        
        total++
        if (phoneNumber != null) completed++
        
        return (completed * 100) / total
    }
    
    // Getter方法
    fun getNickname(): String = nickname
    fun getFirstName(): String? = firstName
    fun getLastName(): String? = lastName
    fun getAvatar(): String? = avatar
    fun getBio(): String? = bio
    fun getBirthDate(): LocalDate? = birthDate
    fun getGender(): Gender? = gender
    fun getPhoneNumber(): String? = phoneNumber
    fun getAddress(): String? = address
    fun getWebsite(): String? = website
    fun getUpdatedAt(): LocalDateTime = updatedAt
    
    init {
        validateNickname(nickname)
        firstName?.let { validateName(it, "名字") }
        lastName?.let { validateName(it, "姓氏") }
        bio?.let { validateBio(it) }
        birthDate?.let { validateBirthDate(it) }
        phoneNumber?.let { validatePhoneNumber(it) }
        address?.let { validateAddress(it) }
        website?.let { validateWebsite(it) }
    }
}

/**
 * 性别枚举
 */
enum class Gender(val displayName: String) {
    MALE("男"),
    FEMALE("女"),
    OTHER("其他"),
    PREFER_NOT_TO_SAY("不愿透露")
}