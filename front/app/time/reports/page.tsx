"use client"

import { useEffect, useState } from "react"
import { Calendar, Download, TrendingUp, Clock, DollarSign, Users } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { Badge } from "@/components/ui/badge"
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
import { TimeChart } from "@/components/time/time-chart"
import { ProjectChart } from "@/components/time/project-chart"

import { ReportTable } from "@/components/time/report-table"
import { ExportDialog } from "@/components/time/export-dialog"
import { ReportAPI } from "@/lib/time/api"
import { useUserStore } from "@/lib/user/store"
import type { ReportData, ProjectReport, TeamReport, ReportType } from "@/lib/time/types"
import { formatNumber } from "@/lib/utils"

type DateRange = {
  from: Date
  to: Date
}

export default function ReportsPage() {
  const { user } = useUserStore()
  const [loading, setLoading] = useState(false)
  const [reportData, setReportData] = useState<ReportData | null>(null)
  const [projectReports, setProjectReports] = useState<ProjectReport[]>([])
  const [teamReports, setTeamReports] = useState<TeamReport[]>([])
  const [showExportDialog, setShowExportDialog] = useState(false)
  
  // 过滤器状态
  const [dateRange, setDateRange] = useState<DateRange>({
    from: new Date(new Date().getFullYear(), new Date().getMonth(), 1),
    to: new Date()
  })
  const [selectedProject, setSelectedProject] = useState<string>("all")
  const [selectedTeam, setSelectedTeam] = useState<string>("all")
  const [reportType, setReportType] = useState<ReportType>("summary")

  useEffect(() => {
    // 获取报告数据
    const fetchReportData = async () => {
      if (!user) return
      
      setLoading(true)
      try {
        const response = await ReportAPI.getReportData(
          reportType,
          dateRange.from.toISOString(),
          dateRange.to.toISOString(),
          selectedProject !== "all" ? selectedProject : undefined,
          selectedTeam !== "all" ? selectedTeam : undefined
        )
        
        if (response.success && response.data) {
          setReportData(response.data)
        }
      } catch (error) {
        console.error("Failed to fetch report data:", error)
      } finally {
        setLoading(false)
      }
    }

    // 获取项目报告
    const fetchProjectReports = async () => {
      if (!user) return
      
      try {
        const response = await ReportAPI.getProjectReport(
          dateRange.from.toISOString(),
          dateRange.to.toISOString()
        )
        
        if (response.success && response.data) {
          setProjectReports(response.data)
        }
      } catch (error) {
        console.error("Failed to fetch project reports:", error)
      }
    }

    // 获取团队报告
    const fetchTeamReports = async () => {
      if (!user) return
      
      try {
        const response = await ReportAPI.getTeamReport(
          dateRange.from.toISOString(),
          dateRange.to.toISOString()
        )
        
        if (response.success && response.data) {
          setTeamReports(response.data)
        }
      } catch (error) {
        console.error("Failed to fetch team reports:", error)
      }
    }

    fetchReportData()
    fetchProjectReports()
    fetchTeamReports()
  }, [user, dateRange, selectedProject, selectedTeam, reportType])

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Card className="w-96">
          <CardHeader>
            <CardTitle>请先登录</CardTitle>
            <CardDescription>
              您需要登录才能查看报告
            </CardDescription>
          </CardHeader>
        </Card>
      </div>
    )
  }

  return (
    <div className="container mx-auto p-6 space-y-6">
      {/* 页面标题和操作 */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">报告分析</h1>
          <p className="text-muted-foreground">
            查看时间追踪数据和项目分析报告
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button 
            variant="outline" 
            onClick={() => setShowExportDialog(true)}
          >
            <Download className="h-4 w-4 mr-2" />
            导出报告
          </Button>
        </div>
      </div>

      {/* 过滤器 */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex flex-wrap items-center gap-4">
            {/* 日期范围 */}
            <div className="flex items-center gap-2">
              <Calendar className="h-4 w-4 text-muted-foreground" />
              <Popover>
                <PopoverTrigger asChild>
                  <Button variant="outline" className="w-auto justify-start text-left font-normal">
                    {dateRange.from.toLocaleDateString()} - {dateRange.to.toLocaleDateString()}
                  </Button>
                </PopoverTrigger>
                <PopoverContent className="w-auto p-0" align="start">
                  <CalendarComponent
                    mode="range"
                    selected={{
                      from: dateRange.from,
                      to: dateRange.to
                    }}
                    onSelect={(range) => {
                      if (range?.from && range?.to) {
                        setDateRange({ from: range.from, to: range.to })
                      }
                    }}
                    numberOfMonths={2}
                  />
                </PopoverContent>
              </Popover>
            </div>

            {/* 报告类型 */}
            <Select value={reportType} onValueChange={(value: ReportType) => setReportType(value)}>
              <SelectTrigger className="w-40">
                <SelectValue placeholder="报告类型" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="summary">汇总报告</SelectItem>
                <SelectItem value="detailed">详细报告</SelectItem>
                <SelectItem value="project">项目报告</SelectItem>
                <SelectItem value="team">团队报告</SelectItem>
              </SelectContent>
            </Select>

            {/* 项目筛选 */}
            <Select value={selectedProject} onValueChange={setSelectedProject}>
              <SelectTrigger className="w-40">
                <SelectValue placeholder="选择项目" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">所有项目</SelectItem>
                {/* 这里应该从项目列表动态生成 */}
              </SelectContent>
            </Select>

            {/* 团队筛选 */}
            <Select value={selectedTeam} onValueChange={setSelectedTeam}>
              <SelectTrigger className="w-40">
                <SelectValue placeholder="选择团队" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">所有团队</SelectItem>
                {/* 这里应该从团队列表动态生成 */}
              </SelectContent>
            </Select>
          </div>
        </CardContent>
      </Card>

      {/* 关键指标 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">总工作时间</CardTitle>
            <Clock className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {reportData?.totalHours ? `${reportData.totalHours.toFixed(1)}h` : '0h'}
            </div>
            <p className="text-xs text-muted-foreground">
              {reportData?.previousPeriodHours && reportData.totalHours > reportData.previousPeriodHours
                ? `+${((reportData.totalHours - reportData.previousPeriodHours) / reportData.previousPeriodHours * 100).toFixed(1)}% 较上期`
                : '较上期无变化'
              }
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">计费金额</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ¥{formatNumber(reportData?.totalRevenue || 0)}
            </div>
            <p className="text-xs text-muted-foreground">
              {reportData?.billableHours ? `${reportData.billableHours.toFixed(1)}h 计费时间` : '无计费时间'}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">活跃项目</CardTitle>
            <TrendingUp className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {reportData?.activeProjects || 0}
            </div>
            <p className="text-xs text-muted-foreground">
              {reportData?.completedProjects ? `${reportData.completedProjects} 个已完成` : '无已完成项目'}
            </p>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">团队成员</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {reportData?.activeMembers || 0}
            </div>
            <p className="text-xs text-muted-foreground">
              平均每人 {reportData?.averageHoursPerMember ? `${reportData.averageHoursPerMember.toFixed(1)}h` : '0h'}
            </p>
          </CardContent>
        </Card>
      </div>

      {/* 报告内容 */}
      <Tabs defaultValue="overview" className="w-full">
        <TabsList>
          <TabsTrigger value="overview">概览</TabsTrigger>
          <TabsTrigger value="projects">项目分析</TabsTrigger>
          <TabsTrigger value="teams">团队分析</TabsTrigger>
          <TabsTrigger value="detailed">详细数据</TabsTrigger>
        </TabsList>

        {/* 概览 */}
        <TabsContent value="overview" className="space-y-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle>时间趋势</CardTitle>
                <CardDescription>
                  查看指定时间范围内的工作时间变化
                </CardDescription>
              </CardHeader>
              <CardContent>
                <TimeChart data={reportData?.timeSeriesData || []} loading={loading} />
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>项目分布</CardTitle>
                <CardDescription>
                  各项目的时间分配情况
                </CardDescription>
              </CardHeader>
              <CardContent>
                <ProjectChart data={reportData?.projectDistribution || []} loading={loading} />
              </CardContent>
            </Card>
          </div>
        </TabsContent>

        {/* 项目分析 */}
        <TabsContent value="projects" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>项目报告</CardTitle>
              <CardDescription>
                各项目的详细时间和收入分析
              </CardDescription>
            </CardHeader>
            <CardContent>
              {projectReports.length === 0 ? (
                <div className="text-center py-8">
                  <TrendingUp className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                  <h3 className="font-semibold mb-2">暂无项目数据</h3>
                  <p className="text-muted-foreground">
                    在选定的时间范围内没有项目活动
                  </p>
                </div>
              ) : (
                <div className="space-y-4">
                  {projectReports.map((project) => (
                    <div key={project.projectId} className="border rounded-lg p-4">
                      <div className="flex items-center justify-between mb-3">
                        <div>
                          <h4 className="font-semibold">{project.projectName}</h4>
                          <p className="text-sm text-muted-foreground">
                            {project.clientName}
                          </p>
                        </div>
                        <Badge 
                          variant={project.status === 'active' ? 'default' : 'secondary'}
                        >
                          {project.status === 'active' ? '进行中' : '已完成'}
                        </Badge>
                      </div>
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                        <div>
                          <p className="text-muted-foreground">总时间</p>
                          <p className="font-semibold">{project.totalHours.toFixed(1)}h</p>
                        </div>
                        <div>
                          <p className="text-muted-foreground">计费时间</p>
                          <p className="font-semibold">{project.billableHours.toFixed(1)}h</p>
                        </div>
                        <div>
                          <p className="text-muted-foreground">收入</p>
                          <p className="font-semibold">¥{formatNumber(project.revenue)}</p>
                        </div>
                        <div>
                          <p className="text-muted-foreground">进度</p>
                          <p className="font-semibold">{project.progress}%</p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* 团队分析 */}
        <TabsContent value="teams" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>团队报告</CardTitle>
              <CardDescription>
                团队成员的工作时间和效率分析
              </CardDescription>
            </CardHeader>
            <CardContent>
              {teamReports.length === 0 ? (
                <div className="text-center py-8">
                  <Users className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                  <h3 className="font-semibold mb-2">暂无团队数据</h3>
                  <p className="text-muted-foreground">
                    在选定的时间范围内没有团队活动
                  </p>
                </div>
              ) : (
                <div className="space-y-4">
                  {teamReports.map((team) => (
                    <div key={team.teamId} className="border rounded-lg p-4">
                      <div className="flex items-center justify-between mb-3">
                        <h4 className="font-semibold">{team.teamName}</h4>
                        <Badge variant="outline">
                          {team.memberCount} 成员
                        </Badge>
                      </div>
                      <div className="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm">
                        <div>
                          <p className="text-muted-foreground">总时间</p>
                          <p className="font-semibold">{team.totalHours.toFixed(1)}h</p>
                        </div>
                        <div>
                          <p className="text-muted-foreground">平均时间</p>
                          <p className="font-semibold">{team.averageHours.toFixed(1)}h</p>
                        </div>
                        <div>
                          <p className="text-muted-foreground">活跃项目</p>
                          <p className="font-semibold">{team.activeProjects}</p>
                        </div>
                        <div>
                          <p className="text-muted-foreground">效率</p>
                          <p className="font-semibold">{team.efficiency}%</p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* 详细数据 */}
        <TabsContent value="detailed" className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle>详细数据表</CardTitle>
              <CardDescription>
                所有时间条目的详细信息
              </CardDescription>
            </CardHeader>
            <CardContent>
              <ReportTable 
                dateRange={dateRange}
                projectId={selectedProject !== "all" ? selectedProject : undefined}
                teamId={selectedTeam !== "all" ? selectedTeam : undefined}
              />
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* 导出对话框 */}
      <ExportDialog 
        open={showExportDialog}
        onOpenChange={setShowExportDialog}
        dateRange={dateRange}
        reportType={reportType}
        projectId={selectedProject !== "all" ? selectedProject : undefined}
        teamId={selectedTeam !== "all" ? selectedTeam : undefined}
      />
    </div>
  )
}