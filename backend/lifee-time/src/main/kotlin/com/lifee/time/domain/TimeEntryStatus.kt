package com.lifee.time.domain

/**
 * 时间条目状态枚举
 */
enum class TimeEntryStatus {
    /**
     * 正在进行中
     */
    RUNNING,

    /**
     * 已停止
     */
    STOPPED,

    /**
     * 已暂停
     */
    PAUSED,

    /**
     * 已删除
     */
    DELETED;

    /**
     * 是否可以停止
     */
    fun canStop(): Boolean {
        return this == RUNNING || this == PAUSED
    }

    /**
     * 是否可以暂停
     */
    fun canPause(): Boolean {
        return this == RUNNING
    }

    /**
     * 是否可以恢复
     */
    fun canResume(): Boolean {
        return this == PAUSED
    }

    /**
     * 是否可以删除
     */
    fun canDelete(): Boolean {
        return this != DELETED
    }

    /**
     * 是否正在活跃（运行或暂停）
     */
    fun isActive(): Boolean {
        return this == RUNNING || this == PAUSED
    }

    /**
     * 是否已完成
     */
    fun isCompleted(): Boolean {
        return this == STOPPED
    }

    /**
     * 是否已删除
     */
    fun isDeleted(): Boolean {
        return this == DELETED
    }
}