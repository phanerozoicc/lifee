// 时间条目状态
export enum TimeEntryStatus {
  RUNNING = "RUNNING",
  STOPPED = "STOPPED",
  PAUSED = "PAUSED"
}

// 项目状态
export enum ProjectStatus {
  ACTIVE = "ACTIVE",
  ARCHIVED = "ARCHIVED",
  COMPLETED = "COMPLETED",
  ON_HOLD = "ON_HOLD"
}

// 团队成员角色
export enum TeamRole {
  OWNER = "OWNER",
  ADMIN = "ADMIN",
  MANAGER = "MANAGER",
  MEMBER = "MEMBER",
  VIEWER = "VIEWER"
}

// 计费类型
export enum BillingType {
  HOURLY = "HOURLY",
  FIXED = "FIXED",
  NON_BILLABLE = "NON_BILLABLE"
}

// 报告类型
export enum ReportType {
  SUMMARY = "SUMMARY",
  DETAILED = "DETAILED",
  WEEKLY = "WEEKLY",
  MONTHLY = "MONTHLY",
  PROJECT = "PROJECT",
  TEAM = "TEAM"
}

// 时间条目接口
export interface TimeEntry {
  id: string
  user_id: string
  project_id?: string
  task_id?: string
  description?: string
  start_time: string
  end_time?: string
  duration?: number // 秒数
  status: TimeEntryStatus
  tags?: string[]
  billable: boolean
  billing_rate?: number
  created_at: string
  updated_at: string
}

// 项目接口
export interface Project {
  id: string
  name: string
  description?: string
  client_id?: string
  color: string
  status: ProjectStatus
  billing_type: BillingType
  hourly_rate?: number
  budget?: number
  budget_type?: "HOURS" | "AMOUNT"
  start_date?: string
  end_date?: string
  created_by: string
  team_id?: string
  created_at: string
  updated_at: string
}

// 客户接口
export interface Client {
  id: string
  name: string
  email?: string
  company?: string
  address?: string
  phone?: string
  notes?: string
  created_by: string
  created_at: string
  updated_at: string
}

// 任务接口
export interface Task {
  id: string
  project_id: string
  name: string
  description?: string
  estimated_hours?: number
  completed: boolean
  assigned_to?: string
  due_date?: string
  created_by: string
  created_at: string
  updated_at: string
}

// 团队接口
export interface Team {
  id: string
  name: string
  description?: string
  created_by: string
  created_at: string
  updated_at: string
}

// 团队成员接口
export interface TeamMember {
  id: string
  team_id: string
  user_id: string
  role: TeamRole
  hourly_rate?: number
  joined_at: string
  created_at: string
  updated_at: string
}

// 标签接口
export interface Tag {
  id: string
  name: string
  color: string
  created_by: string
  created_at: string
}

// 报告数据接口
export interface ReportData {
  total_duration: number
  total_billable_duration: number
  total_amount: number
  entries_count: number
  projects_count: number
  average_daily_hours: number
  period_start: string
  period_end: string
}

// 项目报告接口
export interface ProjectReport {
  project: Project
  total_duration: number
  billable_duration: number
  total_amount: number
  entries_count: number
  team_members: {
    user_id: string
    user_name: string
    duration: number
    amount: number
  }[]
}

// 团队报告接口
export interface TeamReport {
  team: Team
  members: {
    user_id: string
    user_name: string
    total_duration: number
    billable_duration: number
    total_amount: number
    projects_count: number
  }[]
  total_duration: number
  total_amount: number
}

// 时间追踪器状态接口
export interface TimerState {
  isRunning: boolean
  currentEntry?: TimeEntry
  startTime?: Date
  elapsedTime: number
  project?: Project
  task?: Task
  description: string
}

// API响应接口
export interface ApiResponse<T> {
  data: T
  success: boolean
  message?: string
  error?: string
}

// 分页接口
export interface PaginatedResponse<T> {
  data: T[]
  total: number
  page: number
  limit: number
  totalPages: number
}

// 过滤器接口
export interface TimeEntryFilter {
  user_id?: string
  project_id?: string
  client_id?: string
  start_date?: string
  end_date?: string
  billable?: boolean
  tags?: string[]
  description?: string
}

// 项目过滤器接口
export interface ProjectFilter {
  status?: ProjectStatus
  client_id?: string
  team_id?: string
  created_by?: string
  search?: string
}

// 导出选项接口
export interface ExportOptions {
  format: "CSV" | "PDF" | "EXCEL"
  date_range: {
    start: string
    end: string
  }
  include_details: boolean
  group_by?: "PROJECT" | "CLIENT" | "USER" | "DATE"
  filters?: TimeEntryFilter
}

// 仪表板数据接口
export interface DashboardData {
  today_duration: number
  week_duration: number
  month_duration: number
  active_projects: number
  recent_entries: TimeEntry[]
  top_projects: {
    project: Project
    duration: number
    percentage: number
  }[]
  productivity_trend: {
    date: string
    duration: number
  }[]
}

// 用户扩展接口（继承现有用户类型）
export interface TimeUser {
  id: string
  email: string
  display_name: string
  profile_image?: string
  timezone?: string
  default_hourly_rate?: number
  weekly_capacity?: number // 每周工作小时数
  created_at: string
  updated_at: string
}

// 工作负载数据接口
export interface WorkloadData {
  user: TimeUser
  current_week_hours: number
  capacity_percentage: number
  active_projects: Project[]
  upcoming_deadlines: {
    task: Task
    project: Project
    days_remaining: number
  }[]
}