"use client"

import { useState } from "react"
import { MoreHorizontal, Clock, Users, Target, Calendar, DollarSign } from "lucide-react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Progress } from "@/components/ui/progress"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { cn } from "@/lib/utils"
import type { Project } from "@/lib/time/types"

interface ProjectCardProps {
  project: Project
  onEdit?: (project: Project) => void
  onDelete?: (project: Project) => void
  onView?: (project: Project) => void
  className?: string
}

export function ProjectCard({ 
  project, 
  onEdit, 
  onDelete, 
  onView,
  className 
}: ProjectCardProps) {
  const [isHovered, setIsHovered] = useState(false)

  // 计算项目进度
  const progress = project.budget && project.spent 
    ? Math.min((project.spent / project.budget) * 100, 100)
    : 0

  // 计算剩余预算
  const remainingBudget = project.budget ? project.budget - (project.spent || 0) : 0

  // 获取项目状态颜色
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'active':
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300'
      case 'completed':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300'
      case 'paused':
        return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300'
      case 'cancelled':
        return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300'
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-300'
    }
  }

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'active': return '进行中'
      case 'completed': return '已完成'
      case 'paused': return '已暂停'
      case 'cancelled': return '已取消'
      default: return '未知'
    }
  }

  return (
    <Card 
      className={cn(
        "group transition-all duration-200 hover:shadow-lg cursor-pointer",
        "border-l-4 hover:border-l-primary",
        className
      )}
      style={{ borderLeftColor: project.color }}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onClick={() => onView?.(project)}
    >
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between">
          <div className="flex-1 min-w-0">
            <div className="flex items-center gap-2 mb-1">
              <div 
                className="w-3 h-3 rounded-full flex-shrink-0" 
                style={{ backgroundColor: project.color }}
              />
              <CardTitle className="text-lg font-semibold truncate">
                {project.name}
              </CardTitle>
            </div>
            <CardDescription className="line-clamp-2">
              {project.description}
            </CardDescription>
          </div>
          
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button 
                variant="ghost" 
                size="sm" 
                className={cn(
                  "h-8 w-8 p-0 opacity-0 group-hover:opacity-100 transition-opacity",
                  isHovered && "opacity-100"
                )}
                onClick={(e) => e.stopPropagation()}
              >
                <MoreHorizontal className="h-4 w-4" />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end">
              <DropdownMenuItem onClick={(e) => {
                e.stopPropagation()
                onView?.(project)
              }}>
                查看详情
              </DropdownMenuItem>
              <DropdownMenuItem onClick={(e) => {
                e.stopPropagation()
                onEdit?.(project)
              }}>
                编辑项目
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem 
                className="text-destructive"
                onClick={(e) => {
                  e.stopPropagation()
                  onDelete?.(project)
                }}
              >
                删除项目
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
        
        <div className="flex items-center gap-2 mt-2">
          <Badge className={getStatusColor(project.status)}>
            {getStatusLabel(project.status)}
          </Badge>
          {project.client && (
            <Badge variant="outline" className="text-xs">
              {project.client.name}
            </Badge>
          )}
        </div>
      </CardHeader>
      
      <CardContent className="space-y-4">
        {/* 项目统计 */}
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div className="flex items-center gap-2">
            <Clock className="h-4 w-4 text-muted-foreground" />
            <span className="text-muted-foreground">时间:</span>
            <span className="font-medium">{project.totalHours || 0}h</span>
          </div>
          <div className="flex items-center gap-2">
            <Target className="h-4 w-4 text-muted-foreground" />
            <span className="text-muted-foreground">任务:</span>
            <span className="font-medium">{project.taskCount || 0}</span>
          </div>
        </div>

        {/* 预算信息 */}
        {project.budget && (
          <div className="space-y-2">
            <div className="flex items-center justify-between text-sm">
              <div className="flex items-center gap-2">
                <DollarSign className="h-4 w-4 text-muted-foreground" />
                <span className="text-muted-foreground">预算:</span>
              </div>
              <span className="font-medium">
                ¥{project.spent || 0} / ¥{project.budget}
              </span>
            </div>
            <Progress value={progress} className="h-2" />
            <div className="flex justify-between text-xs text-muted-foreground">
              <span>已使用 {progress.toFixed(1)}%</span>
              <span className={remainingBudget < 0 ? "text-destructive" : ""}>
                剩余 ¥{remainingBudget.toFixed(2)}
              </span>
            </div>
          </div>
        )}

        {/* 团队成员 */}
        {project.teamMembers && project.teamMembers.length > 0 && (
          <div className="space-y-2">
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Users className="h-4 w-4" />
              <span>团队成员</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="flex -space-x-2">
                {project.teamMembers.slice(0, 3).map((member) => (
                  <Avatar key={member.id} className="h-6 w-6 border-2 border-background">
                    <AvatarImage src={member.avatar} />
                    <AvatarFallback className="text-xs">
                      {member.name.charAt(0)}
                    </AvatarFallback>
                  </Avatar>
                ))}
              </div>
              {project.teamMembers.length > 3 && (
                <span className="text-xs text-muted-foreground">
                  +{project.teamMembers.length - 3} 更多
                </span>
              )}
            </div>
          </div>
        )}

        {/* 截止日期 */}
        {project.deadline && (
          <div className="flex items-center gap-2 text-sm">
            <Calendar className="h-4 w-4 text-muted-foreground" />
            <span className="text-muted-foreground">截止:</span>
            <span className="font-medium">
              {new Date(project.deadline).toLocaleDateString('zh-CN')}
            </span>
          </div>
        )}
      </CardContent>
    </Card>
  )
}