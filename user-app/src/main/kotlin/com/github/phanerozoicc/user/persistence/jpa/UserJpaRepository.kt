package com.github.phanerozoicc.infrastructure.persistence.jpa

import com.github.phanerozoicc.infrastructure.persistence.entity.UserEntity
import com.github.phanerozoicc.user.domain.UserStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

/**
 * 用户JPA仓储
 */
@Repository
interface UserJpaRepository : JpaRepository<UserEntity, String> {
    
    /**
     * 根据用户名查找用户
     */
    fun findByUsername(username: String): UserEntity?
    
    /**
     * 根据邮箱查找用户
     */
    fun findByEmail(email: String): UserEntity?
    
    /**
     * 检查用户名是否存在
     */
    fun existsByUsername(username: String): Boolean
    
    /**
     * 检查邮箱是否存在
     */
    fun existsByEmail(email: String): Boolean
    
    /**
     * 根据状态查找用户
     */
    fun findByStatus(status: UserStatus): List<UserEntity>
    
    /**
     * 根据状态分页查找用户
     */
    fun findByStatus(status: UserStatus, pageable: Pageable): Page<UserEntity>
    
    /**
     * 查找活跃用户（分页）
     */
    @Query("SELECT u FROM UserEntity u WHERE u.status = 'ACTIVE' ORDER BY u.createdAt DESC")
    fun findActiveUsers(pageable: Pageable): Page<UserEntity>
    
    /**
     * 根据用户名模糊查询
     */
    @Query("SELECT u FROM UserEntity u WHERE u.username LIKE %:username% AND u.status = :status")
    fun findByUsernameContainingAndStatus(
        @Param("username") username: String,
        @Param("status") status: UserStatus,
        pageable: Pageable
    ): Page<UserEntity>
    
    /**
     * 根据邮箱模糊查询
     */
    @Query("SELECT u FROM UserEntity u WHERE u.email LIKE %:email% AND u.status = :status")
    fun findByEmailContainingAndStatus(
        @Param("email") email: String,
        @Param("status") status: UserStatus,
        pageable: Pageable
    ): Page<UserEntity>
    
    /**
     * 统计指定状态的用户数量
     */
    fun countByStatus(status: UserStatus): Long
    
    /**
     * 查找最近注册的用户
     */
    @Query("SELECT u FROM UserEntity u WHERE u.status = 'ACTIVE' ORDER BY u.createdAt DESC")
    fun findRecentlyRegisteredUsers(pageable: Pageable): Page<UserEntity>
    
    /**
     * 根据创建时间范围查找用户
     */
    @Query(
        "SELECT u FROM UserEntity u WHERE u.createdAt >= :startDate AND u.createdAt <= :endDate ORDER BY u.createdAt DESC"
    )
    fun findByCreatedAtBetween(
        @Param("startDate") startDate: java.time.LocalDateTime,
        @Param("endDate") endDate: java.time.LocalDateTime,
        pageable: Pageable
    ): Page<UserEntity>
}