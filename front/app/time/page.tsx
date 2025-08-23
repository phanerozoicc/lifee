"use client"

import { LayoutApp } from "@/app/components/layout/layout-app"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { 
  Play, 
  Pause, 
  Square, 
  Clock, 
  Calendar,
  BarChart3,
  Plus,
  RefreshCw,
  AlertCircle,
  Target,
  Users,
  Settings,
  FileText,
  TrendingUp,
  CheckCircle2,
  Timer
} from "lucide-react"
import { useState, useEffect } from "react"
import { useUser } from "@/lib/user/provider"
import { apiService } from "@/lib/services/api-service"
import type { TimeEntry, Project, Task, UserStatistics } from "@/lib/types/api"
import { toast } from "sonner"

export default function TimePage() {
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [activeTab, setActiveTab] = useState("entries")
  const [isTimerRunning, setIsTimerRunning] = useState(false)
  const [currentTime, setCurrentTime] = useState(0)
  const [currentDescription, setCurrentDescription] = useState("")
  const [currentTimeEntry, setCurrentTimeEntry] = useState<TimeEntry | null>(null)
  const [timeEntries, setTimeEntries] = useState<TimeEntry[]>([])
  const [projects, setProjects] = useState<Project[]>([])
  const [tasks, setTasks] = useState<Task[]>([])
  const [statistics, setStatistics] = useState<UserStatistics | null>(null)
  const { user } = useUser()
  const [refreshing, setRefreshing] = useState(false)

  // 格式化时间显示
  const formatDuration = (seconds: number): string => {
    const hours = Math.floor(seconds / 3600)
    const minutes = Math.floor((seconds % 3600) / 60)
    const secs = seconds % 60
    
    if (hours > 0) {
      return `${hours}h ${minutes}m`
    } else if (minutes > 0) {
      return `${minutes}m ${secs}s`
    } else {
      return `${secs}s`
    }
  }
  
  const formatTime = (dateString: string): string => {
    return new Date(dateString).toLocaleTimeString('zh-CN', {
      hour: '2-digit',
      minute: '2-digit'
    })
  }
  
  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleDateString('zh-CN')
  }

  // 加载数据
  const loadData = async () => {
    try {
      setIsLoading(true)
      setError(null)
      
      // 并行加载所有数据
      const [currentEntryRes, entriesRes, projectsRes, statsRes] = await Promise.all([
        apiService.getCurrentTimeEntry(),
        apiService.getUserTimeEntries({ page: 0, size: 20, sort: 'startTime', direction: 'DESC' }),
        apiService.getUserProjects({ page: 0, size: 50 }),
        apiService.getUserStatistics()
      ])
      
      // 设置当前时间条目
      if (currentEntryRes.data) {
        setCurrentTimeEntry(currentEntryRes.data)
        setIsTimerRunning(currentEntryRes.data.isRunning)
        setCurrentDescription(currentEntryRes.data.description || '')
        
        // 计算运行时间
        if (currentEntryRes.data.isRunning && currentEntryRes.data.startTime) {
          const startTime = new Date(currentEntryRes.data.startTime).getTime()
          const now = Date.now()
          setCurrentTime(Math.floor((now - startTime) / 1000))
        }
      }
      
      // 设置时间条目列表
      if (entriesRes.data) {
        setTimeEntries(entriesRes.data.content)
      }
      
      // 设置项目列表
      if (projectsRes.data) {
        setProjects(projectsRes.data.content)
      }
      
      // 设置统计数据
      if (statsRes.data) {
        setStatistics(statsRes.data)
      }
      
    } catch (err) {
      console.error('Failed to load data:', err)
      setError('加载数据失败，请稍后重试')
      toast.error('加载数据失败')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    if (user) {
      loadData()
    }
  }, [user])
  
  // 计时器更新
  useEffect(() => {
    let interval: NodeJS.Timeout
    
    if (isTimerRunning && currentTimeEntry) {
      interval = setInterval(() => {
        setCurrentTime(prev => prev + 1)
      }, 1000)
    }
    
    return () => {
      if (interval) {
        clearInterval(interval)
      }
    }
  }, [isTimerRunning, currentTimeEntry])

  // 计时器控制函数
  const handleStartTimer = async () => {
    try {
      const response = await apiService.startTimeTracking({
        description: currentDescription || '工作时间',
        billable: true
      })
      
      if (response.data) {
        setCurrentTimeEntry(response.data)
        setIsTimerRunning(true)
        setCurrentTime(0)
        toast.success('计时器已启动')
      }
    } catch (error) {
      console.error('Failed to start timer:', error)
      toast.error('启动计时器失败')
    }
  }
  
  const handlePauseTimer = async () => {
    try {
      const response = await apiService.pauseTimeTracking()
      
      if (response.data) {
        setCurrentTimeEntry(response.data)
        setIsTimerRunning(false)
        toast.success('计时器已暂停')
      }
    } catch (error) {
      console.error('Failed to pause timer:', error)
      toast.error('暂停计时器失败')
    }
  }
  
  const handleResumeTimer = async () => {
    try {
      const response = await apiService.resumeTimeTracking()
      
      if (response.data) {
        setCurrentTimeEntry(response.data)
        setIsTimerRunning(true)
        toast.success('计时器已恢复')
      }
    } catch (error) {
      console.error('Failed to resume timer:', error)
      toast.error('恢复计时器失败')
    }
  }
  
  const handleStopTimer = async () => {
    try {
      const response = await apiService.stopTimeTracking()
      
      if (response.data) {
        setCurrentTimeEntry(null)
        setIsTimerRunning(false)
        setCurrentTime(0)
        setCurrentDescription('')
        toast.success('计时器已停止')
        // 重新加载时间条目列表
        loadData()
      }
    } catch (error) {
      console.error('Failed to stop timer:', error)
      toast.error('停止计时器失败')
    }
  }

  const handleRefresh = async () => {
    setRefreshing(true)
    try {
      await loadData()
    } finally {
      setRefreshing(false)
    }
  }

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Card className="w-96">
          <CardHeader>
            <CardTitle>请先登录</CardTitle>
            <CardDescription>
              您需要登录才能使用时间管理功能
            </CardDescription>
          </CardHeader>
        </Card>
      </div>
    )
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* 页面标题 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">时间管理</h1>
          <p className="text-muted-foreground">
            追踪您的时间，管理项目，提升效率
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={handleRefresh}
            disabled={isLoading || refreshing}
          >
            <RefreshCw className={`h-4 w-4 mr-2 ${(isLoading || refreshing) ? 'animate-spin' : ''}`} />
            刷新
          </Button>
          <Button size="sm">
            <Plus className="h-4 w-4 mr-2" />
            新建项目
          </Button>
        </div>
      </div>

      {/* 错误提示 */}
      {error && (
        <Alert variant="destructive">
          <AlertDescription>
            加载数据时出现错误：{error}. 请尝试刷新页面。
          </AlertDescription>
        </Alert>
      )}

      {/* 快速统计 */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">今日工作</p>
                <p className="text-2xl font-bold">
                  {statistics ? `${statistics.totalHours.toFixed(1)}h` : '0h'}
                </p>
              </div>
              <Clock className="h-8 w-8 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">计费时间</p>
                <p className="text-2xl font-bold">
                  {statistics ? `${statistics.billableHours.toFixed(1)}h` : '0h'}
                </p>
              </div>
              <TrendingUp className="h-8 w-8 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">活跃项目</p>
                <p className="text-2xl font-bold">
                  {statistics ? statistics.projectCount : 0}
                </p>
              </div>
              <Target className="h-8 w-8 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">完成任务</p>
                <p className="text-2xl font-bold">
                  {statistics ? statistics.taskCount : 0}
                </p>
              </div>
              <CheckCircle2 className="h-8 w-8 text-muted-foreground" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* 主要内容区域 */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 左侧：计时器和最近活动 */}
        <div className="lg:col-span-2 space-y-6">
          {/* 计时器 */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg flex items-center gap-2">
                <Timer className="h-5 w-5" />
                时间追踪
              </CardTitle>
              <CardDescription>
                {isTimerRunning ? '正在追踪您的工作时间' : '开始追踪您的工作时间'}
              </CardDescription>
            </CardHeader>
            <CardContent className="space-y-4">
              {/* 计时器显示 */}
              <div className="text-center">
                <div className="text-4xl font-mono font-bold mb-2">
                  {formatDuration(currentTime)}
                </div>
                {currentTimeEntry && (
                  <div className="text-sm text-muted-foreground mb-4">
                    {currentTimeEntry.description || '无描述'}
                  </div>
                )}
              </div>
              
              {/* 描述输入 */}
              {!isTimerRunning && (
                <div className="space-y-2">
                  <label className="text-sm font-medium">工作描述</label>
                  <input
                    type="text"
                    placeholder="您在做什么？"
                    value={currentDescription}
                    onChange={(e) => setCurrentDescription(e.target.value)}
                    className="w-full px-3 py-2 border border-input rounded-md text-sm"
                  />
                </div>
              )}
              
              {/* 控制按钮 */}
              <div className="flex gap-2 justify-center">
                {!isTimerRunning ? (
                  <Button onClick={handleStartTimer} className="flex-1">
                    <Play className="h-4 w-4 mr-2" />
                    开始
                  </Button>
                ) : (
                  <>
                    <Button onClick={handlePauseTimer} variant="outline" className="flex-1">
                      <Pause className="h-4 w-4 mr-2" />
                      暂停
                    </Button>
                    <Button onClick={handleStopTimer} variant="destructive" className="flex-1">
                      <Square className="h-4 w-4 mr-2" />
                      停止
                    </Button>
                  </>
                )}
              </div>
              
              {/* 当前时间条目信息 */}
              {currentTimeEntry && (
                <div className="mt-4 p-3 bg-muted rounded-lg">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-muted-foreground">开始时间</span>
                    <span>{formatTime(currentTimeEntry.startTime)}</span>
                  </div>
                  {currentTimeEntry.projectId && (
                    <div className="flex items-center justify-between text-sm mt-1">
                      <span className="text-muted-foreground">项目</span>
                      <span>{currentTimeEntry.projectId}</span>
                    </div>
                  )}
                  <div className="flex items-center justify-between text-sm mt-1">
                    <span className="text-muted-foreground">计费</span>
                    <span>{currentTimeEntry.billable ? '是' : '否'}</span>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>

          {/* 标签页内容 */}
          <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="entries">时间条目</TabsTrigger>
              <TabsTrigger value="projects">项目概览</TabsTrigger>
              <TabsTrigger value="activity">最近活动</TabsTrigger>
            </TabsList>
            
            <TabsContent value="entries" className="space-y-4">
              {isLoading ? (
                <div className="space-y-4">
                  {[...Array(3)].map((_, i) => (
                    <Card key={i}>
                      <CardContent className="p-4">
                        <div className="animate-pulse space-y-2">
                          <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                          <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              ) : (
                <div className="space-y-4">
                  {timeEntries.map((entry) => (
                    <Card key={entry.id}>
                      <CardContent className="p-4">
                        <div className="flex items-center justify-between">
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-2">
                              <h4 className="font-medium">{entry.description || '无描述'}</h4>
                              {entry.billable && (
                                <Badge variant="secondary" className="text-xs">计费</Badge>
                              )}
                            </div>
                            <div className="flex items-center gap-4 text-sm text-muted-foreground">
                              <span className="flex items-center gap-1">
                                <Clock className="h-3 w-3" />
                                {entry.startTime && formatTime(entry.startTime)} - 
                                {entry.endTime ? formatTime(entry.endTime) : '进行中'}
                              </span>
                              <span className="flex items-center gap-1">
                                <Calendar className="h-3 w-3" />
                                {formatDate(entry.startTime)}
                              </span>
                              {entry.duration && (
                                <span className="flex items-center gap-1">
                                  <Timer className="h-3 w-3" />
                                  {formatDuration(entry.duration)}
                                </span>
                              )}
                            </div>
                            {entry.tags && entry.tags.length > 0 && (
                              <div className="flex gap-1 mt-2">
                                {entry.tags.map((tag, index) => (
                                  <Badge key={index} variant="outline" className="text-xs">
                                    {tag}
                                  </Badge>
                                ))}
                              </div>
                            )}
                          </div>
                          <div className="text-right">
                            <div className="font-medium">
                              {entry.duration ? formatDuration(entry.duration) : '进行中'}
                            </div>
                            {entry.isRunning && (
                              <Badge variant="default" className="text-xs mt-1">
                                运行中
                              </Badge>
                            )}
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                  {timeEntries.length === 0 && (
                    <Card>
                      <CardContent className="p-8 text-center">
                        <Clock className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                        <h3 className="font-medium mb-2">暂无时间记录</h3>
                        <p className="text-sm text-muted-foreground mb-4">
                          开始您的第一个时间记录吧！
                        </p>
                        <Button onClick={handleStartTimer}>
                          <Play className="h-4 w-4 mr-2" />
                          开始计时
                        </Button>
                      </CardContent>
                    </Card>
                  )}
                </div>
              )}
            </TabsContent>
            
            <TabsContent value="projects" className="space-y-4">
              {isLoading ? (
                <div className="grid gap-4 md:grid-cols-2">
                  {[...Array(4)].map((_, i) => (
                    <Card key={i}>
                      <CardContent className="p-4">
                        <div className="animate-pulse space-y-2">
                          <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                          <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                          <div className="h-2 bg-gray-200 rounded w-full"></div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                </div>
              ) : (
                <div className="grid gap-4 md:grid-cols-2">
                  {projects.map((project) => (
                    <Card key={project.id}>
                      <CardContent className="p-4">
                        <div className="flex items-center justify-between mb-3">
                          <div className="flex items-center gap-2">
                            <div 
                              className="w-3 h-3 rounded-full" 
                              style={{ backgroundColor: project.color || '#3b82f6' }}
                            ></div>
                            <h4 className="font-medium">{project.name}</h4>
                          </div>
                          {project.isActive && (
                            <Badge variant="secondary" className="text-xs">活跃</Badge>
                          )}
                        </div>
                        {project.description && (
                          <p className="text-sm text-muted-foreground mb-3">
                            {project.description}
                          </p>
                        )}
                        <div className="space-y-2">
                          <div className="flex justify-between text-sm">
                            <span className="text-muted-foreground">创建时间</span>
                            <span>{formatDate(project.createdAt)}</span>
                          </div>
                          <div className="flex justify-between text-sm">
                            <span className="text-muted-foreground">状态</span>
                            <span>
                              {project.isArchived ? '已归档' : project.isActive ? '活跃' : '非活跃'}
                            </span>
                          </div>
                        </div>
                      </CardContent>
                    </Card>
                  ))}
                  {projects.length === 0 && (
                    <Card className="md:col-span-2">
                      <CardContent className="p-8 text-center">
                        <Target className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                        <h3 className="font-medium mb-2">暂无项目</h3>
                        <p className="text-sm text-muted-foreground mb-4">
                          创建您的第一个项目来组织工作！
                        </p>
                        <Button>
                          <Plus className="h-4 w-4 mr-2" />
                          新建项目
                        </Button>
                      </CardContent>
                    </Card>
                  )}
                </div>
              )}
            </TabsContent>
            
            <TabsContent value="activity" className="space-y-4">
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">最近活动</CardTitle>
                  <CardDescription>
                    查看您最近的时间追踪活动
                  </CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="space-y-4">
                    {timeEntries.slice(0, 5).map((entry) => (
                      <div key={entry.id} className="flex items-center gap-3 p-3 rounded-lg bg-muted/50">
                        <div className="flex-shrink-0">
                          {entry.isRunning ? (
                            <div className="w-2 h-2 bg-green-500 rounded-full animate-pulse"></div>
                          ) : (
                            <div className="w-2 h-2 bg-gray-400 rounded-full"></div>
                          )}
                        </div>
                        <div className="flex-1 min-w-0">
                          <p className="text-sm font-medium truncate">
                            {entry.isRunning ? '正在进行' : '已完成'}: {entry.description || '无描述'}
                          </p>
                          <p className="text-xs text-muted-foreground">
                            {formatDate(entry.startTime)} {formatTime(entry.startTime)}
                            {entry.duration && ` • ${formatDuration(entry.duration)}`}
                          </p>
                        </div>
                      </div>
                    ))}
                    {timeEntries.length === 0 && (
                      <div className="text-center py-8">
                        <FileText className="h-8 w-8 mx-auto text-muted-foreground mb-2" />
                        <p className="text-sm text-muted-foreground">暂无活动记录</p>
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
            </TabsContent>
          </Tabs>
        </div>

        {/* 右侧：侧边栏 */}
        <div className="space-y-6">
          {/* 今日总结 */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">今日总结</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">总工作时间</span>
                  <span className="font-semibold">
                    {statistics?.todayHours ? `${statistics.todayHours.toFixed(1)}h` : '0h'}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">活跃项目</span>
                  <span className="font-semibold">
                    {statistics?.activeProjects || 0}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">完成任务</span>
                  <span className="font-semibold">
                    {statistics?.completedTasks || 0}
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* 本周目标 */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">本周目标</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div>
                  <div className="flex justify-between items-center mb-2">
                    <span className="text-sm text-muted-foreground">工作时间</span>
                    <span className="text-sm">
                      {statistics?.weekHours ? `${statistics.weekHours.toFixed(1)}h` : '0h'} / 40h
                    </span>
                  </div>
                  <div className="w-full bg-secondary rounded-full h-2">
                    <div 
                      className="bg-primary h-2 rounded-full transition-all duration-300"
                      style={{ 
                        width: `${Math.min((statistics?.weekHours || 0) / 40 * 100, 100)}%` 
                      }}
                    />
                  </div>
                </div>
                
                <div className="text-xs text-muted-foreground">
                  {statistics?.weekHours && statistics.weekHours >= 40 
                    ? '🎉 恭喜！您已完成本周目标' 
                    : `还需 ${(40 - (statistics?.weekHours || 0)).toFixed(1)} 小时完成目标`
                  }
                </div>
              </div>
            </CardContent>
          </Card>

          {/* 快速操作 */}
          <Card>
            <CardHeader>
              <CardTitle className="text-lg">快速操作</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                <Button 
                  variant="ghost" 
                  className="w-full justify-start h-auto p-3"
                  onClick={() => window.location.href = '/time/reports'}
                >
                  <BarChart3 className="h-4 w-4 mr-3" />
                  <div className="text-left">
                    <div className="font-medium">查看详细报告</div>
                    <div className="text-xs text-muted-foreground">分析时间使用情况</div>
                  </div>
                </Button>
                <Button 
                  variant="ghost" 
                  className="w-full justify-start h-auto p-3"
                  onClick={() => window.location.href = '/time/projects'}
                >
                  <Plus className="h-4 w-4 mr-3" />
                  <div className="text-left">
                    <div className="font-medium">创建新项目</div>
                    <div className="text-xs text-muted-foreground">开始新的工作项目</div>
                  </div>
                </Button>
                <Button 
                  variant="ghost" 
                  className="w-full justify-start h-auto p-3"
                  onClick={() => window.location.href = '/time/teams'}
                >
                  <Users className="h-4 w-4 mr-3" />
                  <div className="text-left">
                    <div className="font-medium">管理团队</div>
                    <div className="text-xs text-muted-foreground">协作和权限管理</div>
                  </div>
                </Button>
                <Button 
                  variant="ghost" 
                  className="w-full justify-start h-auto p-3"
                  onClick={() => window.location.href = '/settings'}
                >
                  <Settings className="h-4 w-4 mr-3" />
                  <div className="text-left">
                    <div className="font-medium">设置偏好</div>
                    <div className="text-xs text-muted-foreground">个性化配置</div>
                  </div>
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}