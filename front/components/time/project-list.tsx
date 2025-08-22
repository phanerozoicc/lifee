"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardHeader } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { MoreHorizontal, Plus, Search, Filter, Edit, Trash2, Users, Clock, DollarSign } from "lucide-react"
import { Project, ProjectStatus } from "@/lib/time/types"
import { ProjectDialog } from "./project-dialog"
import { cn } from "@/lib/utils"

interface ProjectListProps {
  projects: Project[]
  loading?: boolean
  onEdit?: (project: Project) => void
  onDelete?: (projectId: string) => void
  onCreateNew?: () => void
}

const statusColors = {
  [ProjectStatus.ACTIVE]: "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300",
  [ProjectStatus.COMPLETED]: "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300",
  [ProjectStatus.ON_HOLD]: "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300",
  [ProjectStatus.CANCELLED]: "bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300"
}

const statusLabels = {
  [ProjectStatus.ACTIVE]: "进行中",
  [ProjectStatus.COMPLETED]: "已完成",
  [ProjectStatus.ON_HOLD]: "暂停",
  [ProjectStatus.CANCELLED]: "已取消"
}

export function ProjectList({ projects, loading, onEdit, onDelete, onCreateNew }: ProjectListProps) {
  const [searchTerm, setSearchTerm] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [showDialog, setShowDialog] = useState(false)
  const [editingProject, setEditingProject] = useState<Project | null>(null)

  const filteredProjects = projects.filter(project => {
    const matchesSearch = project.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         project.client?.name.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = statusFilter === "all" || project.status === statusFilter
    return matchesSearch && matchesStatus
  })

  const handleEdit = (project: Project) => {
    setEditingProject(project)
    setShowDialog(true)
    onEdit?.(project)
  }

  const handleCreateNew = () => {
    setEditingProject(null)
    setShowDialog(true)
    onCreateNew?.()
  }

  const handleCloseDialog = () => {
    setShowDialog(false)
    setEditingProject(null)
  }

  if (loading) {
    return (
      <div className="space-y-4">
        {[...Array(5)].map((_, i) => (
          <Card key={i} className="animate-pulse">
            <CardHeader>
              <div className="h-4 bg-muted rounded w-1/3"></div>
              <div className="h-3 bg-muted rounded w-1/2"></div>
            </CardHeader>
            <CardContent>
              <div className="h-3 bg-muted rounded w-full"></div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* 搜索和筛选 */}
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="搜索项目或客户..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
        <div className="flex gap-2">
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-32">
              <Filter className="h-4 w-4 mr-2" />
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">全部状态</SelectItem>
              <SelectItem value={ProjectStatus.ACTIVE}>进行中</SelectItem>
              <SelectItem value={ProjectStatus.COMPLETED}>已完成</SelectItem>
              <SelectItem value={ProjectStatus.ON_HOLD}>暂停</SelectItem>
              <SelectItem value={ProjectStatus.CANCELLED}>已取消</SelectItem>
            </SelectContent>
          </Select>
          <Button onClick={handleCreateNew}>
            <Plus className="h-4 w-4 mr-2" />
            新建项目
          </Button>
        </div>
      </div>

      {/* 项目列表 */}
      <div className="border rounded-lg">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>项目名称</TableHead>
              <TableHead>客户</TableHead>
              <TableHead>状态</TableHead>
              <TableHead>团队成员</TableHead>
              <TableHead>总时间</TableHead>
              <TableHead>预算</TableHead>
              <TableHead>进度</TableHead>
              <TableHead className="w-12"></TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {filteredProjects.length === 0 ? (
              <TableRow>
                <TableCell colSpan={8} className="text-center py-8 text-muted-foreground">
                  {searchTerm || statusFilter !== "all" ? "未找到匹配的项目" : "暂无项目"}
                </TableCell>
              </TableRow>
            ) : (
              filteredProjects.map((project) => {
                const progress = project.budget && project.budget > 0 
                  ? Math.min((project.totalHours * (project.hourlyRate || 0)) / project.budget * 100, 100)
                  : 0

                return (
                  <TableRow key={project.id}>
                    <TableCell>
                      <div>
                        <div className="font-medium">{project.name}</div>
                        {project.description && (
                          <div className="text-sm text-muted-foreground truncate max-w-xs">
                            {project.description}
                          </div>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>
                      {project.client ? (
                        <div>
                          <div className="font-medium">{project.client.name}</div>
                          {project.client.email && (
                            <div className="text-sm text-muted-foreground">{project.client.email}</div>
                          )}
                        </div>
                      ) : (
                        <span className="text-muted-foreground">无客户</span>
                      )}
                    </TableCell>
                    <TableCell>
                      <Badge className={cn("text-xs", statusColors[project.status])}>
                        {statusLabels[project.status]}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1">
                        <Users className="h-4 w-4 text-muted-foreground" />
                        <span>{project.teamMembers?.length || 0}</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-1">
                        <Clock className="h-4 w-4 text-muted-foreground" />
                        <span>{project.totalHours.toFixed(1)}h</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      {project.budget ? (
                        <div className="flex items-center gap-1">
                          <DollarSign className="h-4 w-4 text-muted-foreground" />
                          <span>¥{project.budget.toLocaleString()}</span>
                        </div>
                      ) : (
                        <span className="text-muted-foreground">未设置</span>
                      )}
                    </TableCell>
                    <TableCell>
                      {project.budget && project.budget > 0 ? (
                        <div className="w-full">
                          <div className="flex justify-between text-xs mb-1">
                            <span>{progress.toFixed(0)}%</span>
                          </div>
                          <div className="w-full bg-muted rounded-full h-2">
                            <div 
                              className={cn(
                                "h-2 rounded-full transition-all",
                                progress > 90 ? "bg-red-500" : progress > 75 ? "bg-yellow-500" : "bg-green-500"
                              )}
                              style={{ width: `${Math.min(progress, 100)}%` }}
                            />
                          </div>
                        </div>
                      ) : (
                        <span className="text-muted-foreground text-xs">无预算</span>
                      )}
                    </TableCell>
                    <TableCell>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="sm">
                            <MoreHorizontal className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => handleEdit(project)}>
                            <Edit className="h-4 w-4 mr-2" />
                            编辑
                          </DropdownMenuItem>
                          <DropdownMenuItem 
                            onClick={() => onDelete?.(project.id)}
                            className="text-destructive"
                          >
                            <Trash2 className="h-4 w-4 mr-2" />
                            删除
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                )
              })
            )}
          </TableBody>
        </Table>
      </div>

      {/* 项目对话框 */}
      <ProjectDialog
        open={showDialog}
        onOpenChange={handleCloseDialog}
        project={editingProject}
      />
    </div>
  )
}