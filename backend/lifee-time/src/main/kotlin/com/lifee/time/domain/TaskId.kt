package com.lifee.time.domain

import com.lifee.common.domain.ValueObject
import java.util.*

/**
 * 任务ID值对象
 */
data class TaskId(val value: String) : ValueObject {
    
    init {
        require(isValid(value)) { "无效的任务ID格式: $value" }
    }
    
    companion object {
        private val TASK_ID_PATTERN = Regex("^T[0-9]{8}$")
        
        /**
         * 生成新的任务ID
         */
        fun generate(): TaskId {
            val randomNumber = Random().nextInt(100000000).toString().padStart(8, '0')
            return TaskId("T$randomNumber")
        }
        
        /**
         * 从字符串创建任务ID
         */
        fun of(value: String): TaskId {
            return TaskId(value)
        }
        
        /**
         * 验证任务ID格式
         */
        fun isValid(value: String): Boolean {
            return value.matches(TASK_ID_PATTERN)
        }
    }
    
    override fun toString(): String = value
}