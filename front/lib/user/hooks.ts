"use client"

import { useState, useEffect, useMemo } from 'react'
import { useUser } from '@/lib/user-store/provider'
import { createPermissionChecker } from './permissions'
import { UserTimeAPI } from './api'
import type { UserPermission, UserRole, UserTimeSettings } from './types'

/**
 * 用户权限管理Hook
 * @returns 权限检查器和相关状态
 */
export function useUserPermissions() {
  const { user } = useUser()
  const [userTeams, setUserTeams] = useState<string[]>([])
  const [userProjects, setUserProjects] = useState<string[]>([])
  const [loading, setLoading] = useState(true)

  // 获取用户的团队和项目权限
  useEffect(() => {
    if (!user?.id) {
      setLoading(false)
      return
    }

    const fetchUserPermissions = async () => {
      setLoading(true)
      try {
        const [teams, projects] = await Promise.all([
          UserTimeAPI.getUserTeams(user.id),
          UserTimeAPI.getUserProjects(user.id)
        ])
        setUserTeams(teams)
        setUserProjects(projects)
      } catch (error) {
        console.error('Failed to fetch user permissions:', error)
      } finally {
        setLoading(false)
      }
    }

    fetchUserPermissions()
  }, [user?.id])

  // 创建权限检查器
  const permissionChecker = useMemo(() => {
    return createPermissionChecker(user, userTeams, userProjects)
  }, [user, userTeams, userProjects])

  return {
    user,
    permissionChecker,
    userTeams,
    userProjects,
    loading,
    // 便捷方法
    hasPermission: permissionChecker.hasPermission,
    hasRole: permissionChecker.hasRole,
    canManageUser: permissionChecker.canManageUser,
    canViewTeam: permissionChecker.canViewTeam,
    canManageProject: permissionChecker.canManageProject
  }
}

/**
 * 用户时间设置Hook
 * @returns 时间设置和更新方法
 */
export function useUserTimeSettings() {
  const { user } = useUser()
  const [timeSettings, setTimeSettings] = useState<UserTimeSettings | null>(null)
  const [loading, setLoading] = useState(true)
  const [updating, setUpdating] = useState(false)

  // 获取用户时间设置
  useEffect(() => {
    if (!user?.id) {
      setLoading(false)
      return
    }

    const fetchTimeSettings = async () => {
      setLoading(true)
      try {
        const settings = await UserTimeAPI.getTimeSettings(user.id)
        setTimeSettings(settings)
      } catch (error) {
        console.error('Failed to fetch time settings:', error)
      } finally {
        setLoading(false)
      }
    }

    fetchTimeSettings()
  }, [user?.id])

  // 更新时间设置
  const updateTimeSettings = async (updates: Partial<UserTimeSettings>) => {
    if (!user?.id) return false

    setUpdating(true)
    try {
      const success = await UserTimeAPI.updateTimeSettings(user.id, updates)
      if (success) {
        setTimeSettings(prev => prev ? { ...prev, ...updates } : updates as UserTimeSettings)
      }
      return success
    } catch (error) {
      console.error('Failed to update time settings:', error)
      return false
    } finally {
      setUpdating(false)
    }
  }

  return {
    timeSettings,
    loading,
    updating,
    updateTimeSettings
  }
}

/**
 * 用户工作统计Hook
 * @param period 统计周期
 * @returns 工作统计数据
 */
export function useUserWorkStats(period: 'week' | 'month' | 'year' = 'week') {
  const { user } = useUser()
  const [workStats, setWorkStats] = useState<Record<string, unknown> | null>(null)
  const [timeStats, setTimeStats] = useState<Record<string, unknown> | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!user?.id) {
      setLoading(false)
      return
    }

    const fetchStats = async () => {
      setLoading(true)
      try {
        const [workData, timeData] = await Promise.all([
          UserTimeAPI.getUserWorkStats(user.id),
          UserTimeAPI.getUserTimeStats(user.id, period)
        ])
        setWorkStats(workData)
        setTimeStats(timeData)
      } catch (error) {
        console.error('Failed to fetch user stats:', error)
      } finally {
        setLoading(false)
      }
    }

    fetchStats()
  }, [user?.id, period])

  return {
    workStats,
    timeStats,
    loading
  }
}

/**
 * 权限检查Hook - 用于条件渲染
 * @param permission 需要检查的权限
 * @returns 是否有权限
 */
export function useHasPermission(permission: UserPermission): boolean {
  const { hasPermission } = useUserPermissions()
  return hasPermission(permission)
}

/**
 * 角色检查Hook - 用于条件渲染
 * @param role 需要检查的角色
 * @returns 是否有角色
 */
export function useHasRole(role: UserRole): boolean {
  const { hasRole } = useUserPermissions()
  return hasRole(role)
}

/**
 * 项目访问权限Hook
 * @param projectId 项目ID
 * @returns 是否可以访问项目
 */
export function useCanAccessProject(projectId: string): boolean {
  const { user } = useUser()
  const [canAccess, setCanAccess] = useState(false)

  useEffect(() => {
    if (!user?.id || !projectId) {
      return
    }

    const checkAccess = async () => {
      try {
        const access = await UserTimeAPI.canAccessProject(user.id, projectId)
        setCanAccess(access)
      } catch (error) {
        console.error('Failed to check project access:', error)
        setCanAccess(false)
      }
    }

    checkAccess()
  }, [user?.id, projectId])

  return canAccess
}

/**
 * 团队访问权限Hook
 * @param teamId 团队ID
 * @returns 是否可以访问团队
 */
export function useCanAccessTeam(teamId: string): boolean {
  const { user } = useUser()
  const [canAccess, setCanAccess] = useState(false)

  useEffect(() => {
    if (!user?.id || !teamId) {
      return
    }

    const checkAccess = async () => {
      try {
        const access = await UserTimeAPI.canAccessTeam(user.id, teamId)
        setCanAccess(access)
      } catch (error) {
        console.error('Failed to check team access:', error)
        setCanAccess(false)
      }
    }

    checkAccess()
  }, [user?.id, teamId])

  return canAccess
}