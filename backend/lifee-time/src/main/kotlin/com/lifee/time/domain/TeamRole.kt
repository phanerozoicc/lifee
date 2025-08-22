package com.lifee.time.domain

/**
 * 团队角色枚举
 */
enum class TeamRole {
    /**
     * 团队所有者 - 拥有所有权限
     */
    OWNER,
    
    /**
     * 管理员 - 可以管理团队成员和项目
     */
    ADMIN,
    
    /**
     * 项目经理 - 可以管理项目和任务
     */
    PROJECT_MANAGER,
    
    /**
     * 普通成员 - 可以记录时间和查看分配给自己的任务
     */
    MEMBER;
    
    /**
     * 检查是否可以管理团队成员
     */
    fun canManageMembers(): Boolean {
        return this in setOf(OWNER, ADMIN)
    }
    
    /**
     * 检查是否可以管理项目
     */
    fun canManageProjects(): Boolean {
        return this in setOf(OWNER, ADMIN, PROJECT_MANAGER)
    }
    
    /**
     * 检查是否可以查看团队报告
     */
    fun canViewReports(): Boolean {
        return this in setOf(OWNER, ADMIN, PROJECT_MANAGER)
    }
    
    /**
     * 检查是否可以删除团队
     */
    fun canDeleteTeam(): Boolean {
        return this == OWNER
    }
    
    /**
     * 检查是否可以邀请成员
     */
    fun canInviteMembers(): Boolean {
        return this in setOf(OWNER, ADMIN)
    }
    
    /**
     * 检查是否可以移除成员
     */
    fun canRemoveMembers(): Boolean {
        return this in setOf(OWNER, ADMIN)
    }
    
    /**
     * 检查是否可以修改成员角色
     */
    fun canModifyMemberRoles(): Boolean {
        return this in setOf(OWNER, ADMIN)
    }
    
    /**
     * 检查是否是管理角色
     */
    fun isManagementRole(): Boolean {
        return this in setOf(OWNER, ADMIN, PROJECT_MANAGER)
    }
    
    /**
     * 获取角色显示名称
     */
    fun getDisplayName(): String {
        return when (this) {
            OWNER -> "所有者"
            ADMIN -> "管理员"
            PROJECT_MANAGER -> "项目经理"
            MEMBER -> "成员"
        }
    }
}