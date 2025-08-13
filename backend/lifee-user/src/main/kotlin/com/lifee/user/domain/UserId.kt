package com.lifee.user.domain

import com.lifee.common.domain.ValueObject

/**
 * 用户ID值对象
 * 格式：U00000001 (U + 8位数字，不足8位前面补0)
 */
data class UserId(
    val value: String
) : ValueObject() {
    
    init {
        require(isValidFormat(value)) { "用户ID格式无效: $value，正确格式应为 U00000001" }
    }
    
    companion object {
        private const val PREFIX = "U"
        private const val ID_LENGTH = 9 // U + 8位数字
        private val ID_PATTERN = Regex("^U\\d{8}$")
        
        /**
         * 生成新的用户ID
         * 注意：这个方法需要配合UserIdGenerator服务使用
         */
        fun generate(sequenceNumber: Long): UserId {
            val paddedNumber = sequenceNumber.toString().padStart(8, '0')
            return UserId("$PREFIX$paddedNumber")
        }
        
        /**
         * 从字符串创建用户ID
         */
        fun fromString(value: String): UserId {
            return UserId(value)
        }
        
        /**
         * 验证ID格式是否正确
         */
        private fun isValidFormat(value: String): Boolean {
            return value.length == ID_LENGTH && ID_PATTERN.matches(value)
        }
        
        /**
         * 从数字序列号创建用户ID
         */
        fun fromSequence(sequenceNumber: Long): UserId {
            require(sequenceNumber > 0) { "序列号必须大于0" }
            require(sequenceNumber <= 99999999) { "序列号不能超过99999999" }
            return generate(sequenceNumber)
        }
    }
    
    /**
     * 获取数字部分
     */
    fun getSequenceNumber(): Long {
        return value.substring(1).toLong()
    }
    
    override fun getEqualityComponents(): List<Any> {
        return listOf(value)
    }
    
    override fun toString(): String {
        return value
    }
}