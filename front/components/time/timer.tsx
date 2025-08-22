"use client"

import { useState, useEffect } from "react"
import { Play, Pause, Square, Clock } from "lucide-react"
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
import { useTimerStore } from "@/lib/time/store"
import { useProjectStore } from "@/lib/time/store"
import { useTaskStore } from "@/lib/time/store"
import { cn } from "@/lib/utils"
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
    updateCurrentEntry
  } = useTimerStore()
  
  const { projects, fetchProjects } = useProjectStore()
  const { tasks, fetchTasks } = useTaskStore()
  
  const [description, setDescription] = useState(currentEntry?.description || "")
  const [selectedProject, setSelectedProject] = useState<string>(currentEntry?.projectId || "")
  const [selectedTask, setSelectedTask] = useState<string>(currentEntry?.taskId || "")
  const [availableTasks, setAvailableTasks] = useState<Task[]>([])

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

  // 开始计时
  const handleStart = async () => {
    if (!selectedProject) {
      alert("请选择一个项目")
      return
    }

    const entryData = {
      description: description.trim() || "未命名任务",
      projectId: selectedProject,
      taskId: selectedTask || undefined
    }

    await startTimer(entryData)
  }

  // 暂停计时
  const handlePause = async () => {
    await pauseTimer()
  }

  // 停止计时
  const handleStop = async () => {
    await stopTimer()
    // 重置表单
    setDescription("")
    setSelectedProject("")
    setSelectedTask("")
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
    <Card className={cn("w-full", className)}>
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <Clock className="h-5 w-5" />
          时间追踪器
        </CardTitle>
        <CardDescription>
          开始追踪您的工作时间
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        {/* 时间显示 */}
        <div className="text-center">
          <div className="text-4xl font-mono font-bold text-primary mb-2">
            {formatTime(elapsedTime)}
          </div>
          {isRunning && (
            <Badge variant="default" className="animate-pulse">
              正在运行
            </Badge>
          )}
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
              disabled={!selectedProject}
            >
              <Play className="h-4 w-4 mr-2" />
              开始
            </Button>
          ) : (
            <>
              <Button 
                onClick={handlePause} 
                variant="outline" 
                className="flex-1"
              >
                <Pause className="h-4 w-4 mr-2" />
                暂停
              </Button>
              <Button 
                onClick={handleStop} 
                variant="destructive" 
                className="flex-1"
              >
                <Square className="h-4 w-4 mr-2" />
                停止
              </Button>
            </>
          )}
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
  )
}