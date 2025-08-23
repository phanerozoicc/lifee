"use client"

import { LayoutApp } from "@/app/components/layout/layout-app"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { 
  Brain, 
  Target, 
  TrendingUp, 
  Clock, 
  BookOpen,
  Star,
  Calendar,
  BarChart3,
  FileText,
  Lightbulb,
  CheckCircle,
  Plus,
  Eye,
  AlertCircle
} from "lucide-react"
import { useState, useEffect } from "react"
import { useUser } from "@/lib/user/provider"
import { apiService } from "@/lib/services/api-service"
import type { Recommendation, RecommendationItem } from "@/lib/types/api"
import { toast } from "sonner"

export default function RecommendationsPage() {
  const [activeTab, setActiveTab] = useState("recommendations")
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [recommendations, setRecommendations] = useState<Recommendation[]>([])
  const [highQualityRecommendations, setHighQualityRecommendations] = useState<Recommendation[]>([])
  const { user } = useUser()
  
  // 加载数据
  const loadData = async () => {
    try {
      setIsLoading(true)
      setError(null)
      
      const [userRecommendations, highQualityRecs] = await Promise.all([
        apiService.getUserRecommendations(),
        apiService.getHighQualityRecommendations()
      ])
      
      if (userRecommendations.data) {
        setRecommendations(userRecommendations.data)
      }
      
      if (highQualityRecs.data) {
        setHighQualityRecommendations(highQualityRecs.data)
      }
      
    } catch (err) {
      console.error('Failed to load recommendations:', err)
      setError('加载推荐内容失败，请稍后重试')
      toast.error('加载推荐内容失败')
    } finally {
      setIsLoading(false)
    }
  }
  
  useEffect(() => {
    if (user) {
      loadData()
    }
  }, [user])
  
  // 格式化日期
  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleDateString('zh-CN')
  }

  // Mock data for learning goals (这部分暂时保持模拟数据)
  const learningGoals = [
    {
      id: "1",
      title: "掌握React 18新特性",
      description: "学习并实践React 18的并发特性和Suspense",
      progress: 0.6,
      deadline: "2024-02-15",
      status: "in_progress",
      tasks: [
        { id: "1-1", title: "学习Concurrent Features", completed: true },
        { id: "1-2", title: "实践Suspense", completed: true },
        { id: "1-3", title: "构建示例项目", completed: false },
        { id: "1-4", title: "性能优化", completed: false }
      ]
    },
    {
      id: "2",
      title: "深入理解数据库设计",
      description: "系统学习数据库设计原则和最佳实践",
      progress: 0.3,
      deadline: "2024-03-01",
      status: "in_progress",
      tasks: [
        { id: "2-1", title: "关系型数据库理论", completed: true },
        { id: "2-2", title: "索引设计", completed: false },
        { id: "2-3", title: "查询优化", completed: false },
        { id: "2-4", title: "实际项目应用", completed: false }
      ]
    }
  ]

  // Mock data for statistics (这部分暂时保持模拟数据)
  const stats = {
    totalFiles: 479,
    totalChats: 156,
    studyTime: "24.5小时",
    weeklyProgress: 0.75,
    knowledgeBases: 3,
    completedGoals: 8
  }

  const getPriorityColor = (priority: 'LOW' | 'MEDIUM' | 'HIGH') => {
    switch (priority) {
      case "HIGH": return "text-red-600 bg-red-50"
      case "MEDIUM": return "text-yellow-600 bg-yellow-50"
      case "LOW": return "text-green-600 bg-green-50"
      default: return "text-gray-600 bg-gray-50"
    }
  }

  const getTypeIcon = (type: 'LEARNING' | 'REVIEW' | 'PRACTICE') => {
    switch (type) {
      case "REVIEW": return <Clock className="h-4 w-4" />
      case "LEARNING": return <BookOpen className="h-4 w-4" />
      case "PRACTICE": return <Brain className="h-4 w-4" />
      default: return <FileText className="h-4 w-4" />
    }
  }
  
  const getTypeName = (type: 'LEARNING' | 'REVIEW' | 'PRACTICE') => {
    switch (type) {
      case "REVIEW": return "复习"
      case "LEARNING": return "学习"
      case "PRACTICE": return "练习"
      default: return "其他"
    }
  }
  
  const getPriorityName = (priority: 'LOW' | 'MEDIUM' | 'HIGH') => {
    switch (priority) {
      case "HIGH": return "高"
      case "MEDIUM": return "中"
      case "LOW": return "低"
      default: return "未知"
    }
  }

  return (
    <LayoutApp>
        <div className="container mx-auto p-6 max-w-7xl pt-20">
      <div className="mb-6">
        <h1 className="text-2xl font-bold flex items-center gap-2">
          <Brain className="h-6 w-6" />
          智能推荐
        </h1>
        <p className="text-muted-foreground mt-1">
          基于记忆曲线和学习行为的个性化内容推荐
        </p>
      </div>
      
      {/* 错误提示 */}
      {error && (
        <div className="mb-6 p-4 bg-destructive/10 border border-destructive/20 rounded-lg">
          <div className="flex items-center gap-2 text-destructive">
            <AlertCircle className="h-4 w-4" />
            <span className="text-sm">{error}</span>
          </div>
        </div>
      )}

      {/* Recommendation Algorithm Explanation */}
      <Card className="mb-6 bg-gradient-to-r from-blue-50 to-indigo-50 border-blue-200">
        <CardHeader>
          <CardTitle className="text-lg flex items-center gap-2">
            <Lightbulb className="h-5 w-5 text-blue-600" />
            推荐算法说明
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="flex items-start gap-3">
              <div className="bg-blue-100 p-2 rounded-lg">
                <Clock className="h-4 w-4 text-blue-600" />
              </div>
              <div>
                <h4 className="font-medium text-sm">记忆曲线分析</h4>
                <p className="text-xs text-muted-foreground mt-1">
                  根据艾宾浩斯遗忘曲线，推荐复习时机
                </p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="bg-green-100 p-2 rounded-lg">
                <TrendingUp className="h-4 w-4 text-green-600" />
              </div>
              <div>
                <h4 className="font-medium text-sm">学习行为分析</h4>
                <p className="text-xs text-muted-foreground mt-1">
                  基于您的学习频率和偏好推荐内容
                </p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="bg-purple-100 p-2 rounded-lg">
                <Brain className="h-4 w-4 text-purple-600" />
              </div>
              <div>
                <h4 className="font-medium text-sm">知识关联</h4>
                <p className="text-xs text-muted-foreground mt-1">
                  推荐与当前学习内容相关的知识点
                </p>
              </div>
            </div>
            <div className="flex items-start gap-3">
              <div className="bg-orange-100 p-2 rounded-lg">
                <Target className="h-4 w-4 text-orange-600" />
              </div>
              <div>
                <h4 className="font-medium text-sm">目标导向</h4>
                <p className="text-xs text-muted-foreground mt-1">
                  结合您的学习目标推荐相关内容
                </p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Statistics Overview */}
      <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 mb-6">
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-2">
              <FileText className="h-4 w-4 text-blue-500" />
              <div>
                <p className="text-2xl font-bold">{stats.totalFiles}</p>
                <p className="text-xs text-muted-foreground">文件总数</p>
              </div>
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-2">
              <Brain className="h-4 w-4 text-green-500" />
              <div>
                <p className="text-2xl font-bold">{stats.totalChats}</p>
                <p className="text-xs text-muted-foreground">对话次数</p>
              </div>
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-2">
              <Clock className="h-4 w-4 text-purple-500" />
              <div>
                <p className="text-2xl font-bold">{stats.studyTime}</p>
                <p className="text-xs text-muted-foreground">学习时长</p>
              </div>
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-2">
              <TrendingUp className="h-4 w-4 text-orange-500" />
              <div>
                <p className="text-2xl font-bold">{Math.round(stats.weeklyProgress * 100)}%</p>
                <p className="text-xs text-muted-foreground">周进度</p>
              </div>
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-2">
              <BookOpen className="h-4 w-4 text-indigo-500" />
              <div>
                <p className="text-2xl font-bold">{stats.knowledgeBases}</p>
                <p className="text-xs text-muted-foreground">知识库</p>
              </div>
            </div>
          </CardContent>
        </Card>
        
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center gap-2">
              <Target className="h-4 w-4 text-red-500" />
              <div>
                <p className="text-2xl font-bold">{stats.completedGoals}</p>
                <p className="text-xs text-muted-foreground">完成目标</p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="grid w-full grid-cols-3">
          <TabsTrigger value="recommendations" className="gap-2">
            <Star className="h-4 w-4" />
            智能推荐
          </TabsTrigger>
          <TabsTrigger value="goals" className="gap-2">
            <Target className="h-4 w-4" />
            学习目标
          </TabsTrigger>
          <TabsTrigger value="analytics" className="gap-2">
            <BarChart3 className="h-4 w-4" />
            学习分析
          </TabsTrigger>
        </TabsList>

        {/* Recommendations Tab */}
        <TabsContent value="recommendations" className="space-y-4">
          {isLoading ? (
            <div className="grid gap-4">
              {[...Array(3)].map((_, i) => (
                <Card key={i}>
                  <CardContent className="p-6">
                    <div className="animate-pulse space-y-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 bg-gray-200 rounded-lg"></div>
                        <div className="space-y-2 flex-1">
                          <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                          <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                        </div>
                      </div>
                      <div className="h-2 bg-gray-200 rounded w-full"></div>
                      <div className="flex gap-2">
                        <div className="h-8 bg-gray-200 rounded flex-1"></div>
                        <div className="h-8 w-8 bg-gray-200 rounded"></div>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          ) : recommendations.length === 0 ? (
            <Card>
              <CardContent className="p-8 text-center">
                <Brain className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                <h3 className="font-medium mb-2">暂无推荐内容</h3>
                <p className="text-sm text-muted-foreground mb-4">
                  系统正在为您分析学习行为，稍后将为您推荐合适的内容！
                </p>
              </CardContent>
            </Card>
          ) : (
            <div className="grid gap-4">
              {recommendations.map((item) => (
                <Card key={item.id} className="hover:shadow-md transition-shadow">
                  <CardContent className="p-6">
                    <div className="flex items-start justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-3 mb-2">
                          <div className="flex items-center gap-2">
                            {getTypeIcon(item.type)}
                            <span className={`px-2 py-1 rounded-full text-xs font-medium ${
                              getPriorityColor(item.priority)
                            }`}>
                              {getTypeName(item.type)}
                            </span>
                          </div>
                          <span className="text-xs text-muted-foreground">
                            评分: {item.score.toFixed(1)}
                          </span>
                        </div>
                        
                        <h3 className="font-semibold text-lg mb-2">{item.title}</h3>
                        <p className="text-muted-foreground text-sm mb-3">{item.description || '暂无描述'}</p>
                        
                        <div className="flex items-center gap-4 text-sm text-muted-foreground">
                          <div className="flex items-center gap-1">
                            <Target className="h-3 w-3" />
                            <span>优先级: {getPriorityName(item.priority)}</span>
                          </div>
                          <div className="flex items-center gap-1">
                            <Calendar className="h-3 w-3" />
                            <span>创建: {formatDate(item.createdAt)}</span>
                          </div>
                        </div>
                        
                        <div className="mt-3">
                          <div className="flex items-center justify-between text-sm mb-1">
                            <span>推荐评分</span>
                            <span>{item.score.toFixed(1)}/10</span>
                          </div>
                          <div className="w-full bg-gray-200 rounded-full h-2">
                            <div 
                              className="bg-blue-500 h-2 rounded-full transition-all"
                              style={{ width: `${(item.score / 10) * 100}%` }}
                            />
                          </div>
                        </div>
                      </div>
                      
                      <div className="flex gap-2 ml-4">
                        <Button size="sm" className="gap-2">
                          <Eye className="h-4 w-4" />
                          查看详情
                        </Button>
                        <Button variant="outline" size="sm">
                          <Star className="h-4 w-4" />
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </TabsContent>

        {/* Learning Goals Tab */}
        <TabsContent value="goals" className="space-y-4">
          <div className="flex justify-between items-center">
            <h3 className="text-lg font-semibold">学习目标</h3>
            <Button className="gap-2">
              <Plus className="h-4 w-4" />
              新建目标
            </Button>
          </div>
          
          <div className="grid gap-4">
            {learningGoals.map((goal) => (
              <Card key={goal.id}>
                <CardHeader>
                  <div className="flex items-start justify-between">
                    <div>
                      <CardTitle className="text-lg">{goal.title}</CardTitle>
                      <CardDescription className="mt-1">
                        {goal.description}
                      </CardDescription>
                    </div>
                    <div className="text-right">
                      <div className="text-2xl font-bold text-blue-600">
                        {Math.round(goal.progress * 100)}%
                      </div>
                      <div className="text-xs text-muted-foreground flex items-center gap-1">
                        <Calendar className="h-3 w-3" />
                        {goal.deadline}
                      </div>
                    </div>
                  </div>
                </CardHeader>
                <CardContent>
                  <div className="space-y-3">
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div 
                        className="bg-blue-500 h-2 rounded-full transition-all"
                        style={{ width: `${goal.progress * 100}%` }}
                      />
                    </div>
                    
                    <div className="space-y-2">
                      <h4 className="font-medium text-sm">任务清单</h4>
                      {goal.tasks.map((task) => (
                        <div key={task.id} className="flex items-center gap-2">
                          <CheckCircle className={`h-4 w-4 ${
                            task.completed ? "text-green-500" : "text-gray-300"
                          }`} />
                          <span className={`text-sm ${
                            task.completed ? "line-through text-muted-foreground" : ""
                          }`}>
                            {task.title}
                          </span>
                        </div>
                      ))}
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
        </TabsContent>

        {/* Analytics Tab */}
        <TabsContent value="analytics" className="space-y-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <TrendingUp className="h-5 w-5" />
                  学习趋势
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-center py-12 text-muted-foreground">
                  <BarChart3 className="h-12 w-12 mx-auto mb-4 opacity-50" />
                  <p>学习趋势图表</p>
                  <p className="text-sm">（图表组件待集成）</p>
                </div>
              </CardContent>
            </Card>
            
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Brain className="h-5 w-5" />
                  知识分布
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="text-center py-12 text-muted-foreground">
                  <Brain className="h-12 w-12 mx-auto mb-4 opacity-50" />
                  <p>知识分布图表</p>
                  <p className="text-sm">（图表组件待集成）</p>
                </div>
              </CardContent>
            </Card>
          </div>
        </TabsContent>
      </Tabs>
        </div>
    </LayoutApp>
  )
}