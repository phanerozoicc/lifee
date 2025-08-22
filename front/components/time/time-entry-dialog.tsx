"use client"

import { useState, useEffect } from "react"
import { CalendarIcon, Clock } from "lucide-react"
import { Button } from "@/components/ui/button"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import {
  Popover,
  PopoverContent,
  PopoverTrigger,
} from "@/components/ui/popover"
import { Calendar } from "@/components/ui/calendar"
import { Switch } from "@/components/ui/switch"
import { useTimeEntryStore } from "@/lib/time/store"
import { useProjectStore } from "@/lib/time/store"
import { useTaskStore } from "@/lib/time/store"
import { useTagStore } from "@/lib/time/store"
import { cn } from "@/lib/utils"
import type { TimeEntry, Task } from "@/lib/time/types"

interface TimeEntryDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  entry?: TimeEntry | null
  onSuccess?: () => void
}

export function TimeEntryDialog({ open, onOpenChange, entry, onSuccess }: TimeEntryDialogProps) {
  const { createEntry, updateEntry } = useTimeEntryStore()
  const { projects } = useProjectStore()
  const { tasks, fetchTasks } = useTaskStore()
  const { tags } = useTagStore()
  
  const [loading, setLoading] = useState(false)
  const [description, setDescription] = useState("")
  const [projectId, setProjectId] = useState("")
  const [taskId, setTaskId] = useState("")
  const [selectedTags, setSelectedTags] = useState<string[]>([])
  const [startDate, setStartDate] = useState<Date>(new Date())
  const [startTime, setStartTime] = useState("09:00")
  const [endTime, setEndTime] = useState("17:00")
  const [duration, setDuration] = useState("")
  const [useEndTime, setUseEndTime] = useState(true)
  const [isBillable, setIsBillable] = useState(true)
  const [hourlyRate, setHourlyRate] = useState("")
  const [availableTasks, setAvailableTasks] = useState<Task[]>([])

  // 重置表单
  const resetForm = () => {
    setDescription("")
    setProjectId("")
    setTaskId("")
    setSelectedTags([])
    setStartDate(new Date())
    setStartTime("09:00")
    setEndTime("17:00")
    setDuration("")
    setUseEndTime(true)
    setIsBillable(true)
    setHourlyRate("")
  }

  // 计算持续时间（分钟）
  const calculateDuration = () => {
    if (!useEndTime || !startTime || !endTime) return 0
    
    const [startHour, startMin] = startTime.split(':').map(Number)
    const [endHour, endMin] = endTime.split(':').map(Number)
    
    const startMinutes = startHour * 60 + startMin
    const endMinutes = endHour * 60 + endMin
    
    return endMinutes > startMinutes ? endMinutes - startMinutes : 0
  }

  // 格式化持续时间显示
  const formatDurationDisplay = (minutes: number) => {
    const hours = Math.floor(minutes / 60)
    const mins = minutes % 60
    return hours > 0 ? `${hours}h ${mins}m` : `${mins}m`
  }

  // 根据选择的项目筛选任务
  useEffect(() => {
    if (projectId) {
      const projectTasks = tasks.filter(task => task.projectId === projectId)
      setAvailableTasks(projectTasks)
    } else {
      setAvailableTasks([])
      setTaskId("")
    }
  }, [projectId, tasks])

  // 初始化表单数据
  useEffect(() => {
    if (entry) {
      setDescription(entry.description || "")
      setProjectId(entry.projectId)
      setTaskId(entry.taskId || "")
      setSelectedTags(entry.tags || [])
      
      const startDateTime = new Date(entry.startTime)
      setStartDate(startDateTime)
      setStartTime(startDateTime.toTimeString().slice(0, 5))
      
      if (entry.endTime) {
        const endDateTime = new Date(entry.endTime)
        setEndTime(endDateTime.toTimeString().slice(0, 5))
        setUseEndTime(true)
      } else {
        setDuration(Math.floor(entry.duration / 60).toString())
        setUseEndTime(false)
      }
      
      setIsBillable(entry.billable || false)
      setHourlyRate(entry.hourlyRate?.toString() || "")
    } else {
      resetForm()
    }
  }, [entry, open])

  // 获取任务数据
  useEffect(() => {
    if (open) {
      fetchTasks()
    }
  }, [open, fetchTasks])

  // 处理标签选择
  const handleTagToggle = (tagId: string) => {
    setSelectedTags(prev => 
      prev.includes(tagId) 
        ? prev.filter(id => id !== tagId)
        : [...prev, tagId]
    )
  }

  // 提交表单
  const handleSubmit = async () => {
    if (!description.trim() || !projectId || !startDate) {
      alert("请填写必要信息")
      return
    }

    setLoading(true)
    try {
      // 构建开始时间
      const [startHour, startMin] = startTime.split(':').map(Number)
      const startDateTime = new Date(startDate)
      startDateTime.setHours(startHour, startMin, 0, 0)

      // 构建结束时间或持续时间
      let endDateTime: Date | undefined
      let durationSeconds: number

      if (useEndTime && endTime) {
        const [endHour, endMin] = endTime.split(':').map(Number)
        endDateTime = new Date(startDate)
        endDateTime.setHours(endHour, endMin, 0, 0)
        
        // 如果结束时间早于开始时间，假设是第二天
        if (endDateTime <= startDateTime) {
          endDateTime.setDate(endDateTime.getDate() + 1)
        }
        
        durationSeconds = Math.floor((endDateTime.getTime() - startDateTime.getTime()) / 1000)
      } else {
        durationSeconds = parseInt(duration) * 60 // 转换为秒
      }

      const entryData = {
        description: description.trim(),
        projectId,
        taskId: taskId || undefined,
        tags: selectedTags,
        startTime: startDateTime.toISOString(),
        endTime: endDateTime?.toISOString(),
        duration: durationSeconds,
        billable: isBillable,
        hourlyRate: hourlyRate ? parseFloat(hourlyRate) : undefined
      }

      if (entry) {
        await updateEntry(entry.id, entryData)
      } else {
        await createEntry(entryData)
      }

      onSuccess?.()
      onOpenChange(false)
    } catch (error) {
      console.error("Failed to save time entry:", error)
      alert("保存失败，请重试")
    } finally {
      setLoading(false)
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <Clock className="h-5 w-5" />
            {entry ? "编辑时间条目" : "添加时间条目"}
          </DialogTitle>
          <DialogDescription>
            {entry ? "修改时间记录的详细信息" : "手动添加一条新的时间记录"}
          </DialogDescription>
        </DialogHeader>

        <div className="grid gap-4 py-4">
          {/* 任务描述 */}
          <div className="grid gap-2">
            <Label htmlFor="description">任务描述 *</Label>
            <Textarea
              id="description"
              placeholder="描述您做了什么..."
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              rows={3}
            />
          </div>

          {/* 项目和任务 */}
          <div className="grid grid-cols-2 gap-4">
            <div className="grid gap-2">
              <Label htmlFor="project">项目 *</Label>
              <Select value={projectId} onValueChange={setProjectId}>
                <SelectTrigger>
                  <SelectValue placeholder="选择项目" />
                </SelectTrigger>
                <SelectContent>
                  {projects.map((project) => (
                    <SelectItem key={project.id} value={project.id}>
                      <div className="flex items-center gap-2">
                        <div 
                          className="w-3 h-3 rounded-full" 
                          style={{ backgroundColor: project.color }}
                        />
                        {project.name}
                      </div>
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="grid gap-2">
              <Label htmlFor="task">任务</Label>
              <Select value={taskId} onValueChange={setTaskId} disabled={!projectId}>
                <SelectTrigger>
                  <SelectValue placeholder="选择任务（可选）" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="">无特定任务</SelectItem>
                  {availableTasks.map((task) => (
                    <SelectItem key={task.id} value={task.id}>
                      {task.name}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* 日期 */}
          <div className="grid gap-2">
            <Label>日期 *</Label>
            <Popover>
              <PopoverTrigger asChild>
                <Button
                  variant="outline"
                  className={cn(
                    "w-full justify-start text-left font-normal",
                    !startDate && "text-muted-foreground"
                  )}
                >
                  <CalendarIcon className="mr-2 h-4 w-4" />
                  {startDate ? startDate.toLocaleDateString() : "选择日期"}
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-auto p-0">
                <Calendar
                  mode="single"
                  selected={startDate}
                  onSelect={(date) => date && setStartDate(date)}
                  initialFocus
                />
              </PopoverContent>
            </Popover>
          </div>

          {/* 时间设置 */}
          <div className="grid gap-4">
            <div className="flex items-center space-x-2">
              <Switch
                id="use-end-time"
                checked={useEndTime}
                onCheckedChange={setUseEndTime}
              />
              <Label htmlFor="use-end-time">
                使用结束时间（否则使用持续时间）
              </Label>
            </div>

            {useEndTime ? (
              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-2">
                  <Label htmlFor="start-time">开始时间 *</Label>
                  <Input
                    id="start-time"
                    type="time"
                    value={startTime}
                    onChange={(e) => setStartTime(e.target.value)}
                  />
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="end-time">结束时间 *</Label>
                  <Input
                    id="end-time"
                    type="time"
                    value={endTime}
                    onChange={(e) => setEndTime(e.target.value)}
                  />
                </div>
              </div>
            ) : (
              <div className="grid grid-cols-2 gap-4">
                <div className="grid gap-2">
                  <Label htmlFor="start-time">开始时间 *</Label>
                  <Input
                    id="start-time"
                    type="time"
                    value={startTime}
                    onChange={(e) => setStartTime(e.target.value)}
                  />
                </div>
                <div className="grid gap-2">
                  <Label htmlFor="duration">持续时间（分钟） *</Label>
                  <Input
                    id="duration"
                    type="number"
                    placeholder="60"
                    value={duration}
                    onChange={(e) => setDuration(e.target.value)}
                    min="1"
                  />
                </div>
              </div>
            )}

            {/* 显示计算的持续时间 */}
            {useEndTime && startTime && endTime && (
              <div className="text-sm text-muted-foreground">
                持续时间: {formatDurationDisplay(calculateDuration())}
              </div>
            )}
          </div>

          {/* 计费设置 */}
          <div className="grid gap-4">
            <div className="flex items-center space-x-2">
              <Switch
                id="billable"
                checked={isBillable}
                onCheckedChange={setIsBillable}
              />
              <Label htmlFor="billable">可计费时间</Label>
            </div>

            {isBillable && (
              <div className="grid gap-2">
                <Label htmlFor="hourly-rate">时薪（¥/小时）</Label>
                <Input
                  id="hourly-rate"
                  type="number"
                  placeholder="100"
                  value={hourlyRate}
                  onChange={(e) => setHourlyRate(e.target.value)}
                  min="0"
                  step="0.01"
                />
              </div>
            )}
          </div>

          {/* 标签 */}
          {tags.length > 0 && (
            <div className="grid gap-2">
              <Label>标签</Label>
              <div className="flex flex-wrap gap-2">
                {tags.map((tag) => (
                  <Button
                    key={tag.id}
                    variant={selectedTags.includes(tag.id) ? "default" : "outline"}
                    size="sm"
                    onClick={() => handleTagToggle(tag.id)}
                  >
                    {tag.name}
                  </Button>
                ))}
              </div>
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>
            取消
          </Button>
          <Button onClick={handleSubmit} disabled={loading}>
            {loading ? "保存中..." : entry ? "更新" : "添加"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}