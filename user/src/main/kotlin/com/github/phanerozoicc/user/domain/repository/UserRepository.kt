package com.github.phanerozoicc.user.domain.repository

import com.github.phanerozoicc.user.domain.model.*
import java.time.LocalDateTime

/**
 * 用户仓储接口
 * 定义用户聚合根的持久化操作
 */
interface UserRepository {
    
    /**
     * 保存用户
     * @param user 用户聚合根
     * @return 保存后的用户
     */
    fun save(user: User): User
    
    /**
     * 根据ID查找用户
     * @param userId 用户ID
     * @return 用户实例，如果不存在则返回null
     */
    fun findById(userId: UserId): User?
    
    /**
     * 根据邮箱查找用户
     * @param email 邮箱地址
     * @return 用户实例，如果不存在则返回null
     */
    fun findByEmail(email: Email): User?
    
    /**
     * 根据昵称查找用户
     * @param nickname 昵称
     * @return 用户实例，如果不存在则返回null
     */
    fun findByNickname(nickname: String): User?
    
    /**
     * 检查邮箱是否已存在
     * @param email 邮箱地址
     * @return 如果存在返回true，否则返回false
     */
    fun existsByEmail(email: Email): Boolean
    
    /**
     * 检查昵称是否已存在
     * @param nickname 昵称
     * @return 如果存在返回true，否则返回false
     */
    fun existsByNickname(nickname: String): Boolean
    
    /**
     * 检查昵称是否已存在（排除指定用户）
     * @param nickname 昵称
     * @param excludeUserId 要排除的用户ID
     * @return 如果存在返回true，否则返回false
     */
    fun existsByNicknameExcluding(nickname: String, excludeUserId: UserId): Boolean
    
    /**
     * 根据状态查找用户列表
     * @param status 用户状态
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 用户列表
     */
    fun findByStatus(status: UserStatus, limit: Int = 50, offset: Int = 0): List<User>
    
    /**
     * 查找在指定时间之前创建的用户
     * @param createdBefore 创建时间上限
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 用户列表
     */
    fun findCreatedBefore(createdBefore: LocalDateTime, limit: Int = 50, offset: Int = 0): List<User>
    
    /**
     * 查找在指定时间之后最后登录的用户
     * @param lastLoginAfter 最后登录时间下限
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 用户列表
     */
    fun findLastLoginAfter(lastLoginAfter: LocalDateTime, limit: Int = 50, offset: Int = 0): List<User>
    
    /**
     * 查找未验证邮箱的用户
     * @param createdBefore 创建时间上限（可选）
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 用户列表
     */
    fun findUnverifiedUsers(
        createdBefore: LocalDateTime? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<User>
    
    /**
     * 查找需要密码更新的用户
     * @param passwordCreatedBefore 密码创建时间上限
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 用户列表
     */
    fun findUsersNeedingPasswordUpdate(
        passwordCreatedBefore: LocalDateTime,
        limit: Int = 50,
        offset: Int = 0
    ): List<User>
    
    /**
     * 统计用户总数
     * @return 用户总数
     */
    fun count(): Long
    
    /**
     * 根据状态统计用户数量
     * @param status 用户状态
     * @return 指定状态的用户数量
     */
    fun countByStatus(status: UserStatus): Long
    
    /**
     * 统计在指定时间范围内注册的用户数量
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 注册用户数量
     */
    fun countRegisteredBetween(startDate: LocalDateTime, endDate: LocalDateTime): Long
    
    /**
     * 统计在指定时间范围内活跃的用户数量
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 活跃用户数量
     */
    fun countActiveUsersBetween(startDate: LocalDateTime, endDate: LocalDateTime): Long
    
    /**
     * 删除用户
     * @param userId 用户ID
     */
    fun delete(userId: UserId)
    
    /**
     * 批量删除用户
     * @param userIds 用户ID列表
     */
    fun deleteAll(userIds: List<UserId>)
    
    /**
     * 查找所有用户（分页）
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 用户列表
     */
    fun findAll(limit: Int = 50, offset: Int = 0): List<User>
    
    /**
     * 根据关键词搜索用户
     * @param keyword 搜索关键词（匹配昵称、姓名、邮箱）
     * @param limit 限制数量
     * @param offset 偏移量
     * @return 用户列表
     */
    fun searchUsers(keyword: String, limit: Int = 50, offset: Int = 0): List<User>
    
    /**
     * 根据多个条件查找用户
     * @param criteria 查询条件
     * @return 用户列表
     */
    fun findByCriteria(criteria: UserSearchCriteria): List<User>
}

/**
 * 用户搜索条件
 */
data class UserSearchCriteria(
    val keyword: String? = null,
    val status: UserStatus? = null,
    val emailVerified: Boolean? = null,
    val createdAfter: LocalDateTime? = null,
    val createdBefore: LocalDateTime? = null,
    val lastLoginAfter: LocalDateTime? = null,
    val lastLoginBefore: LocalDateTime? = null,
    val limit: Int = 50,
    val offset: Int = 0,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC"
)