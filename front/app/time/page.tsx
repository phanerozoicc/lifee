"use client"

import { useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Timer } from "@/components/time/timer"
import { TimeEntryList } from "@/components/time/time-entry-list"
import { ProjectOverview } from "@/components/time/project-overview"
import { RecentActivity } from "@/components/time/recent-activity"
import { QuickStats } from "@/components/time/quick-stats"
import { useDashboardStore } from "@/lib/time/store"
import { useUserStore } from "@/lib/user/store"

export default function TimePage() {
  const { data: dashboardData, loading, fetchDashboardData } = useDashboardStore()
  const { user } = useUserStore()

  useEffect(() => {
    if (user) {
      fetchDashboardData()
    }
  }, [user, fetchDashboardData])

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
      </div>

      {/* 快速统计 */}
      <QuickStats data={dashboardData} loading={loading} />

      {/* 主要内容区域 */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 左侧：计时器和最近活动 */}
        <div className="lg:col-span-2 space-y-6">
          {/* 计时器 */}
          <Timer />

          {/* 标签页内容 */}
          <Tabs defaultValue="entries" className="w-full">
            <TabsList className="grid w-full grid-cols-3">
              <TabsTrigger value="entries">时间条目</TabsTrigger>
              <TabsTrigger value="projects">项目概览</TabsTrigger>
              <TabsTrigger value="activity">最近活动</TabsTrigger>
            </TabsList>
            
            <TabsContent value="entries" className="space-y-4">
              <TimeEntryList />
            </TabsContent>
            
            <TabsContent value="projects" className="space-y-4">
              <ProjectOverview />
            </TabsContent>
            
            <TabsContent value="activity" className="space-y-4">
              <RecentActivity />
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
                    {dashboardData?.todayHours ? `${dashboardData.todayHours.toFixed(1)}h` : '0h'}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">活跃项目</span>
                  <span className="font-semibold">
                    {dashboardData?.activeProjects || 0}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-muted-foreground">完成任务</span>
                  <span className="font-semibold">
                    {dashboardData?.completedTasks || 0}
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
                      {dashboardData?.weekHours ? `${dashboardData.weekHours.toFixed(1)}h` : '0h'} / 40h
                    </span>
                  </div>
                  <div className="w-full bg-secondary rounded-full h-2">
                    <div 
                      className="bg-primary h-2 rounded-full transition-all duration-300"
                      style={{ 
                        width: `${Math.min((dashboardData?.weekHours || 0) / 40 * 100, 100)}%` 
                      }}
                    />
                  </div>
                </div>
                
                <div className="text-xs text-muted-foreground">
                  {dashboardData?.weekHours && dashboardData.weekHours >= 40 
                    ? '🎉 恭喜！您已完成本周目标' 
                    : `还需 ${(40 - (dashboardData?.weekHours || 0)).toFixed(1)} 小时完成目标`
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
                <button className="w-full text-left p-2 rounded-md hover:bg-secondary transition-colors text-sm">
                  📊 查看详细报告
                </button>
                <button className="w-full text-left p-2 rounded-md hover:bg-secondary transition-colors text-sm">
                  📋 创建新项目
                </button>
                <button className="w-full text-left p-2 rounded-md hover:bg-secondary transition-colors text-sm">
                  👥 管理团队
                </button>
                <button className="w-full text-left p-2 rounded-md hover:bg-secondary transition-colors text-sm">
                  ⚙️ 设置偏好
                </button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  )
}