"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Dialog, DialogTrigger } from "@/components/ui/dialog"
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog"
import { Search, Plus, MoreHorizontal, Edit, Trash2, CheckSquare, Clock, Calendar, User } from "lucide-react"
import { useTaskStore, useProjectStore } from "@/lib/time/store"
import { Task, TaskStatus } from "@/lib/time/types"
import { TaskDialog } from "./task-dialog"
import { cn } from "@/lib/utils"

interface TaskListProps {
  projectId?: string
  className?: string
}

const statusConfig = {
  todo: { label: "待办", variant: "secondary" as const, icon: Clock },
  in_progress: { label: "进行中", variant: "default" as const, icon: Clock },
  completed: { label: "已完成", variant: "outline" as const, icon: CheckSquare },
  cancelled: { label: "已取消", variant: "destructive" as const, icon: Clock }
}

export function TaskList({ projectId, className }: TaskListProps) {
  const { 
    tasks, 
    loading, 
    searchTerm, 
    statusFilter,
    setSearchTerm, 
    setStatusFilter,
    fetchTasks, 
    deleteTask 
  } = useTaskStore()
  
  const { projects, fetchProjects } = useProjectStore()
  
  const [selectedTask, setSelectedTask] = useState<Task | null>(null)
  const [showDialog, setShowDialog] = useState(false)

  useEffect(() => {
    fetchTasks(projectId)
    if (!projectId) {
      fetchProjects()
    }
  }, [projectId, fetchTasks, fetchProjects])

  const handleEdit = (task: Task) => {
    setSelectedTask(task)
    setShowDialog(true)
  }

  const handleDelete = async (taskId: string) => {
    await deleteTask(taskId)
  }

  const handleDialogClose = () => {
    setShowDialog(false)
    setSelectedTask(null)
  }

  const filteredTasks = tasks.filter(task => {
    const matchesSearch = task.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         task.description?.toLowerCase().includes(searchTerm.toLowerCase())
    const matchesStatus = !statusFilter || task.status === statusFilter
    return matchesSearch && matchesStatus
  })

  const getProjectName = (projectId: string) => {
    const project = projects.find(p => p.id === projectId)
    return project?.name || "未知项目"
  }



  if (loading) {
    return (
      <Card className={cn("animate-pulse", className)}>
        <CardHeader>
          <div className="h-6 bg-muted rounded w-1/4"></div>
          <div className="h-4 bg-muted rounded w-1/2"></div>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="flex gap-4">
              <div className="h-10 bg-muted rounded flex-1"></div>
              <div className="h-10 bg-muted rounded w-32"></div>
            </div>
            <div className="space-y-3">
              {[...Array(5)].map((_, i) => (
                <div key={i} className="h-16 bg-muted rounded"></div>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className={className}>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="flex items-center gap-2">
              <CheckSquare className="h-5 w-5" />
              任务管理
              {projectId && (
                <Badge variant="outline">
                  {getProjectName(projectId)}
                </Badge>
              )}
            </CardTitle>
            <CardDescription>
              {projectId ? "管理项目任务" : "管理所有任务"}
            </CardDescription>
          </div>
          <Dialog open={showDialog} onOpenChange={setShowDialog}>
            <DialogTrigger asChild>
              <Button onClick={() => setSelectedTask(null)}>
                <Plus className="h-4 w-4 mr-2" />
                添加任务
              </Button>
            </DialogTrigger>
            <TaskDialog 
              task={selectedTask} 
              projectId={projectId}
              onClose={handleDialogClose}
            />
          </Dialog>
        </div>
      </CardHeader>
      
      <CardContent className="space-y-4">
        {/* 搜索和筛选 */}
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="搜索任务标题或描述..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
          <Select value={statusFilter || ""} onValueChange={(value) => setStatusFilter(value as TaskStatus || null)}>
            <SelectTrigger className="w-full sm:w-32">
              <SelectValue placeholder="状态" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="">全部状态</SelectItem>
              <SelectItem value="todo">待办</SelectItem>
              <SelectItem value="in_progress">进行中</SelectItem>
              <SelectItem value="completed">已完成</SelectItem>
              <SelectItem value="cancelled">已取消</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {/* 任务统计 */}
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {Object.entries(statusConfig).map(([status, config]) => {
            const count = tasks.filter(task => task.status === status).length
            const Icon = config.icon
            return (
              <div key={status} className="text-center p-3 border rounded-lg">
                <Icon className="h-5 w-5 mx-auto mb-1 text-muted-foreground" />
                <div className="text-lg font-semibold">{count}</div>
                <div className="text-xs text-muted-foreground">{config.label}</div>
              </div>
            )
          })}
        </div>

        {/* 任务列表 */}
        {filteredTasks.length === 0 ? (
          <div className="text-center py-8">
            <CheckSquare className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <h3 className="text-lg font-medium mb-2">暂无任务</h3>
            <p className="text-muted-foreground mb-4">
              {searchTerm || statusFilter ? '没有找到匹配的任务' : '开始添加您的第一个任务'}
            </p>
            {!searchTerm && !statusFilter && (
              <Button onClick={() => setShowDialog(true)}>
                <Plus className="h-4 w-4 mr-2" />
                添加任务
              </Button>
            )}
          </div>
        ) : (
          <div className="border rounded-lg overflow-hidden">
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="min-w-[200px]">任务信息</TableHead>
                    {!projectId && <TableHead className="min-w-[120px]">项目</TableHead>}
                    <TableHead className="min-w-[100px]">负责人</TableHead>
                    <TableHead className="min-w-[120px]">截止日期</TableHead>
                    <TableHead className="min-w-[100px]">状态</TableHead>
                    <TableHead className="w-[50px]"></TableHead>
                  </TableRow>
                </TableHeader>
              <TableBody>
                {filteredTasks.map((task) => {
                  const statusInfo = statusConfig[task.status]
                  const StatusIcon = statusInfo.icon
                  const isOverdue = task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'completed'
                  
                  return (
                    <TableRow key={task.id}>
                      <TableCell>
                        <div className="space-y-1">
                          <div className="font-medium flex items-center gap-2">
                            {task.title}
                            {isOverdue && (
                              <Badge variant="destructive" className="text-xs">
                                逾期
                              </Badge>
                            )}
                          </div>
                          {task.description && (
                            <div className="text-sm text-muted-foreground line-clamp-2">
                              {task.description}
                            </div>
                          )}
                          {task.estimatedHours && (
                            <div className="text-xs text-muted-foreground">
                              预估: {task.estimatedHours}h
                            </div>
                          )}
                        </div>
                      </TableCell>
                      
                      {!projectId && (
                        <TableCell>
                          <Badge variant="outline">
                            {getProjectName(task.projectId)}
                          </Badge>
                        </TableCell>
                      )}
                      
                      <TableCell>
                        {task.assigneeId ? (
                          <div className="flex items-center gap-2">
                            <div className="w-6 h-6 rounded-full bg-primary/10 flex items-center justify-center">
                              <User className="h-3 w-3" />
                            </div>
                            <span className="text-sm">成员</span>
                          </div>
                        ) : (
                          <span className="text-sm text-muted-foreground">未分配</span>
                        )}
                      </TableCell>
                      
                      <TableCell>
                        {task.dueDate ? (
                          <div className="flex items-center gap-1">
                            <Calendar className="h-3 w-3 text-muted-foreground" />
                            <span className={cn(
                              "text-sm",
                              isOverdue && "text-destructive"
                            )}>
                              {new Date(task.dueDate).toLocaleDateString()}
                            </span>
                          </div>
                        ) : (
                          <span className="text-sm text-muted-foreground">无截止日期</span>
                        )}
                      </TableCell>
                      
                      <TableCell>
                        <Badge variant={statusInfo.variant} className="flex items-center gap-1 w-fit">
                          <StatusIcon className="h-3 w-3" />
                          {statusInfo.label}
                        </Badge>
                      </TableCell>
                      
                      <TableCell>
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="sm">
                              <MoreHorizontal className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem onClick={() => handleEdit(task)}>
                              <Edit className="h-4 w-4 mr-2" />
                              编辑
                            </DropdownMenuItem>
                            <AlertDialog>
                              <AlertDialogTrigger asChild>
                                <DropdownMenuItem 
                                  onSelect={(e) => e.preventDefault()}
                                  className="text-destructive"
                                >
                                  <Trash2 className="h-4 w-4 mr-2" />
                                  删除
                                </DropdownMenuItem>
                              </AlertDialogTrigger>
                              <AlertDialogContent>
                                <AlertDialogHeader>
                                  <AlertDialogTitle>确认删除</AlertDialogTitle>
                                  <AlertDialogDescription>
                                    确定要删除任务 &quot;{task.title}&quot; 吗？此操作无法撤销。
                                  </AlertDialogDescription>
                                </AlertDialogHeader>
                                <AlertDialogFooter>
                                  <AlertDialogCancel>取消</AlertDialogCancel>
                                  <AlertDialogAction 
                                    onClick={() => handleDelete(task.id)}
                                    className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                                  >
                                    删除
                                  </AlertDialogAction>
                                </AlertDialogFooter>
                              </AlertDialogContent>
                            </AlertDialog>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
              </Table>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}