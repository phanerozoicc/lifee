"use client"

import { useEffect, useState } from "react"
import { Plus, Search, Filter } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import { ProjectForm } from "@/components/time/project-form"
import { ClientForm } from "@/components/time/client-form"
import { TaskForm } from "@/components/time/task-form"
import { ProjectCard } from "@/components/time/project-card"
import { ClientCard } from "@/components/time/client-card"
import { TaskList } from "@/components/time/task-list"
import { useProjectStore, useClientStore, useTaskStore } from "@/lib/time/store"
import { useUserStore } from "@/lib/user/store"
import type { Project } from "@/lib/time/types"

export default function ProjectsPage() {
  const { user } = useUserStore()
  const {
    projects,
    loading: projectsLoading,
    fetchProjects,
    setFilter: setProjectFilter,
    clearFilter: clearProjectFilter
  } = useProjectStore()
  const {
    clients,
    loading: clientsLoading,
    fetchClients
  } = useClientStore()
  const {
    tasks,
    loading: tasksLoading,
    fetchTasks
  } = useTaskStore()

  const [searchTerm, setSearchTerm] = useState("")
  const [selectedProject, setSelectedProject] = useState<Project | null>(null)
  const [showProjectForm, setShowProjectForm] = useState(false)
  const [showClientForm, setShowClientForm] = useState(false)
  const [showTaskForm, setShowTaskForm] = useState(false)

  useEffect(() => {
    if (user) {
      fetchProjects()
      fetchClients()
      fetchTasks()
    }
  }, [user, fetchProjects, fetchClients, fetchTasks])

  // 搜索过滤
  useEffect(() => {
    if (searchTerm) {
      setProjectFilter({ search: searchTerm })
    } else {
      clearProjectFilter()
    }
  }, [searchTerm, setProjectFilter, clearProjectFilter])

  const handleProjectSelect = (project: Project) => {
    setSelectedProject(project)
    fetchTasks(project.id)
  }

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Card className="w-96">
          <CardHeader>
            <CardTitle>请先登录</CardTitle>
            <CardDescription>
              您需要登录才能管理项目
            </CardDescription>
          </CardHeader>
        </Card>
      </div>
    )
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* 页面标题和操作 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">项目管理</h1>
          <p className="text-muted-foreground">
            管理您的项目、客户和任务
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Dialog open={showClientForm} onOpenChange={setShowClientForm}>
            <DialogTrigger asChild>
              <Button variant="outline">
                <Plus className="h-4 w-4 mr-2" />
                新建客户
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>创建新客户</DialogTitle>
                <DialogDescription>
                  添加新的客户信息
                </DialogDescription>
              </DialogHeader>
              <ClientForm onSuccess={() => setShowClientForm(false)} />
            </DialogContent>
          </Dialog>
          
          <Dialog open={showProjectForm} onOpenChange={setShowProjectForm}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="h-4 w-4 mr-2" />
                新建项目
              </Button>
            </DialogTrigger>
            <DialogContent className="max-w-2xl">
              <DialogHeader>
                <DialogTitle>创建新项目</DialogTitle>
                <DialogDescription>
                  设置项目基本信息、预算和团队成员
                </DialogDescription>
              </DialogHeader>
              <ProjectForm onSuccess={() => setShowProjectForm(false)} />
            </DialogContent>
          </Dialog>
        </div>
      </div>

      {/* 搜索和过滤 */}
      <div className="flex items-center gap-4">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-muted-foreground h-4 w-4" />
          <Input
            placeholder="搜索项目..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
        <Button variant="outline" size="sm">
          <Filter className="h-4 w-4 mr-2" />
          筛选
        </Button>
      </div>

      {/* 主要内容 */}
      <Tabs defaultValue="projects" className="w-full">
        <TabsList>
          <TabsTrigger value="projects">项目</TabsTrigger>
          <TabsTrigger value="clients">客户</TabsTrigger>
          <TabsTrigger value="tasks">任务</TabsTrigger>
        </TabsList>

        {/* 项目列表 */}
        <TabsContent value="projects" className="space-y-4">
          {projectsLoading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {[...Array(6)].map((_, i) => (
                <Card key={i} className="animate-pulse">
                  <CardHeader>
                    <div className="h-4 bg-muted rounded w-3/4"></div>
                    <div className="h-3 bg-muted rounded w-1/2"></div>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-2">
                      <div className="h-3 bg-muted rounded"></div>
                      <div className="h-3 bg-muted rounded w-2/3"></div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : projects.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <div className="text-6xl mb-4">📋</div>
                <h3 className="text-lg font-semibold mb-2">暂无项目</h3>
                <p className="text-muted-foreground text-center mb-4">
                  创建您的第一个项目来开始时间追踪
                </p>
                <Button onClick={() => setShowProjectForm(true)}>
                  <Plus className="h-4 w-4 mr-2" />
                  创建项目
                </Button>
              </CardContent>
            </Card>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {projects.map((project) => (
                <ProjectCard
                  key={project.id}
                  project={project}
                  onClick={() => handleProjectSelect(project)}
                />
              ))}
            </div>
          )}
        </TabsContent>

        {/* 客户列表 */}
        <TabsContent value="clients" className="space-y-4">
          {clientsLoading ? (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {[...Array(6)].map((_, i) => (
                <Card key={i} className="animate-pulse">
                  <CardHeader>
                    <div className="h-4 bg-muted rounded w-3/4"></div>
                    <div className="h-3 bg-muted rounded w-1/2"></div>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-2">
                      <div className="h-3 bg-muted rounded"></div>
                      <div className="h-3 bg-muted rounded w-2/3"></div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : clients.length === 0 ? (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <div className="text-6xl mb-4">👥</div>
                <h3 className="text-lg font-semibold mb-2">暂无客户</h3>
                <p className="text-muted-foreground text-center mb-4">
                  添加客户信息来更好地管理项目
                </p>
                <Button onClick={() => setShowClientForm(true)}>
                  <Plus className="h-4 w-4 mr-2" />
                  添加客户
                </Button>
              </CardContent>
            </Card>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              {clients.map((client) => (
                <ClientCard key={client.id} client={client} />
              ))}
            </div>
          )}
        </TabsContent>

        {/* 任务列表 */}
        <TabsContent value="tasks" className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-lg font-semibold">
                {selectedProject ? `${selectedProject.name} - 任务` : "所有任务"}
              </h3>
              <p className="text-sm text-muted-foreground">
                {selectedProject ? selectedProject.description : "查看和管理所有任务"}
              </p>
            </div>
            <Dialog open={showTaskForm} onOpenChange={setShowTaskForm}>
              <DialogTrigger asChild>
                <Button>
                  <Plus className="h-4 w-4 mr-2" />
                  新建任务
                </Button>
              </DialogTrigger>
              <DialogContent>
                <DialogHeader>
                  <DialogTitle>创建新任务</DialogTitle>
                  <DialogDescription>
                    为项目添加新的任务
                  </DialogDescription>
                </DialogHeader>
                <TaskForm 
                  projectId={selectedProject?.id}
                  onSuccess={() => setShowTaskForm(false)} 
                />
              </DialogContent>
            </Dialog>
          </div>
          
          <TaskList 
            tasks={tasks} 
            loading={tasksLoading}
            projectId={selectedProject?.id}
          />
        </TabsContent>
      </Tabs>
    </div>
  )
}