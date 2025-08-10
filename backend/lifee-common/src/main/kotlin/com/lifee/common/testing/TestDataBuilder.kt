package com.lifee.common.testing

import java.time.Instant
import java.util.*

/**
 * 测试数据构建器基类
 * 提供构建测试数据的通用方法
 */
abstract class TestDataBuilder<T> {
    
    /**
     * 构建测试数据对象
     */
    abstract fun build(): T
    
    companion object {
        /**
         * 生成随机UUID
         */
        fun randomUuid(): UUID = UUID.randomUUID()
        
        /**
         * 生成随机字符串
         */
        fun randomString(length: Int = 10): String {
            val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
            return (1..length)
                .map { chars.random() }
                .joinToString("")
        }
        
        /**
         * 生成随机邮箱
         */
        fun randomEmail(): String {
            return "${randomString(8)}@${randomString(6)}.com"
        }
        
        /**
         * 生成随机整数
         */
        fun randomInt(min: Int = 1, max: Int = 1000): Int {
            return (min..max).random()
        }
        
        /**
         * 生成随机长整数
         */
        fun randomLong(min: Long = 1L, max: Long = 1000L): Long {
            return (min..max).random()
        }
        
        /**
         * 生成随机布尔值
         */
        fun randomBoolean(): Boolean = listOf(true, false).random()
        
        /**
         * 生成随机时间
         */
        fun randomInstant(): Instant {
            val now = Instant.now()
            val randomSeconds = randomLong(-86400, 86400) // ±1天
            return now.plusSeconds(randomSeconds)
        }
        
        /**
         * 从列表中随机选择一个元素
         */
        fun <T> randomFrom(items: List<T>): T {
            return items.random()
        }
        
        /**
         * 从可变参数中随机选择一个元素
         */
        fun <T> randomFrom(vararg items: T): T {
            return items.random()
        }
    }
}