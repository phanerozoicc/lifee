package com.github.phanerozoicc.user.domain

import java.util.*

/**
 * 用户仓储接口
 */
interface UserRepository {
    
    /**
 * 保存用户
     */
    fun save(user: User): User
    
    /**
     * 根据ID查找用户
     */
    fun findById(id: UserId): User?
    
    /**
     * 根据用户名查找用户
     */
    fun findByUsername(username: Username): User?
    
    /**
     * 根据邮箱查找用户
     */
    fun findByEmail(email: Email): User?
    
    /**
     * 检查用户名是否存在
     */
    fun existsByUsername(username: Username): Boolean
    
    /**
     * 检查邮箱是否存在
     */
    fun existsByEmail(email: Email): Boolean
    
    /**
     * 删除用户
     */
    fun delete(user: User)
    
    /**
     * 根据ID删除用户
     */
    fun deleteById(id: UserId)
    
    /**
     * 查找所有活跃用户
     */
    fun findAllActive(): List<User>
    
    /**
     * 分页查找用户
     */
    fun findAll(page: Int, size: Int): List<User>
    
    /**
     * 统计用户总数
     */
    fun count(): Long
}