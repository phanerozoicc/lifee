import { UserRole, UserPermission, type UserProfile, type PermissionChecker } from './types'

// 角色权限映射
const ROLE_PERMISSIONS: Record<UserRole, UserPermission[]> = {
  [UserRole.ADMIN]: [
    // 管理员拥有所有权限
    UserPermission.MANAGE_TIME_ENTRIES,
    UserPermission.VIEW_TIME_ENTRIES,
    UserPermission.EXPORT_TIME_DATA,
    UserPermission.MANAGE_PROJECTS,
    UserPermission.VIEW_PROJECTS,
    UserPermission.MANAGE_CLIENTS,
    UserPermission.VIEW_CLIENTS,
    UserPermission.MANAGE_TEAMS,
    UserPermission.VIEW_TEAMS,
    UserPermission.MANAGE_TEAM_MEMBERS,
    UserPermission.VIEW_TEAM_MEMBERS,
    UserPermission.VIEW_REPORTS,
    UserPermission.EXPORT_REPORTS,
    UserPermission.VIEW_ALL_REPORTS,
    UserPermission.MANAGE_USERS,
    UserPermission.MANAGE_SETTINGS
  ],
  [UserRole.MANAGER]: [
    // 管理者权限
    UserPermission.MANAGE_TIME_ENTRIES,
    UserPermission.VIEW_TIME_ENTRIES,
    UserPermission.EXPORT_TIME_DATA,
    UserPermission.MANAGE_PROJECTS,
    UserPermission.VIEW_PROJECTS,
    UserPermission.MANAGE_CLIENTS,
    UserPermission.VIEW_CLIENTS,
    UserPermission.MANAGE_TEAMS,
    UserPermission.VIEW_TEAMS,
    UserPermission.MANAGE_TEAM_MEMBERS,
    UserPermission.VIEW_TEAM_MEMBERS,
    UserPermission.VIEW_REPORTS,
    UserPermission.EXPORT_REPORTS,
    UserPermission.VIEW_ALL_REPORTS
  ],
  [UserRole.MEMBER]: [
    // 普通成员权限
    UserPermission.MANAGE_TIME_ENTRIES,
    UserPermission.VIEW_TIME_ENTRIES,
    UserPermission.VIEW_PROJECTS,
    UserPermission.VIEW_CLIENTS,
    UserPermission.VIEW_TEAMS,
    UserPermission.VIEW_TEAM_MEMBERS,
    UserPermission.VIEW_REPORTS
  ],
  [UserRole.VIEWER]: [
    // 查看者权限
    UserPermission.VIEW_TIME_ENTRIES,
    UserPermission.VIEW_PROJECTS,
    UserPermission.VIEW_CLIENTS,
    UserPermission.VIEW_TEAMS,
    UserPermission.VIEW_TEAM_MEMBERS,
    UserPermission.VIEW_REPORTS
  ]
}

/**
 * 创建用户权限检查器
 * @param user 用户配置文件
 * @param userTeams 用户所属的团队ID列表
 * @param userProjects 用户有权限的项目ID列表
 * @returns 权限检查器对象
 */
export function createPermissionChecker(
  user: UserProfile | null,
  userTeams: string[] = [],
  userProjects: string[] = []
): PermissionChecker {
  if (!user) {
    return {
      hasPermission: () => false,
      hasRole: () => false,
      canManageUser: () => false,
      canViewTeam: () => false,
      canManageProject: () => false
    }
  }

  const userRole = user.role || UserRole.VIEWER
  const rolePermissions = ROLE_PERMISSIONS[userRole] || []
  const explicitPermissions = user.permissions || []
  const allPermissions = [...new Set([...rolePermissions, ...explicitPermissions])]

  return {
    hasPermission: (permission: UserPermission) => {
      return allPermissions.includes(permission)
    },

    hasRole: (role: UserRole) => {
      return userRole === role
    },

    canManageUser: (targetUserId: string) => {
      // 管理员可以管理所有用户
      if (userRole === UserRole.ADMIN) return true
      
      // 管理者可以管理下属
      if (userRole === UserRole.MANAGER) {
        // 这里需要根据实际的组织结构来判断
        // 暂时简化为：管理者可以管理非管理员用户
        return true
      }
      
      // 用户只能管理自己
      return user.id === targetUserId
    },

    canViewTeam: (teamId: string) => {
      // 管理员和管理者可以查看所有团队
      if (userRole === UserRole.ADMIN || userRole === UserRole.MANAGER) {
        return true
      }
      
      // 普通用户只能查看自己所属的团队
      return userTeams.includes(teamId)
    },

    canManageProject: (projectId: string) => {
      // 管理员和管理者可以管理所有项目
      if (userRole === UserRole.ADMIN || userRole === UserRole.MANAGER) {
        return true
      }
      
      // 普通用户只能管理有权限的项目
      return userProjects.includes(projectId)
    }
  }
}

/**
 * 检查用户是否有指定权限
 * @param user 用户配置文件
 * @param permission 权限
 * @returns 是否有权限
 */
export function hasPermission(user: UserProfile | null, permission: UserPermission): boolean {
  const checker = createPermissionChecker(user)
  return checker.hasPermission(permission)
}

/**
 * 检查用户是否有指定角色
 * @param user 用户配置文件
 * @param role 角色
 * @returns 是否有角色
 */
export function hasRole(user: UserProfile | null, role: UserRole): boolean {
  const checker = createPermissionChecker(user)
  return checker.hasRole(role)
}

/**
 * 获取用户的所有权限
 * @param user 用户配置文件
 * @returns 权限列表
 */
export function getUserPermissions(user: UserProfile | null): UserPermission[] {
  if (!user) return []
  
  const userRole = user.role || UserRole.VIEWER
  const rolePermissions = ROLE_PERMISSIONS[userRole] || []
  const explicitPermissions = user.permissions || []
  
  return [...new Set([...rolePermissions, ...explicitPermissions])]
}

/**
 * 检查用户是否可以访问时间管理功能
 * @param user 用户配置文件
 * @returns 是否可以访问
 */
export function canAccessTimeTracking(user: UserProfile | null): boolean {
  return hasPermission(user, UserPermission.VIEW_TIME_ENTRIES)
}

/**
 * 检查用户是否可以管理项目
 * @param user 用户配置文件
 * @returns 是否可以管理
 */
export function canManageProjects(user: UserProfile | null): boolean {
  return hasPermission(user, UserPermission.MANAGE_PROJECTS)
}

/**
 * 检查用户是否可以查看报告
 * @param user 用户配置文件
 * @returns 是否可以查看
 */
export function canViewReports(user: UserProfile | null): boolean {
  return hasPermission(user, UserPermission.VIEW_REPORTS)
}

/**
 * 检查用户是否可以管理团队
 * @param user 用户配置文件
 * @returns 是否可以管理
 */
export function canManageTeams(user: UserProfile | null): boolean {
  return hasPermission(user, UserPermission.MANAGE_TEAMS)
}

/**
 * 检查用户是否是管理员
 * @param user 用户配置文件
 * @returns 是否是管理员
 */
export function isAdmin(user: UserProfile | null): boolean {
  return hasRole(user, UserRole.ADMIN)
}

/**
 * 检查用户是否是管理者
 * @param user 用户配置文件
 * @returns 是否是管理者
 */
export function isManager(user: UserProfile | null): boolean {
  return hasRole(user, UserRole.MANAGER)
}