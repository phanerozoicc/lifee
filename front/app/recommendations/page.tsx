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
  Eye
} from "lucide-react"
import { useState } from "react"

export default function RecommendationsPage() {
  const [activeTab, setActiveTab] = useState("recommendations")
  
  // Mock data for recommendations
  const recommendations = [
    {
      id: "1",
      title: "React Hook 最佳实践",
      type: "复习",
      reason: "基于记忆曲线，建议复习此内容",
      knowledgeBase: "技术文档",
      lastViewed: "3天前",
      priority: "high",
      estimatedTime: "15分钟",
      progress: 0.7
    },
    {
      id: "2",
      title: "数据库设计原则",
      type: "学习",
      reason: "与您最近的项目相关",
      knowledgeBase: "学习笔记",
      lastViewed: "1周前",
      priority: "medium",
      estimatedTime: "25分钟",
      progress: 0.3
    },
    {
      id: "3",
      title: "TypeScript 高级类型",
      type: "深入",
      reason: "您在此话题上表现出兴趣",
      knowledgeBase: "技术文档",
      lastViewed: "5天前",
      priority: "medium",
      estimatedTime: "30分钟",
      progress: 0.5
    },
    {
      id: "4",
      title: "微服务架构模式",
      type: "探索",
      reason: "推荐的新内容",
      knowledgeBase: "项目资料",
      lastViewed: "从未",
      priority: "low",
      estimatedTime: "45分钟",
      progress: 0
    }
  ]

  // Mock data for learning goals
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

  // Mock data for statistics
  const stats = {
    totalFiles: 479,
    totalChats: 156,
    studyTime: "24.5小时",
    weeklyProgress: 0.75,
    knowledgeBases: 3,
    completedGoals: 8
  }

  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case "high": return "text-red-600 bg-red-50"
      case "medium": return "text-yellow-600 bg-yellow-50"
      case "low": return "text-green-600 bg-green-50"
      default: return "text-gray-600 bg-gray-50"
    }
  }

  const getTypeIcon = (type: string) => {
    switch (type) {
      case "复习": return <Clock className="h-4 w-4" />
      case "学习": return <BookOpen className="h-4 w-4" />
      case "深入": return <Brain className="h-4 w-4" />
      case "探索": return <Lightbulb className="h-4 w-4" />
      default: return <FileText className="h-4 w-4" />
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
                            {item.type}
                          </span>
                        </div>
                        <span className="text-xs text-muted-foreground">
                          {item.knowledgeBase}
                        </span>
                      </div>
                      
                      <h3 className="font-semibold text-lg mb-2">{item.title}</h3>
                      <p className="text-muted-foreground text-sm mb-3">{item.reason}</p>
                      
                      <div className="flex items-center gap-4 text-sm text-muted-foreground">
                        <div className="flex items-center gap-1">
                          <Clock className="h-3 w-3" />
                          <span>{item.estimatedTime}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <Eye className="h-3 w-3" />
                          <span>上次查看: {item.lastViewed}</span>
                        </div>
                      </div>
                      
                      {item.progress > 0 && (
                        <div className="mt-3">
                          <div className="flex items-center justify-between text-sm mb-1">
                            <span>学习进度</span>
                            <span>{Math.round(item.progress * 100)}%</span>
                          </div>
                          <div className="w-full bg-gray-200 rounded-full h-2">
                            <div 
                              className="bg-blue-500 h-2 rounded-full transition-all"
                              style={{ width: `${item.progress * 100}%` }}
                            />
                          </div>
                        </div>
                      )}
                    </div>
                    
                    <div className="flex gap-2 ml-4">
                      <Button size="sm" className="gap-2">
                        <BookOpen className="h-4 w-4" />
                        开始学习
                      </Button>
                      <Button variant="outline" size="sm">
                        稍后
                      </Button>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))}
          </div>
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