package com.lifee.time.domain

import com.lifee.common.domain.ValueObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 时间范围值对象
 * 表示一个时间段，包含开始时间和结束时间
 */
data class TimeRange(
    val startTime: Instant,
    val endTime: Instant?
) : ValueObject {

    init {
        if (endTime != null) {
            require(!startTime.isAfter(endTime)) { 
                "Start time cannot be after end time: $startTime > $endTime" 
            }
        }
    }

    companion object {
        /**
         * 创建一个正在进行的时间范围（没有结束时间）
         */
        fun ongoing(startTime: Instant): TimeRange {
            return TimeRange(startTime, null)
        }

        /**
         * 创建一个已完成的时间范围
         */
        fun completed(startTime: Instant, endTime: Instant): TimeRange {
            return TimeRange(startTime, endTime)
        }

        /**
         * 创建今天的时间范围
         */
        fun today(zoneId: ZoneId = ZoneId.systemDefault()): TimeRange {
            val today = LocalDate.now(zoneId)
            val startOfDay = today.atStartOfDay(zoneId).toInstant()
            val endOfDay = today.plusDays(1).atStartOfDay(zoneId).toInstant()
            return TimeRange(startOfDay, endOfDay)
        }

        /**
         * 创建本周的时间范围
         */
        fun thisWeek(zoneId: ZoneId = ZoneId.systemDefault()): TimeRange {
            val today = LocalDate.now(zoneId)
            val startOfWeek = today.minusDays(today.dayOfWeek.value - 1L)
            val endOfWeek = startOfWeek.plusDays(7)
            return TimeRange(
                startOfWeek.atStartOfDay(zoneId).toInstant(),
                endOfWeek.atStartOfDay(zoneId).toInstant()
            )
        }

        /**
         * 创建本月的时间范围
         */
        fun thisMonth(zoneId: ZoneId = ZoneId.systemDefault()): TimeRange {
            val today = LocalDate.now(zoneId)
            val startOfMonth = today.withDayOfMonth(1)
            val endOfMonth = startOfMonth.plusMonths(1)
            return TimeRange(
                startOfMonth.atStartOfDay(zoneId).toInstant(),
                endOfMonth.atStartOfDay(zoneId).toInstant()
            )
        }
    }

    /**
     * 是否正在进行中（没有结束时间）
     */
    fun isOngoing(): Boolean = endTime == null

    /**
     * 是否已完成
     */
    fun isCompleted(): Boolean = endTime != null

    /**
     * 获取持续时间
     */
    fun getDuration(): Duration? {
        return if (endTime != null) {
            Duration.between(startTime, endTime)
        } else {
            null
        }
    }

    /**
     * 获取到当前时间的持续时间（用于正在进行的时间范围）
     */
    fun getDurationUntilNow(): Duration {
        val currentTime = endTime ?: Instant.now()
        return Duration.between(startTime, currentTime)
    }

    /**
     * 结束时间范围
     */
    fun end(endTime: Instant): TimeRange {
        require(!startTime.isAfter(endTime)) { 
            "End time cannot be before start time: $endTime < $startTime" 
        }
        return TimeRange(startTime, endTime)
    }

    /**
     * 检查是否包含指定时间点
     */
    fun contains(instant: Instant): Boolean {
        val afterStart = !instant.isBefore(startTime)
        val beforeEnd = endTime?.let { !instant.isAfter(it) } ?: true
        return afterStart && beforeEnd
    }

    /**
     * 检查是否与另一个时间范围重叠
     */
    fun overlaps(other: TimeRange): Boolean {
        val thisEnd = this.endTime ?: Instant.now()
        val otherEnd = other.endTime ?: Instant.now()
        
        return !this.startTime.isAfter(otherEnd) && !other.startTime.isAfter(thisEnd)
    }

    /**
     * 格式化为可读字符串
     */
    fun toFormattedString(zoneId: ZoneId = ZoneId.systemDefault()): String {
        val start = startTime.atZone(zoneId)
        return if (endTime != null) {
            val end = endTime.atZone(zoneId)
            "${start.toLocalDateTime()} - ${end.toLocalDateTime()}"
        } else {
            "${start.toLocalDateTime()} - (ongoing)"
        }
    }

    override fun toString(): String = toFormattedString()
}