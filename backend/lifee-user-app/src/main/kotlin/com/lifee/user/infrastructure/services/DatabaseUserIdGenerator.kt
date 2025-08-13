package com.lifee.user.infrastructure.services

import com.lifee.user.domain.UserId
import com.lifee.user.domain.services.UserIdGenerator
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 基于数据库序列的用户ID生成器实现
 */
@Service
class DatabaseUserIdGenerator(
    private val jdbcTemplate: JdbcTemplate
) : UserIdGenerator {
    
    companion object {
        private const val SEQUENCE_NAME = "user_id_seq"
        private const val GET_NEXT_VAL_SQL = "SELECT nextval('$SEQUENCE_NAME')"
        private const val GET_CURRENT_VAL_SQL = "SELECT currval('$SEQUENCE_NAME')"
    }
    
    /**
     * 生成下一个用户ID
     */
    @Transactional
    override fun generateNext(): UserId {
        val sequenceNumber = jdbcTemplate.queryForObject(GET_NEXT_VAL_SQL, Long::class.java)
            ?: throw IllegalStateException("无法获取序列号")
        
        return UserId.fromSequence(sequenceNumber)
    }
    
    /**
     * 获取当前序列号
     */
    override fun getCurrentSequence(): Long {
        return try {
            jdbcTemplate.queryForObject(GET_CURRENT_VAL_SQL, Long::class.java) ?: 0L
        } catch (e: Exception) {
            // 如果序列还没有被使用过，返回0
            0L
        }
    }
}