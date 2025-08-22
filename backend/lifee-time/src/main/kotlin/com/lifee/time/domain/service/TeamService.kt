package com.lifee.time.domain.service

import com.lifee.time.domain.*
import com.lifee.time.domain.repository.TeamRepository
import com.lifee.time.domain.repository.ProjectRepository
import com.lifee.user.domain.UserId

/**
 * 团队管理领域服务
 */
class TeamService(
    private val teamRepository: TeamRepository,
    private val projectRepository: ProjectRepository
) {
    
    /**
     * 创建团队
     */
    suspend fun createTeam(
        name: String,
        description: String?,
        ownerId: UserId
    ): Team {
        // 检查团队名称是否已存在
        if (teamRepository.existsByNameAndOwnerId(name, ownerId)) {
            throw IllegalArgumentException("团队名称已存在: $name")
        }
        
        // 创建团队
        val team = Team.create(
            name = name,
            description = description,
            ownerId = ownerId
        )
        
        teamRepository.save(team)
        
        return team
    }
    
    /**
     * 更新团队信息
     */
    suspend fun updateTeam(
        teamId: TeamId,
        userId: UserId,
        name: String? = null,
        description: String? = null
    ): Team {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("团队不存在: $teamId")
        
        if (!team.canManageMembers(userId)) {
            throw IllegalArgumentException("用户无权管理此团队")
        }
        
        // 检查团队名称是否已存在（如果要更新名称）
        if (name != null && name != team.name) {
            if (teamRepository.existsByNameAndOwnerId(name, team.ownerId)) {
                throw IllegalArgumentException("团队名称已存在: $name")
            }
        }
        
        team.updateInfo(
            name = name ?: team.name,
            description = description ?: team.description
        )
        
        teamRepository.save(team)
        
        return team
    }
    
    /**
     * 添加团队成员
     */
    suspend fun addMember(
        teamId: TeamId,
        userId: UserId,
        memberId: UserId,
        role: TeamRole = TeamRole.MEMBER
    ): Team {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("团队不存在: $teamId")
        
        if (!team.canInviteMembers(userId)) {
            throw IllegalArgumentException("用户无权邀请团队成员")
        }
        
        // 检查是否已经是团队成员
        if (team.isMember(memberId)) {
            throw IllegalArgumentException("用户已经是团队成员")
        }
        
        team.addMember(memberId, role, userId)
        teamRepository.save(team)
        
        return team
    }
    
    /**
     * 移除团队成员
     */
    suspend fun removeMember(
        teamId: TeamId,
        userId: UserId,
        memberId: UserId
    ): Team {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("团队不存在: $teamId")
        
        if (!team.canRemoveMembers(userId)) {
            throw IllegalArgumentException("用户无权移除团队成员")
        }
        
        // 不能移除团队所有者
        if (memberId == team.ownerId) {
            throw IllegalArgumentException("不能移除团队所有者")
        }
        
        // 检查是否是团队成员
        if (!team.isMember(memberId)) {
            throw IllegalArgumentException("用户不是团队成员")
        }
        
        team.removeMember(memberId, userId)
        teamRepository.save(team)
        
        return team
    }
    
    /**
     * 更新成员角色
     */
    suspend fun updateMemberRole(
        teamId: TeamId,
        userId: UserId,
        memberId: UserId,
        role: TeamRole
    ): Team {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("团队不存在: $teamId")
        
        if (!team.canModifyMemberRoles(userId)) {
            throw IllegalArgumentException("用户无权修改成员角色")
        }
        
        // 不能修改团队所有者的角色
        if (memberId == team.ownerId) {
            throw IllegalArgumentException("不能修改团队所有者的角色")
        }
        
        // 检查是否是团队成员
        if (!team.isMember(memberId)) {
            throw IllegalArgumentException("用户不是团队成员")
        }
        
        team.updateMemberRole(memberId, role, userId)
        teamRepository.save(team)
        
        return team
    }
    
    /**
     * 离开团队
     */
    suspend fun leaveTeam(teamId: TeamId, userId: UserId): Team {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("团队不存在: $teamId")
        
        // 团队所有者不能离开团队
        if (userId == team.ownerId) {
            throw IllegalArgumentException("团队所有者不能离开团队，请先转移所有权或删除团队")
        }
        
        // 检查是否是团队成员
        if (!team.isMember(userId)) {
            throw IllegalArgumentException("用户不是团队成员")
        }
        
        team.removeMember(userId, userId)
        teamRepository.save(team)
        
        return team
    }
    
    /**
     * 转移团队所有权
     */
    suspend fun transferOwnership(
        teamId: TeamId,
        currentOwnerId: UserId,
        newOwnerId: UserId
    ): Team {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("团队不存在: $teamId")
        
        // 只有当前所有者可以转移所有权
        if (team.ownerId != currentOwnerId) {
            throw IllegalArgumentException("只有团队所有者可以转移所有权")
        }
        
        // 新所有者必须是团队成员
        if (!team.isMember(newOwnerId)) {
            throw IllegalArgumentException("新所有者必须是团队成员")
        }
        
        // 更新所有者
        team.updateMemberRole(newOwnerId, TeamRole.OWNER, currentOwnerId)
        team.updateMemberRole(currentOwnerId, TeamRole.ADMIN, newOwnerId)
        
        teamRepository.save(team)
        
        return team
    }
    
    /**
     * 停用团队
     */
    suspend fun deactivateTeam(teamId: TeamId, userId: UserId): Team {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("团队不存在: $teamId")
        
        if (!team.canDeleteTeam(userId)) {
            throw IllegalArgumentException("用户无权停用此团队")
        }
        
        team.deactivate(userId)
        teamRepository.save(team)
        
        return team
    }
    
    /**
     * 激活团队
     */
    suspend fun activateTeam(teamId: TeamId, userId: UserId): Team {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("团队不存在: $teamId")
        
        if (team.ownerId != userId) {
            throw IllegalArgumentException("只有团队所有者可以激活团队")
        }
        
        team.activate(userId)
        teamRepository.save(team)
        
        return team
    }
    
    /**
     * 删除团队
     */
    suspend fun deleteTeam(teamId: TeamId, userId: UserId) {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("团队不存在: $teamId")
        
        if (team.ownerId != userId) {
            throw IllegalArgumentException("只有团队所有者可以删除团队")
        }
        
        // 检查是否有关联的项目
        val projectCount = projectRepository.countByTeamId(teamId)
        if (projectCount > 0) {
            throw IllegalStateException("团队存在关联的项目，无法删除")
        }
        
        teamRepository.delete(teamId)
    }
    
    /**
     * 获取用户的团队列表
     */
    suspend fun getUserTeams(
        userId: UserId,
        includeInactive: Boolean = false
    ): List<Team> {
        return teamRepository.findByMemberId(userId, includeInactive)
    }
    
    /**
     * 获取用户拥有的团队列表
     */
    suspend fun getUserOwnedTeams(
        userId: UserId,
        includeInactive: Boolean = false
    ): List<Team> {
        return teamRepository.findByOwnerId(userId, includeInactive)
    }
    
    /**
     * 获取用户可管理的团队列表
     */
    suspend fun getUserManageableTeams(userId: UserId): List<Team> {
        return teamRepository.findManageableByUserId(userId)
    }
    
    /**
     * 搜索团队
     */
    suspend fun searchTeams(
        userId: UserId,
        keyword: String,
        includeInactive: Boolean = false
    ): List<Team> {
        val userTeams = getUserTeams(userId, includeInactive)
        return userTeams.filter { team ->
            team.name.contains(keyword, ignoreCase = true) ||
            team.description?.contains(keyword, ignoreCase = true) == true
        }
    }
    
    /**
     * 获取团队统计信息
     */
    suspend fun getTeamStatistics(teamId: TeamId, userId: UserId): TeamStatistics {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("团队不存在: $teamId")
        
        if (!team.canViewReports(userId)) {
            throw IllegalArgumentException("用户无权查看团队统计信息")
        }
        
        val projectCount = projectRepository.countByTeamId(teamId)
        val activeProjectCount = projectRepository.countActiveByTeamId(teamId)
        val memberCount = team.members.size
        val activeMemberCount = team.members.count { it.isActive }
        
        return TeamStatistics(
            teamId = teamId,
            memberCount = memberCount,
            activeMemberCount = activeMemberCount,
            projectCount = projectCount,
            activeProjectCount = activeProjectCount
        )
    }
    
    /**
     * 获取团队成员列表
     */
    suspend fun getTeamMembers(
        teamId: TeamId,
        userId: UserId,
        includeInactive: Boolean = false
    ): List<TeamMember> {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("团队不存在: $teamId")
        
        if (!team.isMember(userId)) {
            throw IllegalArgumentException("用户不是团队成员")
        }
        
        return if (includeInactive) {
            team.members
        } else {
            team.members.filter { it.isActive }
        }
    }
    
    /**
     * 批量邀请成员
     */
    suspend fun batchInviteMembers(
        teamId: TeamId,
        userId: UserId,
        memberIds: List<UserId>,
        role: TeamRole = TeamRole.MEMBER
    ): Team {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("团队不存在: $teamId")
        
        if (!team.canInviteMembers(userId)) {
            throw IllegalArgumentException("用户无权邀请团队成员")
        }
        
        // 过滤已经是团队成员的用户
        val newMemberIds = memberIds.filter { !team.isMember(it) }
        
        // 批量添加成员
        newMemberIds.forEach { memberId ->
            team.addMember(memberId, role, userId)
        }
        
        teamRepository.save(team)
        
        return team
    }
    
    /**
     * 批量移除成员
     */
    suspend fun batchRemoveMembers(
        teamId: TeamId,
        userId: UserId,
        memberIds: List<UserId>
    ): Team {
        val team = teamRepository.findById(teamId)
            ?: throw IllegalArgumentException("团队不存在: $teamId")
        
        if (!team.canRemoveMembers(userId)) {
            throw IllegalArgumentException("用户无权移除团队成员")
        }
        
        // 过滤掉团队所有者
        val removableMemberIds = memberIds.filter { it != team.ownerId && team.isMember(it) }
        
        // 批量移除成员
        removableMemberIds.forEach { memberId ->
            team.removeMember(memberId, userId)
        }
        
        teamRepository.save(team)
        
        return team
    }
}

/**
 * 团队统计信息
 */
data class TeamStatistics(
    val teamId: TeamId,
    val memberCount: Int,
    val activeMemberCount: Int,
    val projectCount: Int,
    val activeProjectCount: Int
)