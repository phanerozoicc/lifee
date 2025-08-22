"use client"

import { useState, useEffect } from "react"
import { DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Calendar } from "@/components/ui/calendar"
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover"
import { useTaskStore, useProjectStore, useTeamStore } from "@/lib/time/store"
import { Task, TaskStatus } from "@/lib/time/types"
import { CheckSquare, Calendar as CalendarIcon, Clock, User, Folder } from "lucide-react"
import { format } from "date-fns"
import { cn } from "@/lib/utils"

interface TaskDialogProps {
  task?: Task | null
  projectId?: string
  onClose: () => void
}

const statusOptions = [
  { value: "todo", label: "待办" },
  { value: "in_progress", label: "进行中" },
  { value: "completed", label: "已完成" },
  { value: "cancelled", label: "已取消" }
]

export function TaskDialog({ task, projectId, onClose }: TaskDialogProps) {
  const { createTask, updateTask, loading } = useTaskStore()
  const { projects, fetchProjects } = useProjectStore()
  const { teams, fetchTeams } = useTeamStore()
  
  const [formData, setFormData] = useState({
    title: "",
    description: "",
    projectId: projectId || "",
    assigneeId: "",
    status: "todo" as TaskStatus,
    estimatedHours: "",
    dueDate: undefined as Date | undefined
  })
  
  const [errors, setErrors] = useState<Record<string, string>>({})
  const [showCalendar, setShowCalendar] = useState(false)

  useEffect(() => {
    fetchProjects()
    fetchTeams()
  }, [fetchProjects, fetchTeams])

  useEffect(() => {
    if (task) {
      setFormData({
        title: task.title || "",
        description: task.description || "",
        projectId: task.projectId || "",
        assigneeId: task.assigneeId || "",
        status: task.status || "todo",
        estimatedHours: task.estimatedHours?.toString() || "",
        dueDate: task.dueDate ? new Date(task.dueDate) : undefined
      })
    } else {
      setFormData({
        title: "",
        description: "",
        projectId: projectId || "",
        assigneeId: "",
        status: "todo",
        estimatedHours: "",
        dueDate: undefined
      })
    }
    setErrors({})
  }, [task, projectId])

  const validateForm = () => {
    const newErrors: Record<string, string> = {}
    
    if (!formData.title.trim()) {
      newErrors.title = "任务标题不能为空"
    }
    
    if (!formData.projectId) {
      newErrors.projectId = "请选择项目"
    }
    
    if (formData.estimatedHours && (isNaN(Number(formData.estimatedHours)) || Number(formData.estimatedHours) <= 0)) {
      newErrors.estimatedHours = "请输入有效的预估工时"
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm()) {
      return
    }

    try {
      const taskData = {
        title: formData.title.trim(),
        description: formData.description.trim() || undefined,
        projectId: formData.projectId,
        assigneeId: formData.assigneeId || undefined,
        status: formData.status,
        estimatedHours: formData.estimatedHours ? Number(formData.estimatedHours) : undefined,
        dueDate: formData.dueDate?.toISOString()
      }

      if (task) {
        await updateTask(task.id, taskData)
      } else {
        await createTask(taskData)
      }
      
      onClose()
    } catch (error) {
      console.error('Failed to save task:', error)
    }
  }

  const handleInputChange = (field: string, value: string | Date | undefined) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: "" }))
    }
  }

  // 获取所有团队成员
  const allMembers = teams.flatMap(team => team.members || [])

  return (
    <DialogContent className="sm:max-w-[600px]">
      <DialogHeader>
        <DialogTitle className="flex items-center gap-2">
          <CheckSquare className="h-5 w-5" />
          {task ? "编辑任务" : "添加任务"}
        </DialogTitle>
        <DialogDescription>
          {task ? "修改任务信息" : "创建新的任务"}
        </DialogDescription>
      </DialogHeader>
      
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* 基本信息 */}
        <div className="grid grid-cols-2 gap-4">
          <div className="col-span-2 space-y-2">
            <Label htmlFor="title" className="flex items-center gap-2">
              <CheckSquare className="h-4 w-4" />
              任务标题 *
            </Label>
            <Input
              id="title"
              value={formData.title}
              onChange={(e) => handleInputChange("title", e.target.value)}
              placeholder="输入任务标题"
              className={errors.title ? "border-destructive" : ""}
            />
            {errors.title && (
              <p className="text-sm text-destructive">{errors.title}</p>
            )}
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="projectId" className="flex items-center gap-2">
              <Folder className="h-4 w-4" />
              所属项目 *
            </Label>
            <Select 
              value={formData.projectId} 
              onValueChange={(value) => handleInputChange("projectId", value)}
              disabled={!!projectId}
            >
              <SelectTrigger className={errors.projectId ? "border-destructive" : ""}>
                <SelectValue placeholder="选择项目" />
              </SelectTrigger>
              <SelectContent>
                {projects.map((project) => (
                  <SelectItem key={project.id} value={project.id}>
                    {project.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.projectId && (
              <p className="text-sm text-destructive">{errors.projectId}</p>
            )}
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="status" className="flex items-center gap-2">
              <Clock className="h-4 w-4" />
              任务状态
            </Label>
            <Select 
              value={formData.status} 
              onValueChange={(value) => handleInputChange("status", value as TaskStatus)}
            >
              <SelectTrigger>
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {statusOptions.map((option) => (
                  <SelectItem key={option.value} value={option.value}>
                    {option.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
        </div>
        
        {/* 分配和时间 */}
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="assigneeId" className="flex items-center gap-2">
              <User className="h-4 w-4" />
              负责人
            </Label>
            <Select 
              value={formData.assigneeId} 
              onValueChange={(value) => handleInputChange("assigneeId", value)}
            >
              <SelectTrigger>
                <SelectValue placeholder="选择负责人" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">未分配</SelectItem>
                {allMembers.map((member) => (
                  <SelectItem key={member.userId} value={member.userId}>
                    {member.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="estimatedHours" className="flex items-center gap-2">
              <Clock className="h-4 w-4" />
              预估工时 (小时)
            </Label>
            <Input
              id="estimatedHours"
              type="number"
              min="0"
              step="0.5"
              value={formData.estimatedHours}
              onChange={(e) => handleInputChange("estimatedHours", e.target.value)}
              placeholder="0"
              className={errors.estimatedHours ? "border-destructive" : ""}
            />
            {errors.estimatedHours && (
              <p className="text-sm text-destructive">{errors.estimatedHours}</p>
            )}
          </div>
        </div>
        
        {/* 截止日期 */}
        <div className="space-y-2">
          <Label className="flex items-center gap-2">
            <CalendarIcon className="h-4 w-4" />
            截止日期
          </Label>
          <Popover open={showCalendar} onOpenChange={setShowCalendar}>
            <PopoverTrigger asChild>
              <Button
                variant="outline"
                className={cn(
                  "w-full justify-start text-left font-normal",
                  !formData.dueDate && "text-muted-foreground"
                )}
              >
                <CalendarIcon className="mr-2 h-4 w-4" />
                {formData.dueDate ? format(formData.dueDate, "yyyy-MM-dd") : "选择截止日期"}
              </Button>
            </PopoverTrigger>
            <PopoverContent className="w-auto p-0" align="start">
              <Calendar
                mode="single"
                selected={formData.dueDate}
                onSelect={(date) => {
                  handleInputChange("dueDate", date)
                  setShowCalendar(false)
                }}
                disabled={(date) => date < new Date(new Date().setHours(0, 0, 0, 0))}
                initialFocus
              />
              {formData.dueDate && (
                <div className="p-3 border-t">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => {
                      handleInputChange("dueDate", undefined)
                      setShowCalendar(false)
                    }}
                    className="w-full"
                  >
                    清除日期
                  </Button>
                </div>
              )}
            </PopoverContent>
          </Popover>
        </div>
        
        {/* 任务描述 */}
        <div className="space-y-2">
          <Label htmlFor="description">任务描述</Label>
          <Textarea
            id="description"
            value={formData.description}
            onChange={(e) => handleInputChange("description", e.target.value)}
            placeholder="输入任务描述"
            rows={4}
          />
        </div>
        
        <DialogFooter>
          <Button type="button" variant="outline" onClick={onClose}>
            取消
          </Button>
          <Button type="submit" disabled={loading}>
            {loading ? "保存中..." : (task ? "更新" : "创建")}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  )
}