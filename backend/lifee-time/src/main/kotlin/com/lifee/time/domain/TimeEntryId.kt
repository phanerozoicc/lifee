package com.lifee.time.domain

import com.lifee.common.domain.ValueObject
import java.util.*

/**
 * 时间条目ID值对象
 * 格式：T + 8位数字 (例如：T12345678)
 */
data class TimeEntryId(
    val value: String
) : ValueObject {

    init {
        require(isValid(value)) { "Invalid TimeEntryId format: $value" }
    }

    companion object {
        private const val PREFIX = "T"
        private const val ID_LENGTH = 9 // T + 8 digits
        private val ID_PATTERN = Regex("^T\\d{8}$")

        /**
         * 生成新的时间条目ID
         */
        fun generate(): TimeEntryId {
            val randomNumber = (10000000..99999999).random()
            return TimeEntryId("$PREFIX$randomNumber")
        }

        /**
         * 从字符串创建时间条目ID
         */
        fun of(value: String): TimeEntryId {
            return TimeEntryId(value)
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