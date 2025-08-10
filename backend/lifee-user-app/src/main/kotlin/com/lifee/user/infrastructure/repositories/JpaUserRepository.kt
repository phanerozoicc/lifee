package com.lifee.user.infrastructure.repositories

import com.lifee.user.domain.UserStatus
import com.lifee.user.infrastructure.entities.UserEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

/**
 * JPA用户仓储接口
 */
@Repository
interface JpaUserRepository : JpaRepository<UserEntity, String> {
    
    /**
     * 根据邮箱查找用户
     */
    fun findByEmail(email: String): UserEntity?
    
    /**
     * 检查邮箱是否存在
     */
    fun existsByEmail(email: String): Boolean
    
    /**
     * 根据状态查找用户
     */
    fun findByStatusOrderByCreatedAtDesc(
        status: UserStatus, 
        pageable: org.springframework.data.domain.Pageable
    ): List<UserEntity>
    
    /**
     * 查找需要激活的用户（注册超过指定时间但未激活）
     */
    @Query("""
        SELECT u FROM UserEntity u 
        WHERE u.status = 'PENDING' 
        AND u.createdAt < :cutoffTime
        ORDER BY u.createdAt ASC
    """)
    fun findPendingActivationUsers(@Param("cutoffTime") cutoffTime: LocalDateTime): List<UserEntity>
    
    /**
     * 统计指定状态的用户数量
     */
    fun countByStatus(status: UserStatus): Long
}