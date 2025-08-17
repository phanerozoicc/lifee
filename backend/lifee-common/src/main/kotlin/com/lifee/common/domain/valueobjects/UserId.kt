package com.lifee.common.domain.valueobjects

import com.lifee.common.domain.ValueObject

/**
 * 通用用户ID值对象
 * 格式：U00000001 (U + 8位数字，不足8位前面补0)
 */
data class UserId(
    val value: String
) : ValueObject() {
    
    init {
        require(isValidFormat(value)) { "用户ID格式无效: $value，正确格式应为 U00000001" }
    }
    
    override fun getEqualityComponents(): List<Any?> {
        return listOf(value)
    }
    
    companion object {
        private const val PREFIX = "U"
        private const val ID_LENGTH = 9 // U + 8位数字
        
        /**
         * 验证用户ID格式
         */
        fun isValidFormat(value: String): Boolean {
            if (value.length != ID_LENGTH) return false
            if (!value.startsWith(PREFIX)) return false
            
            val numberPart = value.substring(1)
            return numberPart.all { it.isDigit() }
        }
        
        /**
         * 从字符串创建用户ID
         */
        fun fromString(value: String): UserId {
            return UserId(value)
        }
        
        /**
         * 从数字创建用户ID
         */
        fun fromNumber(number: Long): UserId {
            require(number > 0) { "用户ID数字必须大于0" }
            require(number <= 99999999) { "用户ID数字不能超过99999999" }
            
            val formattedNumber = number.toString().padStart(8, '0')
            return UserId(PREFIX + formattedNumber)
        }
        
        /**
         * 获取下一个用户ID
         */
        fun next(currentId: UserId): UserId {
            val currentNumber = currentId.getNumber()
            return fromNumber(currentNumber + 1)
        }
    }
    
    /**
     * 获取用户ID的数字部分
     */
    fun getNumber(): Long {
        return value.substring(1).toLong()
    }
    
    override fun toString(): String {
        return value
    }
}