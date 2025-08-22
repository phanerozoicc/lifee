import { fetchClient } from "@/lib/fetch"
import type {
  TimeEntry,
  Project,
  Client,
  Task,
  Team,
  TeamMember,
  Tag,
  ReportData,
  ProjectReport,
  TeamReport,
  ApiResponse,
  PaginatedResponse,
  TimeEntryFilter,
  ProjectFilter,
  ExportOptions,
  DashboardData,
  WorkloadData
} from "./types"

// API路由常量
const API_ROUTES = {
  TIME_ENTRIES: "/api/time/entries",
  PROJECTS: "/api/time/projects",
  CLIENTS: "/api/time/clients",
  TASKS: "/api/time/tasks",
  TEAMS: "/api/time/teams",
  TEAM_MEMBERS: "/api/time/team-members",
  TAGS: "/api/time/tags",
  REPORTS: "/api/time/reports",
  DASHBOARD: "/api/time/dashboard",
  TIMER: "/api/time/timer",
  EXPORT: "/api/time/export"
} as const

// 时间条目相关API
export class TimeEntryAPI {
  // 获取时间条目列表
  static async getTimeEntries(
    filter?: TimeEntryFilter,
    page = 1,
    limit = 50
  ): Promise<PaginatedResponse<TimeEntry>> {
    const params = new URLSearchParams({
      page: page.toString(),
      limit: limit.toString(),
      ...filter
    })
    
    const response = await fetchClient(`${API_ROUTES.TIME_ENTRIES}?${params}`)
    if (!response.ok) {
      throw new Error(`Failed to fetch time entries: ${response.statusText}`)
    }
    return response.json()
  }

  // 创建时间条目
  static async createTimeEntry(
    entry: Omit<TimeEntry, "id" | "created_at" | "updated_at">
  ): Promise<ApiResponse<TimeEntry>> {
    const response = await fetchClient(API_ROUTES.TIME_ENTRIES, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(entry)
    })
    if (!response.ok) {
      throw new Error(`Failed to create time entry: ${response.statusText}`)
    }
    return response.json()
  }

  // 更新时间条目
  static async updateTimeEntry(
    id: string,
    updates: Partial<TimeEntry>
  ): Promise<ApiResponse<TimeEntry>> {
    const response = await fetchClient(`${API_ROUTES.TIME_ENTRIES}/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updates)
    })
    if (!response.ok) {
      throw new Error(`Failed to update time entry: ${response.statusText}`)
    }
    return response.json()
  }

  // 删除时间条目
  static async deleteTimeEntry(id: string): Promise<ApiResponse<void>> {
    const response = await fetchClient(`${API_ROUTES.TIME_ENTRIES}/${id}`, {
      method: "DELETE"
    })
    if (!response.ok) {
      throw new Error(`Failed to delete time entry: ${response.statusText}`)
    }
    return response.json()
  }

  // 开始计时
  static async startTimer(
    projectId?: string,
    taskId?: string,
    description?: string
  ): Promise<ApiResponse<TimeEntry>> {
    const response = await fetchClient(`${API_ROUTES.TIMER}/start`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ projectId, taskId, description })
    })
    if (!response.ok) {
      throw new Error(`Failed to start timer: ${response.statusText}`)
    }
    return response.json()
  }

  // 停止计时
  static async stopTimer(entryId: string): Promise<ApiResponse<TimeEntry>> {
    const response = await fetchClient(`${API_ROUTES.TIMER}/stop`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ entryId })
    })
    if (!response.ok) {
      throw new Error(`Failed to stop timer: ${response.statusText}`)
    }
    return response.json()
  }

  // 获取当前运行的计时器
  static async getCurrentTimer(): Promise<ApiResponse<TimeEntry | null>> {
    const response = await fetchClient(`${API_ROUTES.TIMER}/current`)
    if (!response.ok) {
      throw new Error(`Failed to get current timer: ${response.statusText}`)
    }
    return response.json()
  }
}

// 项目相关API
export class ProjectAPI {
  // 获取项目列表
  static async getProjects(
    filter?: ProjectFilter,
    page = 1,
    limit = 50
  ): Promise<PaginatedResponse<Project>> {
    const params = new URLSearchParams({
      page: page.toString(),
      limit: limit.toString(),
      ...filter
    })
    
    const response = await fetchClient(`${API_ROUTES.PROJECTS}?${params}`)
    if (!response.ok) {
      throw new Error(`Failed to fetch projects: ${response.statusText}`)
    }
    return response.json()
  }

  // 创建项目
  static async createProject(
    project: Omit<Project, "id" | "created_at" | "updated_at">
  ): Promise<ApiResponse<Project>> {
    const response = await fetchClient(API_ROUTES.PROJECTS, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(project)
    })
    if (!response.ok) {
      throw new Error(`Failed to create project: ${response.statusText}`)
    }
    return response.json()
  }

  // 更新项目
  static async updateProject(
    id: string,
    updates: Partial<Project>
  ): Promise<ApiResponse<Project>> {
    const response = await fetchClient(`${API_ROUTES.PROJECTS}/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updates)
    })
    if (!response.ok) {
      throw new Error(`Failed to update project: ${response.statusText}`)
    }
    return response.json()
  }

  // 删除项目
  static async deleteProject(id: string): Promise<ApiResponse<void>> {
    const response = await fetchClient(`${API_ROUTES.PROJECTS}/${id}`, {
      method: "DELETE"
    })
    if (!response.ok) {
      throw new Error(`Failed to delete project: ${response.statusText}`)
    }
    return response.json()
  }

  // 获取项目详情
  static async getProject(id: string): Promise<ApiResponse<Project>> {
    const response = await fetchClient(`${API_ROUTES.PROJECTS}/${id}`)
    if (!response.ok) {
      throw new Error(`Failed to get project: ${response.statusText}`)
    }
    return response.json()
  }
}

// 客户相关API
export class ClientAPI {
  // 获取客户列表
  static async getClients(
    page = 1,
    limit = 50
  ): Promise<PaginatedResponse<Client>> {
    const params = new URLSearchParams({
      page: page.toString(),
      limit: limit.toString()
    })
    
    const response = await fetchClient(`${API_ROUTES.CLIENTS}?${params}`)
    if (!response.ok) {
      throw new Error(`Failed to fetch clients: ${response.statusText}`)
    }
    return response.json()
  }

  // 创建客户
  static async createClient(
    client: Omit<Client, "id" | "created_at" | "updated_at">
  ): Promise<ApiResponse<Client>> {
    const response = await fetchClient(API_ROUTES.CLIENTS, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(client)
    })
    if (!response.ok) {
      throw new Error(`Failed to create client: ${response.statusText}`)
    }
    return response.json()
  }

  // 更新客户
  static async updateClient(
    id: string,
    updates: Partial<Client>
  ): Promise<ApiResponse<Client>> {
    const response = await fetchClient(`${API_ROUTES.CLIENTS}/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updates)
    })
    if (!response.ok) {
      throw new Error(`Failed to update client: ${response.statusText}`)
    }
    return response.json()
  }

  // 删除客户
  static async deleteClient(id: string): Promise<ApiResponse<void>> {
    const response = await fetchClient(`${API_ROUTES.CLIENTS}/${id}`, {
      method: "DELETE"
    })
    if (!response.ok) {
      throw new Error(`Failed to delete client: ${response.statusText}`)
    }
    return response.json()
  }
}

// 任务相关API
export class TaskAPI {
  // 获取任务列表
  static async getTasks(
    projectId?: string,
    page = 1,
    limit = 50
  ): Promise<PaginatedResponse<Task>> {
    const params = new URLSearchParams({
      page: page.toString(),
      limit: limit.toString()
    })
    
    if (projectId) {
      params.append("project_id", projectId)
    }
    
    const response = await fetchClient(`${API_ROUTES.TASKS}?${params}`)
    if (!response.ok) {
      throw new Error(`Failed to fetch tasks: ${response.statusText}`)
    }
    return response.json()
  }

  // 创建任务
  static async createTask(
    task: Omit<Task, "id" | "created_at" | "updated_at">
  ): Promise<ApiResponse<Task>> {
    const response = await fetchClient(API_ROUTES.TASKS, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(task)
    })
    if (!response.ok) {
      throw new Error(`Failed to create task: ${response.statusText}`)
    }
    return response.json()
  }

  // 更新任务
  static async updateTask(
    id: string,
    updates: Partial<Task>
  ): Promise<ApiResponse<Task>> {
    const response = await fetchClient(`${API_ROUTES.TASKS}/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updates)
    })
    if (!response.ok) {
      throw new Error(`Failed to update task: ${response.statusText}`)
    }
    return response.json()
  }

  // 删除任务
  static async deleteTask(id: string): Promise<ApiResponse<void>> {
    const response = await fetchClient(`${API_ROUTES.TASKS}/${id}`, {
      method: "DELETE"
    })
    if (!response.ok) {
      throw new Error(`Failed to delete task: ${response.statusText}`)
    }
    return response.json()
  }
}

// 团队相关API
export class TeamAPI {
  // 获取团队列表
  static async getTeams(
    page = 1,
    limit = 50
  ): Promise<PaginatedResponse<Team>> {
    const params = new URLSearchParams({
      page: page.toString(),
      limit: limit.toString()
    })
    
    const response = await fetchClient(`${API_ROUTES.TEAMS}?${params}`)
    if (!response.ok) {
      throw new Error(`Failed to fetch teams: ${response.statusText}`)
    }
    return response.json()
  }

  // 创建团队
  static async createTeam(
    team: Omit<Team, "id" | "created_at" | "updated_at">
  ): Promise<ApiResponse<Team>> {
    const response = await fetchClient(API_ROUTES.TEAMS, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(team)
    })
    if (!response.ok) {
      throw new Error(`Failed to create team: ${response.statusText}`)
    }
    return response.json()
  }

  // 获取团队成员
  static async getTeamMembers(
    teamId: string
  ): Promise<ApiResponse<TeamMember[]>> {
    const response = await fetchClient(`${API_ROUTES.TEAM_MEMBERS}?team_id=${teamId}`)
    if (!response.ok) {
      throw new Error(`Failed to fetch team members: ${response.statusText}`)
    }
    return response.json()
  }

  // 添加团队成员
  static async addTeamMember(
    member: Omit<TeamMember, "id" | "created_at" | "updated_at">
  ): Promise<ApiResponse<TeamMember>> {
    const response = await fetchClient(API_ROUTES.TEAM_MEMBERS, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(member)
    })
    if (!response.ok) {
      throw new Error(`Failed to add team member: ${response.statusText}`)
    }
    return response.json()
  }

  // 更新团队成员角色
  static async updateTeamMember(
    id: string,
    updates: Partial<TeamMember>
  ): Promise<ApiResponse<TeamMember>> {
    const response = await fetchClient(`${API_ROUTES.TEAM_MEMBERS}/${id}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(updates)
    })
    if (!response.ok) {
      throw new Error(`Failed to update team member: ${response.statusText}`)
    }
    return response.json()
  }

  // 移除团队成员
  static async removeTeamMember(id: string): Promise<ApiResponse<void>> {
    const response = await fetchClient(`${API_ROUTES.TEAM_MEMBERS}/${id}`, {
      method: "DELETE"
    })
    if (!response.ok) {
      throw new Error(`Failed to remove team member: ${response.statusText}`)
    }
    return response.json()
  }
}

// 报告相关API
export class ReportAPI {
  // 获取仪表板数据
  static async getDashboardData(): Promise<ApiResponse<DashboardData>> {
    const response = await fetchClient(API_ROUTES.DASHBOARD)
    if (!response.ok) {
      throw new Error(`Failed to fetch dashboard data: ${response.statusText}`)
    }
    return response.json()
  }

  // 获取汇总报告
  static async getSummaryReport(
    startDate: string,
    endDate: string,
    filter?: TimeEntryFilter
  ): Promise<ApiResponse<ReportData>> {
    const params = new URLSearchParams({
      start_date: startDate,
      end_date: endDate,
      ...filter
    })
    
    const response = await fetchClient(`${API_ROUTES.REPORTS}/summary?${params}`)
    if (!response.ok) {
      throw new Error(`Failed to fetch summary report: ${response.statusText}`)
    }
    return response.json()
  }

  // 获取项目报告
  static async getProjectReport(
    projectId: string,
    startDate: string,
    endDate: string
  ): Promise<ApiResponse<ProjectReport>> {
    const params = new URLSearchParams({
      project_id: projectId,
      start_date: startDate,
      end_date: endDate
    })
    
    const response = await fetchClient(`${API_ROUTES.REPORTS}/project?${params}`)
    if (!response.ok) {
      throw new Error(`Failed to fetch project report: ${response.statusText}`)
    }
    return response.json()
  }

  // 获取团队报告
  static async getTeamReport(
    teamId: string,
    startDate: string,
    endDate: string
  ): Promise<ApiResponse<TeamReport>> {
    const params = new URLSearchParams({
      team_id: teamId,
      start_date: startDate,
      end_date: endDate
    })
    
    const response = await fetchClient(`${API_ROUTES.REPORTS}/team?${params}`)
    if (!response.ok) {
      throw new Error(`Failed to fetch team report: ${response.statusText}`)
    }
    return response.json()
  }

  // 导出报告
  static async exportReport(
    options: ExportOptions
  ): Promise<Blob> {
    const response = await fetchClient(API_ROUTES.EXPORT, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(options)
    })
    if (!response.ok) {
      throw new Error(`Failed to export report: ${response.statusText}`)
    }
    return response.blob()
  }

  // 获取工作负载数据
  static async getWorkloadData(
    teamId?: string
  ): Promise<ApiResponse<WorkloadData[]>> {
    const params = teamId ? `?team_id=${teamId}` : ""
    const response = await fetchClient(`${API_ROUTES.REPORTS}/workload${params}`)
    if (!response.ok) {
      throw new Error(`Failed to fetch workload data: ${response.statusText}`)
    }
    return response.json()
  }
}

// 标签相关API
export class TagAPI {
  // 获取标签列表
  static async getTags(): Promise<ApiResponse<Tag[]>> {
    const response = await fetchClient(API_ROUTES.TAGS)
    if (!response.ok) {
      throw new Error(`Failed to fetch tags: ${response.statusText}`)
    }
    return response.json()
  }

  // 创建标签
  static async createTag(
    tag: Omit<Tag, "id" | "created_at">
  ): Promise<ApiResponse<Tag>> {
    const response = await fetchClient(API_ROUTES.TAGS, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(tag)
    })
    if (!response.ok) {
      throw new Error(`Failed to create tag: ${response.statusText}`)
    }
    return response.json()
  }

  // 删除标签
  static async deleteTag(id: string): Promise<ApiResponse<void>> {
    const response = await fetchClient(`${API_ROUTES.TAGS}/${id}`, {
      method: "DELETE"
    })
    if (!response.ok) {
      throw new Error(`Failed to delete tag: ${response.statusText}`)
    }
    return response.json()
  }
}