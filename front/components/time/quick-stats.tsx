"use client"

import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Clock, TrendingUp, Target, Users } from "lucide-react"
import { cn } from "@/lib/utils"

interface QuickStatsProps {
  data?: {
    todayHours?: number
    weekHours?: number
    monthHours?: number
    activeProjects?: number
    completedTasks?: number
    teamMembers?: number
    efficiency?: number
  }
  loading?: boolean
  className?: string
}

export function QuickStats({ data, loading, className }: QuickStatsProps) {
  const stats = [
    {
      title: "今日工作时间",
      value: data?.todayHours ? `${data.todayHours.toFixed(1)}h` : "0h",
      description: "今天已记录的时间",
      icon: Clock,
      color: "text-blue-600",
      bgColor: "bg-blue-50 dark:bg-blue-950",
      change: "+12%",
      changeType: "positive" as const
    },
    {
      title: "本周进度",
      value: data?.weekHours ? `${data.weekHours.toFixed(1)}h` : "0h",
      description: "本周目标 40h",
      icon: TrendingUp,
      color: "text-green-600",
      bgColor: "bg-green-50 dark:bg-green-950",
      change: data?.weekHours ? `${Math.round((data.weekHours / 40) * 100)}%` : "0%",
      changeType: "neutral" as const
    },
    {
      title: "活跃项目",
      value: data?.activeProjects?.toString() || "0",
      description: "正在进行的项目",
      icon: Target,
      color: "text-orange-600",
      bgColor: "bg-orange-50 dark:bg-orange-950",
      change: "+2",
      changeType: "positive" as const
    },
    {
      title: "团队成员",
      value: data?.teamMembers?.toString() || "0",
      description: "协作团队人数",
      icon: Users,
      color: "text-purple-600",
      bgColor: "bg-purple-50 dark:bg-purple-950",
      change: "稳定",
      changeType: "neutral" as const
    }
  ]

  if (loading) {
    return (
      <div className={cn("grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4", className)}>
        {[...Array(4)].map((_, i) => (
          <Card key={i} className="animate-pulse">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <div className="h-4 bg-muted rounded w-20"></div>
              <div className="h-4 w-4 bg-muted rounded"></div>
            </CardHeader>
            <CardContent>
              <div className="h-8 bg-muted rounded w-16 mb-2"></div>
              <div className="h-3 bg-muted rounded w-24"></div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  return (
    <div className={cn("grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4", className)}>
      {stats.map((stat, index) => {
        const Icon = stat.icon
        return (
          <Card key={index} className="transition-all duration-200 hover:shadow-md">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {stat.title}
              </CardTitle>
              <div className={cn("p-2 rounded-lg", stat.bgColor)}>
                <Icon className={cn("h-4 w-4", stat.color)} />
              </div>
            </CardHeader>
            <CardContent>
              <div className="flex items-baseline justify-between">
                <div className="text-2xl font-bold">{stat.value}</div>
                <Badge 
                  variant={stat.changeType === "positive" ? "default" : "secondary"}
                  className="text-xs"
                >
                  {stat.change}
                </Badge>
              </div>
              <p className="text-xs text-muted-foreground mt-1">
                {stat.description}
              </p>
            </CardContent>
          </Card>
        )
      })}
    </div>
  )
}