"use client"

import { useState, useEffect, useCallback } from "react"
import { Play, Pause, Square, Clock, AlertCircle, Keyboard } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { Badge } from "@/components/ui/badge"
import { Alert, AlertDescription } from "@/components/ui/alert"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import { useTimerStore } from "@/lib/time/store"
import { useProjectStore } from "@/lib/time/store"
import { useTaskStore } from "@/lib/time/store"
import { cn } from "@/lib/utils"
import { toast } from "sonner"
import type { Task } from "@/lib/time/types"

interface TimerProps {
  className?: string
}

export function Timer({ className }: TimerProps) {
  const {
    isRunning,
    currentEntry,
    elapsedTime,
    startTimer,
    pauseTimer,
    stopTimer,
    updateCurrentEntry,
    error
  } = useTimerStore()
  
  const { projects, fetchProjects, loading: projectsLoading } = useProjectStore()
  const { tasks, fetchTasks, loading: tasksLoading } = useTaskStore()
  
  const [description, setDescription] = useState(currentEntry?.description || "")
  const [selectedProject, setSelectedProject] = useState<string>(currentEntry?.projectId || "")
  const [selectedTask, setSelectedTask] = useState<string>(currentEntry?.taskId || "")
  const [availableTasks, setAvailableTasks] = useState<Task[]>([])
  const [isLoading, setIsLoading] = useState(false)

  // 格式化时间显示
  const formatTime = (seconds: number) => {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const secs = seconds % 60
    return `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`
  }

  // 获取项目和任务数据
  useEffect(() => {
    fetchProjects()
    fetchTasks()
  }, [fetchProjects, fetchTasks])

  // 根据选择的项目筛选任务
  useEffect(() => {
    if (selectedProject) {
      const projectTasks = tasks.filter(task => task.projectId === selectedProject)
      setAvailableTasks(projectTasks)
    } else {
      setAvailableTasks([])
      setSelectedTask("")
    }
  }, [selectedProject, tasks])

  // 同步当前条目状态
  useEffect(() => {
    if (currentEntry) {
      setDescription(currentEntry.description || "")
      setSelectedProject(currentEntry.projectId || "")
      setSelectedTask(currentEntry.taskId || "")
    }
  }, [currentEntry])

  // 键盘快捷键处理
  const handleKeyDown = useCallback((event: KeyboardEvent) => {
    // Ctrl/Cmd + Space: 开始/暂停计时
    if ((event.ctrlKey || event.metaKey) && event.code === 'Space') {
      event.preventDefault()
      if (isRunning) {
        handlePause()
      } else {
        handleStart()
      }
    }
    // Ctrl/Cmd + Shift + Space: 停止计时
    if ((event.ctrlKey || event.metaKey) && event.shiftKey && event.code === 'Space') {
      event.preventDefault()
      if (isRunning) {
        handleStop()
      }
    }
  }, [isRunning])

  useEffect(() => {
    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [handleKeyDown])

  // 开始计时
  const handleStart = async () => {
    if (!selectedProject) {
      toast.error("请先选择一个项目")
      return
    }

    setIsLoading(true)
    try {
      const entryData = {
        description: description.trim() || "未命名任务",
        projectId: selectedProject,
        taskId: selectedTask || undefined
      }

      await startTimer(entryData)
      toast.success("计时器已开始")
    } catch (error) {
      toast.error("启动计时器失败")
    } finally {
      setIsLoading(false)
    }
  }

  // 暂停计时
  const handlePause = async () => {
    setIsLoading(true)
    try {
      await pauseTimer()
      toast.success("计时器已暂停")
    } catch (error) {
      toast.error("暂停计时器失败")
    } finally {
      setIsLoading(false)
    }
  }

  // 停止计时
  const handleStop = async () => {
    setIsLoading(true)
    try {
      await stopTimer()
      toast.success("计时器已停止")
      // 重置表单
      setDescription("")
      setSelectedProject("")
      setSelectedTask("")
    } catch (error) {
      toast.error("停止计时器失败")
    } finally {
      setIsLoading(false)
    }
  }

  // 更新当前条目
  const handleUpdateEntry = () => {
    if (currentEntry) {
      updateCurrentEntry({
        description: description.trim() || "未命名任务",
        projectId: selectedProject,
        taskId: selectedTask || undefined
      })
    }
  }

  return (
    <TooltipProvider>
      <Card className={cn("w-full", className)}>
        <CardHeader>
          <CardTitle className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Clock className="h-5 w-5" />
              时间追踪器
            </div>
            <Tooltip>
              <TooltipTrigger asChild>
                <Button variant="ghost" size="sm">
                  <Keyboard className="h-4 w-4" />
                </Button>
              </TooltipTrigger>
              <TooltipContent>
                <div className="text-sm space-y-1">
                  <p><kbd>Ctrl/Cmd + Space</kbd>: 开始/暂停</p>
                  <p><kbd>Ctrl/Cmd + Shift + Space</kbd>: 停止</p>
                </div>
              </TooltipContent>
            </Tooltip>
          </CardTitle>
          <CardDescription>
            开始追踪您的工作时间
          </CardDescription>
        </CardHeader>
      <CardContent className="space-y-4">
          {/* 错误提示 */}
          {error && (
            <Alert variant="destructive">
              <AlertCircle className="h-4 w-4" />
              <AlertDescription>
                {error}
              </AlertDescription>
            </Alert>
          )}

          {/* 时间显示 */}
          <div className="text-center">
            <div className="text-4xl font-mono font-bold text-primary mb-2">
              {formatTime(elapsedTime)}
            </div>
            <div className="flex items-center justify-center gap-2">
              {isRunning && (
                <Badge variant="default" className="animate-pulse">
                  正在运行
                </Badge>
              )}
              {isLoading && (
                <Badge variant="secondary">
                  处理中...
                </Badge>
              )}
            </div>
          </div>

        {/* 任务描述 */}
        <div className="space-y-2">
          <Label htmlFor="description">任务描述</Label>
          <Input
            id="description"
            placeholder="您在做什么？"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            onBlur={handleUpdateEntry}
            disabled={isRunning}
          />
        </div>

        {/* 项目选择 */}
        <div className="space-y-2">
          <Label htmlFor="project">项目</Label>
          <Select 
            value={selectedProject} 
            onValueChange={(value) => {
              setSelectedProject(value)
              if (currentEntry) {
                updateCurrentEntry({ projectId: value })
              }
            }}
            disabled={isRunning}
          >
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

        {/* 任务选择 */}
        {selectedProject && availableTasks.length > 0 && (
          <div className="space-y-2">
            <Label htmlFor="task">任务</Label>
            <Select 
              value={selectedTask} 
              onValueChange={(value) => {
                setSelectedTask(value)
                if (currentEntry) {
                  updateCurrentEntry({ taskId: value })
                }
              }}
              disabled={isRunning}
            >
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
        )}

        {/* 控制按钮 */}
        <div className="flex items-center gap-2">
          {!isRunning ? (
            <Button 
              onClick={handleStart} 
              className="flex-1"
              disabled={!selectedProject || isLoading || projectsLoading}
            >
              <Play className="h-4 w-4 mr-2" />
              {isLoading ? "启动中..." : "开始"}
            </Button>
          ) : (
            <>
              <Button 
                onClick={handlePause} 
                variant="outline" 
                className="flex-1"
                disabled={isLoading}
              >
                <Pause className="h-4 w-4 mr-2" />
                {isLoading ? "处理中..." : "暂停"}
              </Button>
              <Button 
                onClick={handleStop} 
                variant="destructive" 
                className="flex-1"
                disabled={isLoading}
              >
                <Square className="h-4 w-4 mr-2" />
                {isLoading ? "停止中..." : "停止"}
              </Button>
            </>
          )}
        </div>

        {/* 快捷键提示 */}
        <div className="text-xs text-muted-foreground text-center">
          使用 <kbd className="px-1 py-0.5 bg-muted rounded text-xs">Ctrl/Cmd + Space</kbd> 快速开始/暂停
        </div>

        {/* 当前条目信息 */}
        {currentEntry && (
          <div className="pt-4 border-t">
            <div className="text-sm text-muted-foreground space-y-1">
              <p><strong>开始时间:</strong> {new Date(currentEntry.startTime).toLocaleString()}</p>
              {currentEntry.projectId && (
                <p><strong>项目:</strong> {projects.find(p => p.id === currentEntry.projectId)?.name}</p>
              )}
              {currentEntry.taskId && (
                <p><strong>任务:</strong> {tasks.find(t => t.id === currentEntry.taskId)?.name}</p>
              )}
            </div>
          </div>
        )}
      </CardContent>
    </Card>
    </TooltipProvider>
  )
}