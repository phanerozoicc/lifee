package com.lifee.user.infrastructure.services

import com.lifee.user.domain.UserId
import com.lifee.user.domain.services.UserIdGenerator
import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

/**
 * 基于Redis的用户ID生成器实现
 * 使用Redisson的原子递增功能确保分布式环境下的ID唯一性
 */
@Service
class RedisUserIdGenerator(
    private val redissonClient: RedissonClient
) : UserIdGenerator {
    
    companion object {
        private const val USER_ID_COUNTER_KEY = "lifee:user:id:counter"
        private val logger = LoggerFactory.getLogger(RedisUserIdGenerator::class.java)
    }
    
    /**
     * 生成下一个用户ID
     * 使用Redis原子递增确保分布式环境下的唯一性
     */
    override fun generateNext(): UserId {
        try {
            val atomicLong = redissonClient.getAtomicLong(USER_ID_COUNTER_KEY)
            val sequenceNumber = atomicLong.incrementAndGet()
            
            logger.debug("生成新的用户ID序列号: {}", sequenceNumber)
            
            return UserId.fromSequence(sequenceNumber)
        } catch (e: Exception) {
            logger.error("生成用户ID失败", e)
            throw IllegalStateException("无法生成用户ID: ${e.message}", e)
        }
    }
    
    /**
     * 获取当前序列号
     */
    override fun getCurrentSequence(): Long {
        return try {
            val atomicLong = redissonClient.getAtomicLong(USER_ID_COUNTER_KEY)
            atomicLong.get()
        } catch (e: Exception) {
            logger.warn("获取当前序列号失败: {}", e.message)
            0L
        }
    }
    
    /**
     * 初始化计数器（如果需要从特定值开始）
     * @param initialValue 初始值
     */
    fun initializeCounter(initialValue: Long) {
        try {
            val atomicLong = redissonClient.getAtomicLong(USER_ID_COUNTER_KEY)
            if (atomicLong.get() == 0L) {
                atomicLong.set(initialValue)
                logger.info("初始化用户ID计数器为: {}", initialValue)
            }
        } catch (e: Exception) {
            logger.error("初始化用户ID计数器失败", e)
        }
    }
}