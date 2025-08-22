package com.lifee.time.domain

import com.lifee.common.domain.ValueObject
import java.time.temporal.ChronoUnit

/**
 * 时间持续长度值对象
 * 以秒为单位存储持续时间
 */
data class Duration(
    val seconds: Long
) : ValueObject {

    init {
        require(seconds >= 0) { "Duration cannot be negative: $seconds" }
    }

    companion object {
        /**
         * 零持续时间
         */
        val ZERO = Duration(0)

        /**
         * 从秒数创建持续时间
         */
        fun ofSeconds(seconds: Long): Duration {
            return Duration(seconds)
        }

        /**
         * 从分钟数创建持续时间
         */
        fun ofMinutes(minutes: Long): Duration {
            return Duration(minutes * 60)
        }

        /**
         * 从小时数创建持续时间
         */
        fun ofHours(hours: Long): Duration {
            return Duration(hours * 3600)
        }

        /**
         * 从Java Duration创建
         */
        fun of(javaDuration: java.time.Duration): Duration {
            return Duration(javaDuration.seconds)
        }

        /**
         * 从两个时间点计算持续时间
         */
        fun between(start: java.time.Instant, end: java.time.Instant): Duration {
            val javaDuration = java.time.Duration.between(start, end)
            return Duration(javaDuration.seconds)
        }
    }

    /**
     * 获取分钟数
     */
    fun toMinutes(): Long = seconds / 60

    /**
     * 获取小时数
     */
    fun toHours(): Long = seconds / 3600

    /**
     * 获取天数
     */
    fun toDays(): Long = seconds / 86400

    /**
     * 转换为Java Duration
     */
    fun toJavaDuration(): java.time.Duration {
        return java.time.Duration.ofSeconds(seconds)
    }

    /**
     * 加法运算
     */
    fun plus(other: Duration): Duration {
        return Duration(this.seconds + other.seconds)
    }

    /**
     * 减法运算
     */
    fun minus(other: Duration): Duration {
        val result = this.seconds - other.seconds
        require(result >= 0) { "Duration cannot be negative after subtraction" }
        return Duration(result)
    }

    /**
     * 是否为零
     */
    fun isZero(): Boolean = seconds == 0L

    /**
     * 是否为正数
     */
    fun isPositive(): Boolean = seconds > 0L

    /**
     * 格式化为可读字符串 (HH:MM:SS)
     */
    fun toFormattedString(): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    override fun toString(): String = toFormattedString()
}