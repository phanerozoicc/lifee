package com.lifee.user.domain

import java.util.*

/**
 * 用户仓储接口
 */
interface UserRepository {
    
    /**
     * 保存用户
     */
    suspend fun save(user: User): User
    
    /**
     * 根据ID查找用户
     */
    suspend fun findById(id: UserId): User?
    
    /**
     * 根据邮箱查找用户
     */
    suspend fun findByEmail(email: Email): User?
    
    /**
     * 检查邮箱是否已存在
     */
    suspend fun existsByEmail(email: Email): Boolean
    
    /**
     * 根据状态查找用户列表
     */
    suspend fun findByStatus(status: UserStatus, limit: Int = 100, offset: Int = 0): List<User>
    
    /**
     * 查找需要激活的用户（注册超过指定时间但未激活）
     */
    suspend fun findPendingActivationUsers(olderThanHours: Int): List<User>
    
    /**
     * 统计用户数量
     */
    suspend fun countByStatus(status: UserStatus): Long
    
    /**
     * 删除用户（物理删除，谨慎使用）
     */
    suspend fun delete(user: User)
    
    /**
     * 根据ID删除用户（物理删除，谨慎使用）
     */
    suspend fun deleteById(id: UserId)
}