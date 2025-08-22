"use client"

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Progress } from "@/components/ui/progress"
import { Clock, DollarSign, TrendingUp, TrendingDown, Users, Calendar, Download, Eye } from "lucide-react"
import { ReportData, ProjectReport, TeamReport } from "@/lib/time/types"
import { cn } from "@/lib/utils"

interface ReportCardProps {
  title: string
  description?: string
  data: ReportData | ProjectReport | TeamReport
  type: "summary" | "project" | "team"
  loading?: boolean
  onView?: () => void
  onExport?: () => void
  className?: string
}

export function ReportCard({ 
  title, 
  description, 
  data, 
  type, 
  loading, 
  onView, 
  onExport, 
  className 
}: ReportCardProps) {
  if (loading) {
    return (
      <Card className={cn("animate-pulse", className)}>
        <CardHeader>
          <div className="h-4 bg-muted rounded w-1/2"></div>
          <div className="h-3 bg-muted rounded w-3/4"></div>
        </CardHeader>
        <CardContent>
          <div className="space-y-3">
            <div className="h-3 bg-muted rounded w-full"></div>
            <div className="h-3 bg-muted rounded w-2/3"></div>
            <div className="h-3 bg-muted rounded w-1/2"></div>
          </div>
        </CardContent>
      </Card>
    )
  }

  const renderSummaryData = (data: ReportData) => (
    <div className="space-y-4">
      <div className="grid grid-cols-2 gap-4">
        <div className="space-y-2">
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <Clock className="h-4 w-4" />
            总工作时间
          </div>
          <div className="text-2xl font-bold">{data.totalHours.toFixed(1)}h</div>
          <div className="flex items-center gap-1 text-xs">
            {data.hoursChange >= 0 ? (
              <TrendingUp className="h-3 w-3 text-green-500" />
            ) : (
              <TrendingDown className="h-3 w-3 text-red-500" />
            )}
            <span className={data.hoursChange >= 0 ? "text-green-500" : "text-red-500"}>
              {Math.abs(data.hoursChange).toFixed(1)}%
            </span>
            <span className="text-muted-foreground">vs 上周</span>
          </div>
        </div>
        
        <div className="space-y-2">
          <div className="flex items-center gap-2 text-sm text-muted-foreground">
            <DollarSign className="h-4 w-4" />
            总收入
          </div>
          <div className="text-2xl font-bold">¥{data.totalEarnings.toLocaleString()}</div>
          <div className="flex items-center gap-1 text-xs">
            {data.earningsChange >= 0 ? (
              <TrendingUp className="h-3 w-3 text-green-500" />
            ) : (
              <TrendingDown className="h-3 w-3 text-red-500" />
            )}
            <span className={data.earningsChange >= 0 ? "text-green-500" : "text-red-500"}>
              {Math.abs(data.earningsChange).toFixed(1)}%
            </span>
            <span className="text-muted-foreground">vs 上周</span>
          </div>
        </div>
      </div>
      
      <div className="grid grid-cols-2 gap-4 pt-2 border-t">
        <div className="text-center">
          <div className="text-lg font-semibold">{data.activeProjects}</div>
          <div className="text-xs text-muted-foreground">活跃项目</div>
        </div>
        <div className="text-center">
          <div className="text-lg font-semibold">{data.completedTasks}</div>
          <div className="text-xs text-muted-foreground">完成任务</div>
        </div>
      </div>
    </div>
  )

  const renderProjectData = (data: ProjectReport) => (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <div className="font-medium">{data.projectName}</div>
          <div className="text-sm text-muted-foreground">{data.clientName}</div>
        </div>
        <Badge variant={data.status === 'active' ? 'default' : 'secondary'}>
          {data.status === 'active' ? '进行中' : '已完成'}
        </Badge>
      </div>
      
      <div className="grid grid-cols-2 gap-4">
        <div>
          <div className="text-sm text-muted-foreground">工作时间</div>
          <div className="text-lg font-semibold">{data.totalHours.toFixed(1)}h</div>
        </div>
        <div>
          <div className="text-sm text-muted-foreground">收入</div>
          <div className="text-lg font-semibold">¥{data.totalEarnings.toLocaleString()}</div>
        </div>
      </div>
      
      {data.budget && data.budget > 0 && (
        <div className="space-y-2">
          <div className="flex justify-between text-sm">
            <span>预算使用</span>
            <span>{((data.totalEarnings / data.budget) * 100).toFixed(0)}%</span>
          </div>
          <Progress value={(data.totalEarnings / data.budget) * 100} className="h-2" />
          <div className="text-xs text-muted-foreground">
            已用 ¥{data.totalEarnings.toLocaleString()} / 总预算 ¥{data.budget.toLocaleString()}
          </div>
        </div>
      )}
      
      <div className="flex items-center gap-4 pt-2 border-t text-sm">
        <div className="flex items-center gap-1">
          <Users className="h-4 w-4 text-muted-foreground" />
          <span>{data.teamMembers} 名成员</span>
        </div>
        <div className="flex items-center gap-1">
          <Calendar className="h-4 w-4 text-muted-foreground" />
          <span>{data.activeDays} 活跃天数</span>
        </div>
      </div>
    </div>
  )

  const renderTeamData = (data: TeamReport) => (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <div className="font-medium">{data.teamName}</div>
          <div className="text-sm text-muted-foreground">{data.memberCount} 名成员</div>
        </div>
      </div>
      
      <div className="grid grid-cols-2 gap-4">
        <div>
          <div className="text-sm text-muted-foreground">团队总时间</div>
          <div className="text-lg font-semibold">{data.totalHours.toFixed(1)}h</div>
        </div>
        <div>
          <div className="text-sm text-muted-foreground">平均效率</div>
          <div className="text-lg font-semibold">{data.averageEfficiency.toFixed(1)}%</div>
        </div>
      </div>
      
      <div className="space-y-2">
        <div className="text-sm font-medium">工作负载分布</div>
        <div className="space-y-1">
          {data.workloadDistribution?.slice(0, 3).map((member, index) => (
            <div key={index} className="flex items-center justify-between text-sm">
              <span className="truncate">{member.name}</span>
              <div className="flex items-center gap-2">
                <div className="w-16 bg-muted rounded-full h-1.5">
                  <div 
                    className="bg-primary h-1.5 rounded-full" 
                    style={{ width: `${(member.hours / data.totalHours) * 100}%` }}
                  />
                </div>
                <span className="text-xs text-muted-foreground w-8">
                  {member.hours.toFixed(0)}h
                </span>
              </div>
            </div>
          ))}
        </div>
      </div>
      
      <div className="flex items-center gap-4 pt-2 border-t text-sm">
        <div className="flex items-center gap-1">
          <Calendar className="h-4 w-4 text-muted-foreground" />
          <span>{data.activeProjects} 个项目</span>
        </div>
        <div className="flex items-center gap-1">
          <TrendingUp className="h-4 w-4 text-muted-foreground" />
          <span>{data.completedTasks} 个任务</span>
        </div>
      </div>
    </div>
  )

  const renderData = () => {
    switch (type) {
      case "summary":
        return renderSummaryData(data as ReportData)
      case "project":
        return renderProjectData(data as ProjectReport)
      case "team":
        return renderTeamData(data as TeamReport)
      default:
        return null
    }
  }

  return (
    <Card className={cn("hover:shadow-md transition-shadow", className)}>
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between">
          <div>
            <CardTitle className="text-lg">{title}</CardTitle>
            {description && (
              <CardDescription className="mt-1">{description}</CardDescription>
            )}
          </div>
          <div className="flex gap-1">
            {onView && (
              <Button variant="ghost" size="sm" onClick={onView}>
                <Eye className="h-4 w-4" />
              </Button>
            )}
            {onExport && (
              <Button variant="ghost" size="sm" onClick={onExport}>
                <Download className="h-4 w-4" />
              </Button>
            )}
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {renderData()}
      </CardContent>
    </Card>
  )
}