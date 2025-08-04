package com.github.phanerozoicc.user.domain.model

import java.util.concurrent.atomic.AtomicLong

/**
 * 用户ID值对象
 * 使用自增ID字符串格式（如 USER_000001）
 */
data class UserId(
    private val value: String
) {
    companion object {
        private val counter = AtomicLong(0)
        private const val PREFIX = "USER_"
        private const val ID_FORMAT = "%06d"
        
        /**
         * 从字符串创建用户ID
         * @param value 用户ID字符串值
         * @return UserId实例
         * @throws IllegalArgumentException 如果格式不正确
         */
        fun of(value: String): UserId {
            require(value.isNotBlank()) { "用户ID不能为空" }
            require(value.startsWith(PREFIX)) { "用户ID必须以${PREFIX}开头" }
            require(value.length == PREFIX.length + 6) { "用户ID格式不正确，应为${PREFIX}XXXXXX" }
            return UserId(value)
        }
        
        /**
         * 生成新的自增用户ID
         * @return 新的UserId实例
         */
        fun generate(): UserId {
            val nextId = counter.incrementAndGet()
            val formattedId = PREFIX + ID_FORMAT.format(nextId)
            return UserId(formattedId)
        }
        
        /**
         * 重置计数器（仅用于测试）
         */
        internal fun resetCounter() {
            counter.set(0)
        }
    }
    
    /**
     * 获取ID值
     */
    fun getValue(): String = value
    
    /**
     * 转换为字符串
     */
    override fun toString(): String = value
    
    init {
        require(value.isNotBlank()) { "用户ID不能为空" }
        require(value.startsWith(PREFIX)) { "用户ID必须以${PREFIX}开头" }
    }
}