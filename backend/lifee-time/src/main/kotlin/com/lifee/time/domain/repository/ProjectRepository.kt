package com.lifee.time.domain.repository

import com.lifee.time.domain.Project
import com.lifee.time.domain.ProjectId
import com.lifee.time.domain.TeamId
import com.lifee.user.domain.UserId
import java.util.*

/**
 * 项目仓储接口
 */
interface ProjectRepository {
    
    /**
     * 保存项目
     */
    suspend fun save(project: Project)
    
    /**
     * 根据ID查找项目
     */
    suspend fun findById(projectId: ProjectId): Project?
    
    /**
     * 根据所有者ID查找项目列表
     */
    suspend fun findByOwnerId(ownerId: UserId, limit: Int = 50, offset: Int = 0): List<Project>
    
    /**
     * 根据团队ID查找项目列表
     */
    suspend fun findByTeamId(teamId: TeamId, limit: Int = 50, offset: Int = 0): List<Project>
    
    /**
     * 查找用户可访问的项目列表
     */
    suspend fun findAccessibleByUserId(userId: UserId, limit: Int = 50, offset: Int = 0): List<Project>
    
    /**
     * 根据名称搜索项目
     */
    suspend fun findByNameContaining(
        name: String,
        userId: UserId? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Project>
    
    /**
     * 查找活跃的项目
     */
    suspend fun findActiveProjects(
        userId: UserId? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Project>
    
    /**
     * 查找已归档的项目
     */
    suspend fun findArchivedProjects(
        userId: UserId? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Project>
    
    /**
     * 根据客户名称查找项目
     */
    suspend fun findByClientName(
        clientName: String,
        userId: UserId? = null,
        limit: Int = 50,
        offset: Int = 0
    ): List<Project>
    
    /**
     * 统计用户拥有的项目数量
     */
    suspend fun countByOwnerId(ownerId: UserId): Long
    
    /**
     * 统计团队的项目数量
     */
    suspend fun countByTeamId(teamId: TeamId): Long
    
    /**
     * 统计用户可访问的项目数量
     */
    suspend fun countAccessibleByUserId(userId: UserId): Long
    
    /**
     * 删除项目
     */
    suspend fun delete(projectId: ProjectId)
    
    /**
     * 检查项目是否存在
     */
    suspend fun exists(projectId: ProjectId): Boolean
    
    /**
     * 检查项目名称是否已存在（在同一所有者下）
     */
    suspend fun existsByNameAndOwnerId(name: String, ownerId: UserId): Boolean
}