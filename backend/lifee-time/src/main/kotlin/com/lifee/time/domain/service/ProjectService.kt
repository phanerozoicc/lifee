package com.lifee.time.domain.service

import com.lifee.time.domain.*
import com.lifee.time.domain.repository.ProjectRepository
import com.lifee.time.domain.repository.TeamRepository
import com.lifee.time.domain.repository.TimeEntryRepository
import com.lifee.user.domain.UserId
import java.math.BigDecimal

/**
 * 项目管理领域服务
 */
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val teamRepository: TeamRepository,
    private val timeEntryRepository: TimeEntryRepository
) {
    
    /**
     * 创建项目
     */
    suspend fun createProject(
        name: String,
        description: String?,
        ownerId: UserId,
        teamId: TeamId? = null,
        clientName: String? = null,
        hourlyRate: BigDecimal? = null,
        color: String? = null,
        isPublic: Boolean = false
    ): Project {
        // 检查项目名称是否已存在
        if (projectRepository.existsByNameAndOwnerId(name, ownerId)) {
            throw IllegalArgumentException("项目名称已存在: $name")
        }
        
        // 验证团队访问权限
        if (teamId != null) {
            val team = teamRepository.findById(teamId)
                ?: throw IllegalArgumentException("团队不存在: $teamId")
            
            if (!team.canManageProjects(ownerId)) {
                throw IllegalArgumentException("用户无权在此团队创建项目")
            }
        }
        
        // 创建项目
        val project = Project.create(
            name = name,
            description = description,
            ownerId = ownerId,
            teamId = teamId,
            clientName = clientName,
            hourlyRate = hourlyRate,
            color = color,
            isPublic = isPublic
        )
        
        // 保存项目
        projectRepository.save(project)
        
        return project
    }
    
    /**
     * 更新项目信息
     */
    suspend fun updateProject(
        projectId: ProjectId,
        userId: UserId,
        name: String? = null,
        description: String? = null,
        clientName: String? = null,
        hourlyRate: BigDecimal? = null,
        color: String? = null,
        isPublic: Boolean? = null
    ): Project {
        val project = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("项目不存在: $projectId")
        
        if (!project.canEdit(userId)) {
            throw IllegalArgumentException("用户无权编辑此项目")
        }
        
        // 检查项目名称是否已存在（如果要更新名称）
        if (name != null && name != project.name) {
            if (projectRepository.existsByNameAndOwnerId(name, project.ownerId)) {
                throw IllegalArgumentException("项目名称已存在: $name")
            }
        }
        
        // 更新项目信息
        project.updateInfo(
            name = name ?: project.name,
            description = description ?: project.description,
            clientName = clientName ?: project.clientName,
            hourlyRate = hourlyRate ?: project.hourlyRate,
            color = color ?: project.color,
            isPublic = isPublic ?: project.isPublic
        )
        
        projectRepository.save(project)
        
        return project
    }
    
    /**
     * 添加项目成员
     */
    suspend fun addMember(
        projectId: ProjectId,
        userId: UserId,
        memberId: UserId,
        role: ProjectRole = ProjectRole.MEMBER
    ): Project {
        val project = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("项目不存在: $projectId")
        
        if (!project.canManageMembers(userId)) {
            throw IllegalArgumentException("用户无权管理项目成员")
        }
        
        // 验证团队成员权限（如果项目属于团队）
        if (project.teamId != null) {
            val team = teamRepository.findById(project.teamId!!)
                ?: throw IllegalArgumentException("团队不存在: ${project.teamId}")
            
            if (!team.isMember(memberId)) {
                throw IllegalArgumentException("用户不是团队成员，无法添加到项目")
            }
        }
        
        project.addMember(memberId, role)
        projectRepository.save(project)
        
        return project
    }
    
    /**
     * 移除项目成员
     */
    suspend fun removeMember(
        projectId: ProjectId,
        userId: UserId,
        memberId: UserId
    ): Project {
        val project = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("项目不存在: $projectId")
        
        if (!project.canManageMembers(userId)) {
            throw IllegalArgumentException("用户无权管理项目成员")
        }
        
        project.removeMember(memberId)
        projectRepository.save(project)
        
        return project
    }
    
    /**
     * 更新成员角色
     */
    suspend fun updateMemberRole(
        projectId: ProjectId,
        userId: UserId,
        memberId: UserId,
        role: ProjectRole
    ): Project {
        val project = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("项目不存在: $projectId")
        
        if (!project.canManageMembers(userId)) {
            throw IllegalArgumentException("用户无权管理项目成员")
        }
        
        project.updateMemberRole(memberId, role)
        projectRepository.save(project)
        
        return project
    }
    
    /**
     * 归档项目
     */
    suspend fun archiveProject(projectId: ProjectId, userId: UserId): Project {
        val project = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("项目不存在: $projectId")
        
        if (!project.canEdit(userId)) {
            throw IllegalArgumentException("用户无权归档此项目")
        }
        
        project.archive()
        projectRepository.save(project)
        
        return project
    }
    
    /**
     * 恢复项目
     */
    suspend fun restoreProject(projectId: ProjectId, userId: UserId): Project {
        val project = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("项目不存在: $projectId")
        
        if (!project.canEdit(userId)) {
            throw IllegalArgumentException("用户无权恢复此项目")
        }
        
        project.restore()
        projectRepository.save(project)
        
        return project
    }
    
    /**
     * 删除项目
     */
    suspend fun deleteProject(projectId: ProjectId, userId: UserId) {
        val project = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("项目不存在: $projectId")
        
        if (project.ownerId != userId) {
            throw IllegalArgumentException("只有项目所有者可以删除项目")
        }
        
        // 检查是否有关联的时间条目
        val timeEntryCount = timeEntryRepository.countByProjectId(projectId)
        if (timeEntryCount > 0) {
            throw IllegalStateException("项目存在关联的时间条目，无法删除")
        }
        
        projectRepository.delete(projectId)
    }
    
    /**
     * 获取用户可访问的项目列表
     */
    suspend fun getUserAccessibleProjects(
        userId: UserId,
        includeArchived: Boolean = false
    ): List<Project> {
        return projectRepository.findAccessibleByUserId(userId, includeArchived)
    }
    
    /**
     * 搜索项目
     */
    suspend fun searchProjects(
        userId: UserId,
        keyword: String,
        includeArchived: Boolean = false
    ): List<Project> {
        val accessibleProjects = getUserAccessibleProjects(userId, includeArchived)
        return accessibleProjects.filter { project ->
            project.name.contains(keyword, ignoreCase = true) ||
            project.description?.contains(keyword, ignoreCase = true) == true ||
            project.clientName?.contains(keyword, ignoreCase = true) == true
        }
    }
    
    /**
     * 获取项目统计信息
     */
    suspend fun getProjectStatistics(projectId: ProjectId, userId: UserId): ProjectStatistics {
        val project = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("项目不存在: $projectId")
        
        if (!project.canAccess(userId)) {
            throw IllegalArgumentException("用户无权访问此项目")
        }
        
        val timeEntries = timeEntryRepository.findByProjectId(projectId)
        val totalDuration = timeEntries
            .filter { it.status == TimeEntryStatus.STOPPED }
            .map { it.getTotalDuration() }
            .fold(Duration.ZERO) { acc, duration -> acc.plus(duration) }
        
        val billableEntries = timeEntries.filter { it.billable && it.status == TimeEntryStatus.STOPPED }
        val billableDuration = billableEntries
            .map { it.getTotalDuration() }
            .fold(Duration.ZERO) { acc, duration -> acc.plus(duration) }
        
        val totalRevenue = billableEntries
            .mapNotNull { entry ->
                val rate = entry.hourlyRate ?: project.hourlyRate
                rate?.let { it.multiply(entry.getTotalDuration().toHours()) }
            }
            .fold(BigDecimal.ZERO) { acc, revenue -> acc.add(revenue) }
        
        return ProjectStatistics(
            projectId = projectId,
            totalDuration = totalDuration,
            billableDuration = billableDuration,
            totalRevenue = totalRevenue,
            timeEntryCount = timeEntries.size,
            memberCount = project.members.size
        )
    }
    
    /**
     * 复制项目
     */
    suspend fun duplicateProject(
        projectId: ProjectId,
        userId: UserId,
        newName: String
    ): Project {
        val originalProject = projectRepository.findById(projectId)
            ?: throw IllegalArgumentException("项目不存在: $projectId")
        
        if (!originalProject.canAccess(userId)) {
            throw IllegalArgumentException("用户无权访问此项目")
        }
        
        // 检查新项目名称是否已存在
        if (projectRepository.existsByNameAndOwnerId(newName, userId)) {
            throw IllegalArgumentException("项目名称已存在: $newName")
        }
        
        // 创建新项目
        val newProject = Project.create(
            name = newName,
            description = originalProject.description,
            ownerId = userId,
            teamId = originalProject.teamId,
            clientName = originalProject.clientName,
            hourlyRate = originalProject.hourlyRate,
            color = originalProject.color,
            isPublic = originalProject.isPublic
        )
        
        projectRepository.save(newProject)
        
        return newProject
    }
}

/**
 * 项目统计信息
 */
data class ProjectStatistics(
    val projectId: ProjectId,
    val totalDuration: Duration,
    val billableDuration: Duration,
    val totalRevenue: BigDecimal,
    val timeEntryCount: Int,
    val memberCount: Int
)