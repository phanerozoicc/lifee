"use client"

import { useState, useEffect } from "react"
import { Edit, Trash2, Play, Clock, Calendar, Search } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
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
import { Calendar as CalendarComponent } from "@/components/ui/calendar"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { TimeEntryDialog } from "@/components/time/time-entry-dialog"
import { useTimeEntryStore } from "@/lib/time/store"
import { useProjectStore } from "@/lib/time/store"
import { useTaskStore } from "@/lib/time/store"
import { useTimerStore } from "@/lib/time/store"
import { cn } from "@/lib/utils"
import type { TimeEntry } from "@/lib/time/types"

interface TimeEntryListProps {
  className?: string
  showFilters?: boolean
  limit?: number
}

export function TimeEntryList({ className, showFilters = true, limit }: TimeEntryListProps) {
  const {
    entries,
    loading,
    filter,
    fetchEntries,
    deleteEntry,
    updateFilter
  } = useTimeEntryStore()
  
  const { projects } = useProjectStore()
  const { tasks } = useTaskStore()
  const { startTimer } = useTimerStore()
  
  const [searchTerm, setSearchTerm] = useState("")
  const [selectedDate, setSelectedDate] = useState<Date | undefined>()
  const [showEditDialog, setShowEditDialog] = useState(false)
  const [showDeleteDialog, setShowDeleteDialog] = useState(false)
  const [selectedEntry, setSelectedEntry] = useState<TimeEntry | null>(null)

  // 格式化持续时间
  const formatDuration = (seconds: number) => {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    
    if (hours > 0) {
      return `${hours}h ${minutes}m`
    }
    return `${minutes}m`
  }

  // 获取项目名称
  const getProjectName = (projectId: string) => {
    const project = projects.find(p => p.id === projectId)
    return project?.name || "未知项目"
  }

  // 获取任务名称
  const getTaskName = (taskId?: string) => {
    if (!taskId) return null
    const task = tasks.find(t => t.id === taskId)
    return task?.name
  }

  // 获取项目颜色
  const getProjectColor = (projectId: string) => {
    const project = projects.find(p => p.id === projectId)
    return project?.color || "#6b7280"
  }

  // 处理搜索
  const handleSearch = (term: string) => {
    setSearchTerm(term)
    updateFilter({ ...filter, search: term })
  }

  // 处理日期筛选
  const handleDateFilter = (date: Date | undefined) => {
    setSelectedDate(date)
    if (date) {
      const startOfDay = new Date(date)
      startOfDay.setHours(0, 0, 0, 0)
      const endOfDay = new Date(date)
      endOfDay.setHours(23, 59, 59, 999)
      
      updateFilter({
        ...filter,
        startDate: startOfDay.toISOString(),
        endDate: endOfDay.toISOString()
      })
    } else {
      updateFilter({
        ...filter,
        startDate: undefined,
        endDate: undefined
      })
    }
  }

  // 处理项目筛选
  const handleProjectFilter = (projectId: string) => {
    updateFilter({
      ...filter,
      projectId: projectId === "all" ? undefined : projectId
    })
  }

  // 编辑条目
  const handleEdit = (entry: TimeEntry) => {
    setSelectedEntry(entry)
    setShowEditDialog(true)
  }

  // 删除条目
  const handleDelete = (entry: TimeEntry) => {
    setSelectedEntry(entry)
    setShowDeleteDialog(true)
  }

  // 确认删除
  const confirmDelete = async () => {
    if (selectedEntry) {
      await deleteEntry(selectedEntry.id)
      setShowDeleteDialog(false)
      setSelectedEntry(null)
    }
  }

  // 复制并开始新的计时
  const handleCopyAndStart = async (entry: TimeEntry) => {
    await startTimer({
      description: entry.description,
      projectId: entry.projectId,
      taskId: entry.taskId
    })
  }

  // 获取显示的条目
  const displayEntries = limit ? entries.slice(0, limit) : entries

  // 计算总时间
  const totalDuration = displayEntries.reduce((total, entry) => total + entry.duration, 0)

  useEffect(() => {
    fetchEntries()
  }, [filter, fetchEntries])

  return (
    <Card className={cn("w-full", className)}>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" />
              时间条目
            </CardTitle>
            <CardDescription>
              {displayEntries.length} 条记录，总计 {formatDuration(totalDuration)}
            </CardDescription>
          </div>
          {!limit && (
            <Button onClick={() => setShowEditDialog(true)}>
              添加条目
            </Button>
          )}
        </div>
      </CardHeader>
      
      <CardContent className="space-y-4">
        {/* 筛选器 */}
        {showFilters && (
          <div className="flex flex-wrap items-center gap-4">
            {/* 搜索 */}
            <div className="flex items-center gap-2 flex-1 min-w-64">
              <Search className="h-4 w-4 text-muted-foreground" />
              <Input
                placeholder="搜索描述..."
                value={searchTerm}
                onChange={(e) => handleSearch(e.target.value)}
                className="flex-1"
              />
            </div>

            {/* 日期筛选 */}
            <Popover>
              <PopoverTrigger asChild>
                <Button variant="outline" className="w-auto justify-start text-left font-normal">
                  <Calendar className="h-4 w-4 mr-2" />
                  {selectedDate ? selectedDate.toLocaleDateString() : "选择日期"}
                </Button>
              </PopoverTrigger>
              <PopoverContent className="w-auto p-0" align="start">
                <CalendarComponent
                  mode="single"
                  selected={selectedDate}
                  onSelect={handleDateFilter}
                  initialFocus
                />
              </PopoverContent>
            </Popover>

            {/* 项目筛选 */}
            <Select 
              value={filter.projectId || "all"} 
              onValueChange={handleProjectFilter}
            >
              <SelectTrigger className="w-48">
                <SelectValue placeholder="选择项目" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">所有项目</SelectItem>
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

            {/* 清除筛选 */}
            {(filter.search || filter.projectId || filter.startDate) && (
              <Button 
                variant="ghost" 
                size="sm"
                onClick={() => {
                  setSearchTerm("")
                  setSelectedDate(undefined)
                  updateFilter({})
                }}
              >
                清除筛选
              </Button>
            )}
          </div>
        )}

        {/* 条目列表 */}
        {loading ? (
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mx-auto"></div>
            <p className="text-muted-foreground mt-2">加载中...</p>
          </div>
        ) : displayEntries.length === 0 ? (
          <div className="text-center py-8">
            <Clock className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
            <h3 className="font-semibold mb-2">暂无时间记录</h3>
            <p className="text-muted-foreground mb-4">
              开始追踪您的时间或手动添加条目
            </p>
            <Button onClick={() => setShowEditDialog(true)}>
              添加第一条记录
            </Button>
          </div>
        ) : (
          <div className="border rounded-lg">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>描述</TableHead>
                  <TableHead>项目</TableHead>
                  <TableHead>任务</TableHead>
                  <TableHead>日期</TableHead>
                  <TableHead>时间</TableHead>
                  <TableHead>持续时间</TableHead>
                  <TableHead className="text-right">操作</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {displayEntries.map((entry) => (
                  <TableRow key={entry.id}>
                    <TableCell className="font-medium">
                      {entry.description || "未命名任务"}
                    </TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <div 
                          className="w-3 h-3 rounded-full" 
                          style={{ backgroundColor: getProjectColor(entry.projectId) }}
                        />
                        {getProjectName(entry.projectId)}
                      </div>
                    </TableCell>
                    <TableCell>
                      {getTaskName(entry.taskId) || "-"}
                    </TableCell>
                    <TableCell>
                      {new Date(entry.startTime).toLocaleDateString()}
                    </TableCell>
                    <TableCell>
                      <div className="text-sm">
                        <div>{new Date(entry.startTime).toLocaleTimeString()}</div>
                        {entry.endTime && (
                          <div className="text-muted-foreground">
                            - {new Date(entry.endTime).toLocaleTimeString()}
                          </div>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant="secondary">
                        {formatDuration(entry.duration)}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleCopyAndStart(entry)}
                          title="复制并开始新计时"
                        >
                          <Play className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleEdit(entry)}
                          title="编辑"
                        >
                          <Edit className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDelete(entry)}
                          title="删除"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </div>
        )}

        {/* 显示更多按钮 */}
        {limit && entries.length > limit && (
          <div className="text-center">
            <Button variant="outline">
              查看全部 {entries.length} 条记录
            </Button>
          </div>
        )}
      </CardContent>

      {/* 编辑对话框 */}
      <TimeEntryDialog
        open={showEditDialog}
        onOpenChange={setShowEditDialog}
        entry={selectedEntry}
        onSuccess={() => {
          setShowEditDialog(false)
          setSelectedEntry(null)
          fetchEntries()
        }}
      />

      {/* 删除确认对话框 */}
      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>确认删除</AlertDialogTitle>
            <AlertDialogDescription>
              您确定要删除这条时间记录吗？此操作无法撤销。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>取消</AlertDialogCancel>
            <AlertDialogAction onClick={confirmDelete}>
              删除
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </Card>
  )
}