package com.lifee.time.domain

/**
 * 任务状态枚举
 */
enum class TaskStatus {
    /**
     * 待办
     */
    TODO,
    
    /**
     * 进行中
     */
    IN_PROGRESS,
    
    /**
     * 已完成
     */
    COMPLETED,
    
    /**
     * 已取消
     */
    CANCELLED;
    
    /**
     * 检查是否可以开始任务
     */
    fun canStart(): Boolean {
        return this == TODO
    }
    
    /**
     * 检查是否可以完成任务
     */
    fun canComplete(): Boolean {
        return this == IN_PROGRESS
    }
    
    /**
     * 检查是否可以取消任务
     */
    fun canCancel(): Boolean {
        return this == TODO || this == IN_PROGRESS
    }
    
    /**
     * 检查是否可以重新开始任务
     */
    fun canRestart(): Boolean {
        return this == COMPLETED || this == CANCELLED
    }
    
    /**
     * 检查任务是否处于活跃状态
     */
    fun isActive(): Boolean {
        return this == TODO || this == IN_PROGRESS
    }
    
    /**
     * 检查任务是否已结束
     */
    fun isFinished(): Boolean {
        return this == COMPLETED || this == CANCELLED
    }
    
    /**
     * 检查任务是否可以记录时间
     */
    fun canTrackTime(): Boolean {
        return this == IN_PROGRESS
    }
}