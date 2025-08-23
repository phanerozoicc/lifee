// app/providers/user-provider.tsx
"use client"

import {
  fetchUserProfile,
  signOutUser,
  subscribeToUserUpdates,
  updateUserProfile,
} from "@/lib/user-store/api"
import { UserTimeAPI } from "@/lib/user/api"
import { createPermissionChecker, type PermissionChecker } from "@/lib/user/permissions"
import type { UserProfile, UserRole, UserPermission, UserTimeSettings } from "@/lib/user/types"
import { createContext, useContext, useEffect, useState, useCallback } from "react"

type UserContextType = {
  user: UserProfile | null
  isLoading: boolean
  permissionChecker: PermissionChecker | null
  timeSettings: UserTimeSettings | null
  userTeams: string[]
  userProjects: string[]
  updateUser: (updates: Partial<UserProfile>) => Promise<void>
  refreshUser: () => Promise<void>
  signOut: () => Promise<void>
  updateUserRole: (userId: string, role: UserRole) => Promise<boolean>
  updateUserPermissions: (userId: string, permissions: UserPermission[]) => Promise<boolean>
  updateTimeSettings: (updates: Partial<UserTimeSettings>) => Promise<boolean>
  refreshPermissions: () => Promise<void>
}

const UserContext = createContext<UserContextType | undefined>(undefined)

export function UserProvider({
  children,
  initialUser,
}: {
  children: React.ReactNode
  initialUser: UserProfile | null
}) {
  const [user, setUser] = useState<UserProfile | null>(initialUser)
  const [isLoading, setIsLoading] = useState(false)
  const [permissionChecker, setPermissionChecker] = useState<PermissionChecker | null>(null)
  const [timeSettings, setTimeSettings] = useState<UserTimeSettings | null>(null)
  const [userTeams, setUserTeams] = useState<string[]>([])
  const [userProjects, setUserProjects] = useState<string[]>([])

  const refreshUser = useCallback(async () => {
    if (!user?.id) return

    setIsLoading(true)
    try {
      const updatedUser = await fetchUserProfile(user.id)
      if (updatedUser) setUser(updatedUser)
    } finally {
      setIsLoading(false)
    }
  }, [user?.id])

  const updateUser = async (updates: Partial<UserProfile>) => {
    if (!user?.id) return

    setIsLoading(true)
    try {
      const success = await updateUserProfile(user.id, updates)
      if (success) {
        setUser((prev) => (prev ? { ...prev, ...updates } : null))
      }
    } finally {
      setIsLoading(false)
    }
  }

  const signOut = async () => {
    setIsLoading(true)
    try {
      const success = await signOutUser()
      if (success) {
        setUser(null)
        setPermissionChecker(null)
        setTimeSettings(null)
        setUserTeams([])
        setUserProjects([])
      }
    } finally {
      setIsLoading(false)
    }
  }

  // 刷新用户权限和团队项目信息
  const refreshPermissions = useCallback(async () => {
    if (!user?.id) return

    try {
      const [teams, projects, settings] = await Promise.all([
        UserTimeAPI.getUserTeams(user.id),
        UserTimeAPI.getUserProjects(user.id),
        UserTimeAPI.getTimeSettings(user.id)
      ])
      setUserTeams(teams)
      setUserProjects(projects)
      setTimeSettings(settings)
      
      // 更新权限检查器
      const checker = createPermissionChecker(user, teams, projects)
      setPermissionChecker(checker)
    } catch (error) {
      console.error('Failed to refresh permissions:', error)
    }
  }, [user])

  // 更新用户角色
  const updateUserRole = async (userId: string, role: UserRole): Promise<boolean> => {
    try {
      const success = await UserTimeAPI.updateUserRole(userId, role)
      if (success && userId === user?.id) {
        setUser(prev => prev ? { ...prev, role } : null)
        await refreshPermissions()
      }
      return success
    } catch (error) {
      console.error('Failed to update user role:', error)
      return false
    }
  }

  // 更新用户权限
  const updateUserPermissions = async (userId: string, permissions: UserPermission[]): Promise<boolean> => {
    try {
      const permissionsObj = permissions.reduce((acc, permission) => {
        acc[permission] = true
        return acc
      }, {} as Record<string, unknown>)
      const success = await UserTimeAPI.updateUserPermissions(userId, permissionsObj)
      if (success && userId === user?.id) {
        setUser(prev => prev ? { ...prev, permissions } : null)
        await refreshPermissions()
      }
      return success
    } catch (error) {
      console.error('Failed to update user permissions:', error)
      return false
    }
  }

  // 更新时间设置
  const updateTimeSettings = async (updates: Partial<UserTimeSettings>): Promise<boolean> => {
    if (!user?.id) return false

    try {
      const success = await UserTimeAPI.updateTimeSettings(user.id, updates)
      if (success) {
        setTimeSettings(prev => prev ? { ...prev, ...updates } : updates as UserTimeSettings)
      }
      return success
    } catch (error) {
      console.error('Failed to update time settings:', error)
      return false
    }
  }

  // 初始化权限和时间设置
  useEffect(() => {
    if (user?.id) {
      refreshPermissions()
    }
  }, [user?.id, refreshPermissions])

  // Set up realtime subscription for user data changes
  useEffect(() => {
    if (!user?.id) return

    const unsubscribe = subscribeToUserUpdates(user.id, (newData) => {
      setUser((prev) => (prev ? { ...prev, ...newData } : null))
      // 如果用户数据更新，刷新权限
      if (newData.role || newData.permissions) {
        refreshPermissions()
      }
    })

    return () => {
      unsubscribe()
    }
  }, [user?.id, refreshPermissions])

  return (
    <UserContext.Provider
      value={{
        user,
        isLoading,
        permissionChecker,
        timeSettings,
        userTeams,
        userProjects,
        updateUser,
        refreshUser,
        signOut,
        updateUserRole,
        updateUserPermissions,
        updateTimeSettings,
        refreshPermissions
      }}
    >
      {children}
    </UserContext.Provider>
  )
}

// Custom hook to use the user context
export function useUser() {
  const context = useContext(UserContext)
  if (context === undefined) {
    throw new Error("useUser must be used within a UserProvider")
  }
  return context
}
