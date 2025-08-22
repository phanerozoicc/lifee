package com.lifee.time.domain.repository

import com.lifee.time.domain.Team
import com.lifee.time.domain.TeamId
import com.lifee.time.domain.TeamRole
import com.lifee.user.domain.UserId
import java.util.*

/**
 * 团队仓储接口
 */
interface TeamRepository {
    
    /**
     * 保存团队
     */
    suspend fun save(team: Team)
    
    /**
     * 根据ID查找团队
     */
    suspend fun findById(teamId: TeamId): Team?
    
    /**
     * 根据所有者ID查找团队列表
     */
    suspend fun findByOwnerId(ownerId: UserId, limit: Int = 50, offset: Int = 0): List<Team>
    
    /**
     * 查找用户所属的团队列表
     */
    suspend fun findByMemberId(userId: UserId, limit: Int = 50, offset: Int = 0): List<Team>
    
    /**
     * 根据名称搜索团队
     */
    suspend fun findByNameContaining(
        name: String,
        userId: UserId? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Team>
    
    /**
     * 查找活跃的团队
     */
    suspend fun findActiveTeams(
        userId: UserId? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Team>
    
    /**
     * 查找已停用的团队
     */
    suspend fun findInactiveTeams(
        userId: UserId? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Team>
    
    /**
     * 查找用户在指定角色下的团队
     */
    suspend fun findByMemberIdAndRole(
        userId: UserId,
        role: TeamRole,
        limit: Int = 50,
        offset: Int = 0
    ): List<Team>
    
    /**
     * 查找用户可管理的团队
     */
    suspend fun findManageableByUserId(
        userId: UserId,
        limit: Int = 50,
        offset: Int = 0
    ): List<Team>
    
    /**
     * 统计用户拥有的团队数量
     */
    suspend fun countByOwnerId(ownerId: UserId): Long
    
    /**
     * 统计用户所属的团队数量
     */
    suspend fun countByMemberId(userId: UserId): Long
    
    /**
     * 统计活跃团队数量
     */
    suspend fun countActiveTeams(): Long
    
    /**
     * 删除团队
     */
    suspend fun delete(teamId: TeamId)
    
    /**
     * 检查团队是否存在
     */
    suspend fun exists(teamId: TeamId): Boolean
    
    /**
     * 检查用户是否是团队成员
     */
    suspend fun isMember(teamId: TeamId, userId: UserId): Boolean
    
    /**
     * 检查团队名称是否已存在（在同一所有者下）
     */
    suspend fun existsByNameAndOwnerId(name: String, ownerId: UserId): Boolean
}