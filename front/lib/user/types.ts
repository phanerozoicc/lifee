import type { Tables } from "@/app/types/database.types"
import type { UserPreferences } from "../user-preference-store/utils"

// 用户角色枚举
export enum UserRole {
  ADMIN = "admin",
  MANAGER = "manager",
  MEMBER = "member",
  VIEWER = "viewer"
}

// 用户权限枚举
export enum UserPermission {
  // 时间管理权限
  MANAGE_TIME_ENTRIES = "manage_time_entries",
  VIEW_TIME_ENTRIES = "view_time_entries",
  EXPORT_TIME_DATA = "export_time_data",
  
  // 项目管理权限
  MANAGE_PROJECTS = "manage_projects",
  VIEW_PROJECTS = "view_projects",
  MANAGE_CLIENTS = "manage_clients",
  VIEW_CLIENTS = "view_clients",
  
  // 团队管理权限
  MANAGE_TEAMS = "manage_teams",
  VIEW_TEAMS = "view_teams",
  MANAGE_TEAM_MEMBERS = "manage_team_members",
  VIEW_TEAM_MEMBERS = "view_team_members",
  
  // 报告权限
  VIEW_REPORTS = "view_reports",
  EXPORT_REPORTS = "export_reports",
  VIEW_ALL_REPORTS = "view_all_reports",
  
  // 系统管理权限
  MANAGE_USERS = "manage_users",
  MANAGE_SETTINGS = "manage_settings"
}

// 扩展的用户配置文件
export type UserProfile = {
  profile_image: string
  display_name: string
  preferences?: UserPreferences
  // 时间管理相关字段
  role?: UserRole
  permissions?: UserPermission[]
  timezone?: string
  hourly_rate?: number
  weekly_capacity?: number // 每周工作小时数
  department?: string
  job_title?: string
  manager_id?: string
  is_active?: boolean
} & Tables<"users">

// 用户权限检查工具类型
export type PermissionChecker = {
  hasPermission: (permission: UserPermission) => boolean
  hasRole: (role: UserRole) => boolean
  canManageUser: (targetUserId: string) => boolean
  canViewTeam: (teamId: string) => boolean
  canManageProject: (projectId: string) => boolean
}

// 用户设置类型
export type UserTimeSettings = {
  defaultProject?: string
  defaultTask?: string
  autoStartTimer?: boolean
  reminderEnabled?: boolean
  reminderInterval?: number // 分钟
  weekStartDay?: number // 0-6, 0为周日
  timeFormat?: '12h' | '24h'
  dateFormat?: string
}
