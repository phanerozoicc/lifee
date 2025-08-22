package com.lifee.time.domain

import com.lifee.common.domain.ValueObject
import com.lifee.user.domain.UserId
import java.time.Instant

/**
 * 团队成员值对象
 */
data class TeamMember(
    val userId: UserId,
    val role: TeamRole,
    val joinedAt: Instant,
    val isActive: Boolean = true
) : ValueObject {
    
    /**
     * 检查是否可以管理团队成员
     */
    fun canManageMembers(): Boolean {
        return isActive && role.canManageMembers()
    }
    
    /**
     * 检查是否可以管理项目
     */
    fun canManageProjects(): Boolean {
        return isActive && role.canManageProjects()
    }
    
    /**
     * 检查是否可以查看团队报告
     */
    fun canViewReports(): Boolean {
        return isActive && role.canViewReports()
    }
    
    /**
     * 检查是否可以删除团队
     */
    fun canDeleteTeam(): Boolean {
        return isActive && role.canDeleteTeam()
    }
    
    /**
     * 检查是否可以邀请成员
     */
    fun canInviteMembers(): Boolean {
        return isActive && role.canInviteMembers()
    }
    
    /**
     * 检查是否可以移除成员
     */
    fun canRemoveMembers(): Boolean {
        return isActive && role.canRemoveMembers()
    }
    
    /**
     * 检查是否可以修改成员角色
     */
    fun canModifyMemberRoles(): Boolean {
        return isActive && role.canModifyMemberRoles()
    }
    
    /**
     * 检查是否是管理角色
     */
    fun isManagementRole(): Boolean {
        return isActive && role.isManagementRole()
    }
    
    /**
     * 更新角色
     */
    fun withRole(newRole: TeamRole): TeamMember {
        return copy(role = newRole)
    }
    
    /**
     * 停用成员
     */
    fun deactivate(): TeamMember {
        return copy(isActive = false)
    }
    
    /**
     * 激活成员
     */
    fun activate(): TeamMember {
        return copy(isActive = true)
    }
    
    companion object {
        /**
         * 创建新的团队成员
         */
        fun create(
            userId: UserId,
            role: TeamRole,
            joinedAt: Instant = Instant.now()
        ): TeamMember {
            return TeamMember(
                userId = userId,
                role = role,
                joinedAt = joinedAt,
                isActive = true
            )
        }
    }
}