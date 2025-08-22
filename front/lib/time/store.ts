import { create } from "zustand"
import { persist } from "zustand/middleware"
import type {
  TimeEntry,
  Project,
  Client,
  Task,
  Team,
  TeamMember,
  Tag,
  DashboardData,
  TimeEntryFilter,
  ProjectFilter
} from "./types"
import {
  TimeEntryAPI,
  ProjectAPI,
  ClientAPI,
  TaskAPI,
  TeamAPI,
  ReportAPI,
  TagAPI
} from "./api"

// 计时器状态管理
interface TimerStore {
  // 状态
  isRunning: boolean
  currentEntry: TimeEntry | null
  startTime: Date | null
  elapsedTime: number
  selectedProject: Project | null
  selectedTask: Task | null
  description: string
  
  // 动作
  startTimer: (projectId?: string, taskId?: string, description?: string) => Promise<void>
  stopTimer: () => Promise<void>
  pauseTimer: () => void
  resumeTimer: () => void
  updateDescription: (description: string) => void
  setProject: (project: Project | null) => void
  setTask: (task: Task | null) => void
  updateElapsedTime: () => void
  reset: () => void
}

export const useTimerStore = create<TimerStore>()(
  persist(
    (set, get) => ({
      // 初始状态
      isRunning: false,
      currentEntry: null,
      startTime: null,
      elapsedTime: 0,
      selectedProject: null,
      selectedTask: null,
      description: "",

      // 开始计时
      startTimer: async (projectId, taskId, description = "") => {
        try {
          const response = await TimeEntryAPI.startTimer(projectId, taskId, description)
          if (response.success && response.data) {
            set({
              isRunning: true,
              currentEntry: response.data,
              startTime: new Date(response.data.start_time),
              elapsedTime: 0,
              description
            })
          }
        } catch (error) {
          console.error("Failed to start timer:", error)
          throw error
        }
      },

      // 停止计时
      stopTimer: async () => {
        const { currentEntry } = get()
        if (!currentEntry) return

        try {
          const response = await TimeEntryAPI.stopTimer(currentEntry.id)
          if (response.success) {
            set({
              isRunning: false,
              currentEntry: null,
              startTime: null,
              elapsedTime: 0
            })
          }
        } catch (error) {
          console.error("Failed to stop timer:", error)
          throw error
        }
      },

      // 暂停计时
      pauseTimer: () => {
        set({ isRunning: false })
      },

      // 恢复计时
      resumeTimer: () => {
        set({ isRunning: true })
      },

      // 更新描述
      updateDescription: (description: string) => {
        set({ description })
      },

      // 设置项目
      setProject: (project: Project | null) => {
        set({ selectedProject: project, selectedTask: null })
      },

      // 设置任务
      setTask: (task: Task | null) => {
        set({ selectedTask: task })
      },

      // 更新已用时间
      updateElapsedTime: () => {
        const { startTime, isRunning } = get()
        if (startTime && isRunning) {
          const now = new Date()
          const elapsed = Math.floor((now.getTime() - startTime.getTime()) / 1000)
          set({ elapsedTime: elapsed })
        }
      },

      // 重置状态
      reset: () => {
        set({
          isRunning: false,
          currentEntry: null,
          startTime: null,
          elapsedTime: 0,
          selectedProject: null,
          selectedTask: null,
          description: ""
        })
      }
    }),
    {
      name: "timer-store",
      partialize: (state) => ({
        selectedProject: state.selectedProject,
        selectedTask: state.selectedTask,
        description: state.description
      })
    }
  )
)

// 时间条目状态管理
interface TimeEntryStore {
  // 状态
  entries: TimeEntry[]
  loading: boolean
  filter: TimeEntryFilter
  currentPage: number
  totalPages: number
  total: number
  
  // 动作
  fetchEntries: (page?: number) => Promise<void>
  createEntry: (entry: Omit<TimeEntry, "id" | "created_at" | "updated_at">) => Promise<void>
  updateEntry: (id: string, updates: Partial<TimeEntry>) => Promise<void>
  deleteEntry: (id: string) => Promise<void>
  setFilter: (filter: Partial<TimeEntryFilter>) => void
  clearFilter: () => void
}

export const useTimeEntryStore = create<TimeEntryStore>()((set, get) => ({
  // 初始状态
  entries: [],
  loading: false,
  filter: {},
  currentPage: 1,
  totalPages: 1,
  total: 0,

  // 获取时间条目
  fetchEntries: async (page = 1) => {
    set({ loading: true })
    try {
      const { filter } = get()
      const response = await TimeEntryAPI.getTimeEntries(filter, page, 50)
      set({
        entries: response.data,
        currentPage: response.page,
        totalPages: response.totalPages,
        total: response.total,
        loading: false
      })
    } catch (error) {
      console.error("Failed to fetch time entries:", error)
      set({ loading: false })
      throw error
    }
  },

  // 创建时间条目
  createEntry: async (entry) => {
    try {
      const response = await TimeEntryAPI.createTimeEntry(entry)
      if (response.success && response.data) {
        const { entries } = get()
        set({ entries: [response.data, ...entries] })
      }
    } catch (error) {
      console.error("Failed to create time entry:", error)
      throw error
    }
  },

  // 更新时间条目
  updateEntry: async (id, updates) => {
    try {
      const response = await TimeEntryAPI.updateTimeEntry(id, updates)
      if (response.success && response.data) {
        const { entries } = get()
        const updatedEntries = entries.map(entry => 
          entry.id === id ? response.data! : entry
        )
        set({ entries: updatedEntries })
      }
    } catch (error) {
      console.error("Failed to update time entry:", error)
      throw error
    }
  },

  // 删除时间条目
  deleteEntry: async (id) => {
    try {
      await TimeEntryAPI.deleteTimeEntry(id)
      const { entries } = get()
      const filteredEntries = entries.filter(entry => entry.id !== id)
      set({ entries: filteredEntries })
    } catch (error) {
      console.error("Failed to delete time entry:", error)
      throw error
    }
  },

  // 设置过滤器
  setFilter: (newFilter) => {
    const { filter } = get()
    set({ filter: { ...filter, ...newFilter } })
  },

  // 清除过滤器
  clearFilter: () => {
    set({ filter: {} })
  }
}))

// 项目状态管理
interface ProjectStore {
  // 状态
  projects: Project[]
  loading: boolean
  filter: ProjectFilter
  currentPage: number
  totalPages: number
  total: number
  
  // 动作
  fetchProjects: (page?: number) => Promise<void>
  createProject: (project: Omit<Project, "id" | "created_at" | "updated_at">) => Promise<void>
  updateProject: (id: string, updates: Partial<Project>) => Promise<void>
  deleteProject: (id: string) => Promise<void>
  setFilter: (filter: Partial<ProjectFilter>) => void
  clearFilter: () => void
}

export const useProjectStore = create<ProjectStore>()((set, get) => ({
  // 初始状态
  projects: [],
  loading: false,
  filter: {},
  currentPage: 1,
  totalPages: 1,
  total: 0,

  // 获取项目
  fetchProjects: async (page = 1) => {
    set({ loading: true })
    try {
      const { filter } = get()
      const response = await ProjectAPI.getProjects(filter, page, 50)
      set({
        projects: response.data,
        currentPage: response.page,
        totalPages: response.totalPages,
        total: response.total,
        loading: false
      })
    } catch (error) {
      console.error("Failed to fetch projects:", error)
      set({ loading: false })
      throw error
    }
  },

  // 创建项目
  createProject: async (project) => {
    try {
      const response = await ProjectAPI.createProject(project)
      if (response.success && response.data) {
        const { projects } = get()
        set({ projects: [response.data, ...projects] })
      }
    } catch (error) {
      console.error("Failed to create project:", error)
      throw error
    }
  },

  // 更新项目
  updateProject: async (id, updates) => {
    try {
      const response = await ProjectAPI.updateProject(id, updates)
      if (response.success && response.data) {
        const { projects } = get()
        const updatedProjects = projects.map(project => 
          project.id === id ? response.data! : project
        )
        set({ projects: updatedProjects })
      }
    } catch (error) {
      console.error("Failed to update project:", error)
      throw error
    }
  },

  // 删除项目
  deleteProject: async (id) => {
    try {
      await ProjectAPI.deleteProject(id)
      const { projects } = get()
      const filteredProjects = projects.filter(project => project.id !== id)
      set({ projects: filteredProjects })
    } catch (error) {
      console.error("Failed to delete project:", error)
      throw error
    }
  },

  // 设置过滤器
  setFilter: (newFilter) => {
    const { filter } = get()
    set({ filter: { ...filter, ...newFilter } })
  },

  // 清除过滤器
  clearFilter: () => {
    set({ filter: {} })
  }
}))

// 客户状态管理
interface ClientStore {
  clients: Client[]
  loading: boolean
  fetchClients: () => Promise<void>
  createClient: (client: Omit<Client, "id" | "created_at" | "updated_at">) => Promise<void>
  updateClient: (id: string, updates: Partial<Client>) => Promise<void>
  deleteClient: (id: string) => Promise<void>
}

export const useClientStore = create<ClientStore>()((set, get) => ({
  clients: [],
  loading: false,

  fetchClients: async () => {
    set({ loading: true })
    try {
      const response = await ClientAPI.getClients(1, 100)
      set({ clients: response.data, loading: false })
    } catch (error) {
      console.error("Failed to fetch clients:", error)
      set({ loading: false })
      throw error
    }
  },

  createClient: async (client) => {
    try {
      const response = await ClientAPI.createClient(client)
      if (response.success && response.data) {
        const { clients } = get()
        set({ clients: [response.data, ...clients] })
      }
    } catch (error) {
      console.error("Failed to create client:", error)
      throw error
    }
  },

  updateClient: async (id, updates) => {
    try {
      const response = await ClientAPI.updateClient(id, updates)
      if (response.success && response.data) {
        const { clients } = get()
        const updatedClients = clients.map(client => 
          client.id === id ? response.data! : client
        )
        set({ clients: updatedClients })
      }
    } catch (error) {
      console.error("Failed to update client:", error)
      throw error
    }
  },

  deleteClient: async (id) => {
    try {
      await ClientAPI.deleteClient(id)
      const { clients } = get()
      const filteredClients = clients.filter(client => client.id !== id)
      set({ clients: filteredClients })
    } catch (error) {
      console.error("Failed to delete client:", error)
      throw error
    }
  }
}))

// 任务状态管理
interface TaskStore {
  tasks: Task[]
  loading: boolean
  fetchTasks: (projectId?: string) => Promise<void>
  createTask: (task: Omit<Task, "id" | "created_at" | "updated_at">) => Promise<void>
  updateTask: (id: string, updates: Partial<Task>) => Promise<void>
  deleteTask: (id: string) => Promise<void>
}

export const useTaskStore = create<TaskStore>()((set, get) => ({
  tasks: [],
  loading: false,

  fetchTasks: async (projectId) => {
    set({ loading: true })
    try {
      const response = await TaskAPI.getTasks(projectId, 1, 100)
      set({ tasks: response.data, loading: false })
    } catch (error) {
      console.error("Failed to fetch tasks:", error)
      set({ loading: false })
      throw error
    }
  },

  createTask: async (task) => {
    try {
      const response = await TaskAPI.createTask(task)
      if (response.success && response.data) {
        const { tasks } = get()
        set({ tasks: [response.data, ...tasks] })
      }
    } catch (error) {
      console.error("Failed to create task:", error)
      throw error
    }
  },

  updateTask: async (id, updates) => {
    try {
      const response = await TaskAPI.updateTask(id, updates)
      if (response.success && response.data) {
        const { tasks } = get()
        const updatedTasks = tasks.map(task => 
          task.id === id ? response.data! : task
        )
        set({ tasks: updatedTasks })
      }
    } catch (error) {
      console.error("Failed to update task:", error)
      throw error
    }
  },

  deleteTask: async (id) => {
    try {
      await TaskAPI.deleteTask(id)
      const { tasks } = get()
      const filteredTasks = tasks.filter(task => task.id !== id)
      set({ tasks: filteredTasks })
    } catch (error) {
      console.error("Failed to delete task:", error)
      throw error
    }
  }
}))

// 团队状态管理
interface TeamStore {
  teams: Team[]
  teamMembers: { [teamId: string]: TeamMember[] }
  loading: boolean
  fetchTeams: () => Promise<void>
  fetchTeamMembers: (teamId: string) => Promise<void>
  createTeam: (team: Omit<Team, "id" | "created_at" | "updated_at">) => Promise<void>
  addTeamMember: (member: Omit<TeamMember, "id" | "created_at" | "updated_at">) => Promise<void>
  updateTeamMember: (id: string, updates: Partial<TeamMember>) => Promise<void>
  removeTeamMember: (id: string, teamId: string) => Promise<void>
}

export const useTeamStore = create<TeamStore>()((set, get) => ({
  teams: [],
  teamMembers: {},
  loading: false,

  fetchTeams: async () => {
    set({ loading: true })
    try {
      const response = await TeamAPI.getTeams(1, 100)
      set({ teams: response.data, loading: false })
    } catch (error) {
      console.error("Failed to fetch teams:", error)
      set({ loading: false })
      throw error
    }
  },

  fetchTeamMembers: async (teamId) => {
    try {
      const response = await TeamAPI.getTeamMembers(teamId)
      if (response.success && response.data) {
        const { teamMembers } = get()
        set({ 
          teamMembers: { 
            ...teamMembers, 
            [teamId]: response.data 
          } 
        })
      }
    } catch (error) {
      console.error("Failed to fetch team members:", error)
      throw error
    }
  },

  createTeam: async (team) => {
    try {
      const response = await TeamAPI.createTeam(team)
      if (response.success && response.data) {
        const { teams } = get()
        set({ teams: [response.data, ...teams] })
      }
    } catch (error) {
      console.error("Failed to create team:", error)
      throw error
    }
  },

  addTeamMember: async (member) => {
    try {
      const response = await TeamAPI.addTeamMember(member)
      if (response.success && response.data) {
        const { teamMembers } = get()
        const currentMembers = teamMembers[member.team_id] || []
        set({ 
          teamMembers: { 
            ...teamMembers, 
            [member.team_id]: [response.data, ...currentMembers] 
          } 
        })
      }
    } catch (error) {
      console.error("Failed to add team member:", error)
      throw error
    }
  },

  updateTeamMember: async (id, updates) => {
    try {
      const response = await TeamAPI.updateTeamMember(id, updates)
      if (response.success && response.data) {
        const { teamMembers } = get()
        const teamId = response.data.team_id
        const currentMembers = teamMembers[teamId] || []
        const updatedMembers = currentMembers.map(member => 
          member.id === id ? response.data! : member
        )
        set({ 
          teamMembers: { 
            ...teamMembers, 
            [teamId]: updatedMembers 
          } 
        })
      }
    } catch (error) {
      console.error("Failed to update team member:", error)
      throw error
    }
  },

  removeTeamMember: async (id, teamId) => {
    try {
      await TeamAPI.removeTeamMember(id)
      const { teamMembers } = get()
      const currentMembers = teamMembers[teamId] || []
      const filteredMembers = currentMembers.filter(member => member.id !== id)
      set({ 
        teamMembers: { 
          ...teamMembers, 
          [teamId]: filteredMembers 
        } 
      })
    } catch (error) {
      console.error("Failed to remove team member:", error)
      throw error
    }
  }
}))

// 标签状态管理
interface TagStore {
  tags: Tag[]
  loading: boolean
  fetchTags: () => Promise<void>
  createTag: (tag: Omit<Tag, "id" | "created_at">) => Promise<void>
  deleteTag: (id: string) => Promise<void>
}

export const useTagStore = create<TagStore>()((set, get) => ({
  tags: [],
  loading: false,

  fetchTags: async () => {
    set({ loading: true })
    try {
      const response = await TagAPI.getTags()
      if (response.success && response.data) {
        set({ tags: response.data, loading: false })
      }
    } catch (error) {
      console.error("Failed to fetch tags:", error)
      set({ loading: false })
      throw error
    }
  },

  createTag: async (tag) => {
    try {
      const response = await TagAPI.createTag(tag)
      if (response.success && response.data) {
        const { tags } = get()
        set({ tags: [response.data, ...tags] })
      }
    } catch (error) {
      console.error("Failed to create tag:", error)
      throw error
    }
  },

  deleteTag: async (id) => {
    try {
      await TagAPI.deleteTag(id)
      const { tags } = get()
      const filteredTags = tags.filter(tag => tag.id !== id)
      set({ tags: filteredTags })
    } catch (error) {
      console.error("Failed to delete tag:", error)
      throw error
    }
  }
}))

// 仪表板状态管理
interface DashboardStore {
  data: DashboardData | null
  loading: boolean
  fetchDashboardData: () => Promise<void>
}

export const useDashboardStore = create<DashboardStore>()((set) => ({
  data: null,
  loading: false,

  fetchDashboardData: async () => {
    set({ loading: true })
    try {
      const response = await ReportAPI.getDashboardData()
      if (response.success && response.data) {
        set({ data: response.data, loading: false })
      }
    } catch (error) {
      console.error("Failed to fetch dashboard data:", error)
      set({ loading: false })
      throw error
    }
  }
}))