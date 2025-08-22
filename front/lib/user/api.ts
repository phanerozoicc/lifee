import { isSupabaseEnabled } from "@/lib/supabase/config"
import { createClient } from "@/lib/supabase/server"
import {
  convertFromApiFormat,
  defaultPreferences,
} from "@/lib/user-preference-store/utils"
import type { UserProfile, UserRole, UserTimeSettings } from "./types"
import { fetchClient } from "@/lib/api"

export async function getSupabaseUser() {
  const supabase = await createClient()
  if (!supabase) return { supabase: null, user: null }

  const { data } = await supabase.auth.getUser()
  return {
    supabase,
    user: data.user ?? null,
  }
}

export async function getUserProfile(): Promise<UserProfile | null> {
  if (!isSupabaseEnabled) {
    // return fake user profile for no supabase
    return {
      id: "guest",
      email: "guest@zola.chat",
      display_name: "Guest",
      profile_image: "",
      anonymous: true,
      preferences: defaultPreferences,
    } as UserProfile
  }

  const { supabase, user } = await getSupabaseUser()
  if (!supabase || !user) return null

  const { data: userProfileData } = await supabase
    .from("users")
    .select("*, user_preferences(*)")
    .eq("id", user.id)
    .single()

  // Don't load anonymous users in the user store
  if (userProfileData?.anonymous) return null

  // Format user preferences if they exist
  const formattedPreferences = userProfileData?.user_preferences
    ? convertFromApiFormat(userProfileData.user_preferences)
    : undefined

  return {
    ...userProfileData,
    profile_image: user.user_metadata?.avatar_url ?? "",
    display_name: user.user_metadata?.name ?? "",
    preferences: formattedPreferences,
  } as UserProfile
}

// 时间管理相关的用户API
export const UserTimeAPI = {
  /**
   * 获取用户的时间设置
   */
  async getTimeSettings(userId: string): Promise<UserTimeSettings | null> {
    try {
      const response = await fetchClient(`/api/users/${userId}/time-settings`)
      return response.data
    } catch (error) {
      console.error('Failed to fetch user time settings:', error)
      return null
    }
  },

  /**
   * 更新用户的时间设置
   */
  async updateTimeSettings(userId: string, settings: Record<string, unknown>): Promise<boolean> {
    try {
      await fetchClient(`/api/users/${userId}/time-settings`, {
        method: 'PUT',
        body: JSON.stringify(settings)
      })
      return true
    } catch (error) {
      console.error('Failed to update user time settings:', error)
      return false
    }
  },

  /**
   * 获取用户的团队列表
   */
  async getUserTeams(userId: string): Promise<string[]> {
    try {
      const response = await fetchClient(`/api/users/${userId}/teams`)
      return response.data.map((team: Record<string, unknown>) => team.id as string)
    } catch (error) {
      console.error('Failed to fetch user teams:', error)
      return []
    }
  },

  /**
   * 获取用户有权限的项目列表
   */
  async getUserProjects(userId: string): Promise<string[]> {
    try {
      const response = await fetchClient(`/api/users/${userId}/projects`)
      return response.data.map((project: Record<string, unknown>) => project.id as string)
    } catch (error) {
      console.error('Failed to fetch user projects:', error)
      return []
    }
  },

  /**
   * 更新用户角色
   */
  async updateUserRole(userId: string, role: UserRole): Promise<boolean> {
    try {
      await fetchClient(`/api/users/${userId}/role`, {
        method: 'PUT',
        body: JSON.stringify({ role })
      })
      return true
    } catch (error) {
      console.error('Failed to update user role:', error)
      return false
    }
  },

  /**
   * 更新用户权限
   */
  async updateUserPermissions(userId: string, permissions: Record<string, unknown>): Promise<boolean> {
    try {
      await fetchClient(`/api/users/${userId}/permissions`, {
        method: 'PUT',
        body: JSON.stringify({ permissions })
      })
      return true
    } catch (error) {
      console.error('Failed to update user permissions:', error)
      return false
    }
  },

  /**
   * 获取用户的工作统计
   */
  async getUserWorkStats(userId: string, startDate?: string, endDate?: string): Promise<Record<string, unknown> | null> {
    try {
      const params = new URLSearchParams()
      if (startDate) params.append('startDate', startDate)
      if (endDate) params.append('endDate', endDate)
      
      const response = await fetchClient(`/api/users/${userId}/work-stats?${params}`)
      return response.data
    } catch (error) {
      console.error('Failed to fetch user work stats:', error)
      return null
    }
  },

  /**
   * 获取用户的时间条目统计
   */
  async getUserTimeStats(userId: string, period: 'week' | 'month' | 'year' = 'week'): Promise<Record<string, unknown> | null> {
    try {
      const response = await fetchClient(`/api/users/${userId}/time-stats?period=${period}`)
      return response.data
    } catch (error) {
      console.error('Failed to fetch user time stats:', error)
      return null
    }
  },

  /**
   * 检查用户是否可以访问项目
   */
  async canAccessProject(userId: string, projectId: string): Promise<boolean> {
    try {
      const response = await fetchClient(`/api/users/${userId}/can-access-project/${projectId}`)
      return response.data.canAccess
    } catch (error) {
      console.error('Failed to check project access:', error)
      return false
    }
  },

  /**
   * 检查用户是否可以访问团队
   */
  async canAccessTeam(userId: string, teamId: string): Promise<boolean> {
    try {
      const response = await fetchClient(`/api/users/${userId}/can-access-team/${teamId}`)
      return response.data.canAccess
    } catch (error) {
      console.error('Failed to check team access:', error)
      return false
    }
  }
}
