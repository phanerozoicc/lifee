"use client"

import { useUserPermissions } from '@/lib/user/hooks'
import type { UserPermission, UserRole } from '@/lib/user/types'
import { ReactNode } from 'react'

interface PermissionGuardProps {
  children: ReactNode
  /** 需要的权限 */
  permission?: UserPermission
  /** 需要的角色 */
  role?: UserRole
  /** 需要的权限列表（满足其中一个即可） */
  permissions?: UserPermission[]
  /** 需要的角色列表（满足其中一个即可） */
  roles?: UserRole[]
  /** 是否需要所有权限都满足 */
  requireAll?: boolean
  /** 无权限时显示的内容 */
  fallback?: ReactNode
  /** 是否在加载时显示children */
  showWhileLoading?: boolean
}

/**
 * 权限保护组件
 * 根据用户权限控制子组件的显示
 */
export function PermissionGuard({
  children,
  permission,
  role,
  permissions = [],
  roles = [],
  requireAll = false,
  fallback = null,
  showWhileLoading = false
}: PermissionGuardProps) {
  const { permissionChecker, loading } = useUserPermissions()

  // 加载中的处理
  if (loading) {
    return showWhileLoading ? <>{children}</> : <>{fallback}</>
  }

  // 没有权限检查器时（用户未登录）
  if (!permissionChecker) {
    return <>{fallback}</>
  }

  // 检查单个权限
  if (permission && !permissionChecker.hasPermission(permission)) {
    return <>{fallback}</>
  }

  // 检查单个角色
  if (role && !permissionChecker.hasRole(role)) {
    return <>{fallback}</>
  }

  // 检查权限列表
  if (permissions.length > 0) {
    const hasPermissions = requireAll
      ? permissions.every(p => permissionChecker.hasPermission(p))
      : permissions.some(p => permissionChecker.hasPermission(p))
    
    if (!hasPermissions) {
      return <>{fallback}</>
    }
  }

  // 检查角色列表
  if (roles.length > 0) {
    const hasRoles = requireAll
      ? roles.every(r => permissionChecker.hasRole(r))
      : roles.some(r => permissionChecker.hasRole(r))
    
    if (!hasRoles) {
      return <>{fallback}</>
    }
  }

  return <>{children}</>
}

/**
 * 管理员权限保护组件
 */
export function AdminGuard({ children, fallback = null }: { children: ReactNode; fallback?: ReactNode }) {
  return (
    <PermissionGuard role="admin" fallback={fallback}>
      {children}
    </PermissionGuard>
  )
}

/**
 * 项目管理权限保护组件
 */
export function ProjectManagerGuard({ children, fallback = null }: { children: ReactNode; fallback?: ReactNode }) {
  return (
    <PermissionGuard 
      roles={['admin', 'project_manager']} 
      fallback={fallback}
    >
      {children}
    </PermissionGuard>
  )
}

/**
 * 团队领导权限保护组件
 */
export function TeamLeaderGuard({ children, fallback = null }: { children: ReactNode; fallback?: ReactNode }) {
  return (
    <PermissionGuard 
      roles={['admin', 'project_manager', 'team_leader']} 
      fallback={fallback}
    >
      {children}
    </PermissionGuard>
  )
}

/**
 * 时间管理权限保护组件
 */
export function TimeManagementGuard({ children, fallback = null }: { children: ReactNode; fallback?: ReactNode }) {
  return (
    <PermissionGuard 
      permissions={['manage_time_entries', 'view_time_entries']} 
      fallback={fallback}
    >
      {children}
    </PermissionGuard>
  )
}

/**
 * 报告查看权限保护组件
 */
export function ReportsGuard({ children, fallback = null }: { children: ReactNode; fallback?: ReactNode }) {
  return (
    <PermissionGuard 
      permissions={['view_reports', 'view_detailed_reports']} 
      fallback={fallback}
    >
      {children}
    </PermissionGuard>
  )
}