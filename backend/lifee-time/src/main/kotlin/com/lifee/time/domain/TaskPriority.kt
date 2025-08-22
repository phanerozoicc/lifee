package com.lifee.time.domain

/**
 * 任务优先级枚举
 */
enum class TaskPriority(val level: Int, val displayName: String) {
    /**
     * 低优先级
     */
    LOW(1, "低"),
    
    /**
     * 普通优先级
     */
    NORMAL(2, "普通"),
    
    /**
     * 高优先级
     */
    HIGH(3, "高"),
    
    /**
     * 紧急优先级
     */
    URGENT(4, "紧急");
    
    /**
     * 检查是否比另一个优先级高
     */
    fun isHigherThan(other: TaskPriority): Boolean {
        return this.level > other.level
    }
    
    /**
     * 检查是否比另一个优先级低
     */
    fun isLowerThan(other: TaskPriority): Boolean {
        return this.level < other.level
    }
    
    /**
     * 检查是否是高优先级（高或紧急）
     */
    fun isHighPriority(): Boolean {
        return this == HIGH || this == URGENT
    }
    
    /**
     * 检查是否是低优先级（低或普通）
     */
    fun isLowPriority(): Boolean {
        return this == LOW || this == NORMAL
    }
    
    companion object {
        /**
         * 根据级别获取优先级
         */
        fun fromLevel(level: Int): TaskPriority? {
            return values().find { it.level == level }
        }
        
        /**
         * 获取默认优先级
         */
        fun default(): TaskPriority {
            return NORMAL
        }
    }
}