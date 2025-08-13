package com.lifee.user.infrastructure.services

import org.redisson.api.RedissonClient
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 用户ID迁移服务
 * 负责将数据库序列的当前值同步到Redis计数器
 */
@Service
class UserIdMigrationService(
    private val jdbcTemplate: JdbcTemplate,
    private val redissonClient: RedissonClient
) {
    
    companion object {
        private const val USER_ID_COUNTER_KEY = "lifee:user:id:counter"
        private const val SEQUENCE_NAME = "user_id_seq"
        private const val GET_CURRENT_VAL_SQL = "SELECT currval('$SEQUENCE_NAME')"
        private const val CHECK_SEQUENCE_SQL = "SELECT last_value FROM $SEQUENCE_NAME"
        private val logger = LoggerFactory.getLogger(UserIdMigrationService::class.java)
    }
    
    /**
     * 应用启动后自动执行迁移
     */
    @EventListener(ApplicationReadyEvent::class)
    @Transactional(readOnly = true)
    fun migrateSequenceToRedis() {
        try {
            logger.info("开始迁移用户ID序列到Redis...")
            
            // 检查Redis计数器是否已经初始化
            val atomicLong = redissonClient.getAtomicLong(USER_ID_COUNTER_KEY)
            val currentRedisValue = atomicLong.get()
            
            if (currentRedisValue > 0) {
                logger.info("Redis计数器已存在，当前值: {}", currentRedisValue)
                return
            }
            
            // 获取数据库序列的当前值
            val sequenceValue = getDatabaseSequenceValue()
            
            if (sequenceValue > 0) {
                // 将序列值设置到Redis
                atomicLong.set(sequenceValue)
                logger.info("成功将数据库序列值 {} 迁移到Redis计数器", sequenceValue)
            } else {
                // 如果数据库序列还没有被使用，初始化为0
                atomicLong.set(0L)
                logger.info("数据库序列未使用，Redis计数器初始化为0")
            }
            
        } catch (e: Exception) {
            logger.error("迁移用户ID序列到Redis失败", e)
            // 不抛出异常，避免影响应用启动
        }
    }
    
    /**
     * 获取数据库序列的当前值
     */
    private fun getDatabaseSequenceValue(): Long {
        return try {
            // 首先尝试获取序列的last_value
            jdbcTemplate.queryForObject(CHECK_SEQUENCE_SQL, Long::class.java) ?: 0L
        } catch (e: Exception) {
            logger.warn("无法获取数据库序列值: {}", e.message)
            0L
        }
    }
    
    /**
     * 手动同步序列值到Redis（用于维护）
     */
    fun syncSequenceToRedis(): Long {
        val sequenceValue = getDatabaseSequenceValue()
        val atomicLong = redissonClient.getAtomicLong(USER_ID_COUNTER_KEY)
        atomicLong.set(sequenceValue)
        logger.info("手动同步序列值 {} 到Redis", sequenceValue)
        return sequenceValue
    }
    
    /**
     * 获取当前Redis计数器值
     */
    fun getCurrentRedisCounter(): Long {
        val atomicLong = redissonClient.getAtomicLong(USER_ID_COUNTER_KEY)
        return atomicLong.get()
    }
}