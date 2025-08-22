package com.lifee.time.domain

import com.lifee.common.domain.ValueObject

/**
 * 项目ID值对象
 * 格式：P + 8位数字 (例如：P12345678)
 */
data class ProjectId(
    val value: String
) : ValueObject {

    init {
        require(isValid(value)) { "Invalid ProjectId format: $value" }
    }

    companion object {
        private const val PREFIX = "P"
        private const val ID_LENGTH = 9 // P + 8 digits
        private val ID_PATTERN = Regex("^P\\d{8}$")

        /**
         * 生成新的项目ID
         */
        fun generate(): ProjectId {
            val randomNumber = (10000000..99999999).random()
            return ProjectId("$PREFIX$randomNumber")
        }

        /**
         * 从字符串创建项目ID
         */
        fun of(value: String): ProjectId {
            return ProjectId(value)
        }

        /**
         * 验证ID格式是否有效
         */
        fun isValid(value: String): Boolean {
            return value.length == ID_LENGTH && ID_PATTERN.matches(value)
        }
    }

    override fun toString(): String = value
}