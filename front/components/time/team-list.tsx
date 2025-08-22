"use client"

import { useState } from "react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { MoreHorizontal, Plus, Search, Edit, Trash2, Clock, TrendingUp } from "lucide-react"
import { Team, TeamRole } from "@/lib/team/types"
import { TeamDialog } from "./team-dialog"
import { cn } from "@/lib/utils"

interface TeamListProps {
  teams: Team[]
  loading?: boolean
  onEdit?: (team: Team) => void
  onDelete?: (teamId: string) => void
  onCreateNew?: () => void
}

const roleColors = {
  [TeamRole.OWNER]: "bg-purple-100 text-purple-800 dark:bg-purple-900 dark:text-purple-300",
  [TeamRole.ADMIN]: "bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300",
  [TeamRole.MANAGER]: "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300",
  [TeamRole.MEMBER]: "bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300",
  [TeamRole.VIEWER]: "bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-300"
}

const roleLabels = {
  [TeamRole.OWNER]: "所有者",
  [TeamRole.ADMIN]: "管理员",
  [TeamRole.MANAGER]: "经理",
  [TeamRole.MEMBER]: "成员",
  [TeamRole.VIEWER]: "查看者"
}

export function TeamList({ teams, loading, onEdit, onDelete, onCreateNew }: TeamListProps) {
  const [searchTerm, setSearchTerm] = useState("")
  const [showDialog, setShowDialog] = useState(false)
  const [editingTeam, setEditingTeam] = useState<Team | null>(null)

  const filteredTeams = teams.filter(team => {
    const matchesSearch = team.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         team.description?.toLowerCase().includes(searchTerm.toLowerCase())
    return matchesSearch
  })

  const handleEdit = (team: Team) => {
    setEditingTeam(team)
    setShowDialog(true)
    onEdit?.(team)
  }

  const handleCreateNew = () => {
    setEditingTeam(null)
    setShowDialog(true)
    onCreateNew?.()
  }

  const handleCloseDialog = () => {
    setShowDialog(false)
    setEditingTeam(null)
  }

  if (loading) {
    return (
      <div className="space-y-4">
        {[...Array(3)].map((_, i) => (
          <Card key={i} className="animate-pulse">
            <CardHeader>
              <div className="h-4 bg-muted rounded w-1/3"></div>
              <div className="h-3 bg-muted rounded w-1/2"></div>
            </CardHeader>
            <CardContent>
              <div className="h-3 bg-muted rounded w-full"></div>
            </CardContent>
          </Card>
        ))}
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* 搜索和筛选 */}
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <Input
            placeholder="搜索团队..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
        <div className="flex gap-2">
          <Button onClick={handleCreateNew}>
            <Plus className="h-4 w-4 mr-2" />
            新建团队
          </Button>
        </div>
      </div>

      {/* 团队卡片 */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {filteredTeams.length === 0 ? (
          <div className="col-span-full text-center py-12 text-muted-foreground">
            {searchTerm ? "未找到匹配的团队" : "暂无团队"}
          </div>
        ) : (
          filteredTeams.map((team) => (
            <Card key={team.id} className="hover:shadow-md transition-shadow">
              <CardHeader className="pb-3">
                <div className="flex items-start justify-between">
                  <div className="flex-1">
                    <CardTitle className="text-lg">{team.name}</CardTitle>
                    {team.description && (
                      <CardDescription className="mt-1">
                        {team.description}
                      </CardDescription>
                    )}
                  </div>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="sm">
                        <MoreHorizontal className="h-4 w-4" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end">
                      <DropdownMenuItem onClick={() => handleEdit(team)}>
                        <Edit className="h-4 w-4 mr-2" />
                        编辑
                      </DropdownMenuItem>
                      <DropdownMenuItem 
                        onClick={() => onDelete?.(team.id)}
                        className="text-destructive"
                      >
                        <Trash2 className="h-4 w-4 mr-2" />
                        删除
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              </CardHeader>
              <CardContent className="space-y-4">
                {/* 团队成员头像 */}
                <div className="flex items-center gap-2">
                  <div className="flex -space-x-2">
                    {team.members?.slice(0, 5).map((member) => (
                      <Avatar key={member.id} className="h-8 w-8 border-2 border-background">
                        <AvatarImage src={member.user.profileImage} />
                        <AvatarFallback className="text-xs">
                          {member.user.displayName?.charAt(0) || member.user.email.charAt(0).toUpperCase()}
                        </AvatarFallback>
                      </Avatar>
                    ))}
                    {team.members && team.members.length > 5 && (
                      <div className="h-8 w-8 rounded-full bg-muted border-2 border-background flex items-center justify-center text-xs text-muted-foreground">
                        +{team.members.length - 5}
                      </div>
                    )}
                  </div>
                  <span className="text-sm text-muted-foreground">
                    {team.members?.length || 0} 名成员
                  </span>
                </div>

                {/* 统计信息 */}
                <div className="grid grid-cols-2 gap-4 text-sm">
                  <div className="flex items-center gap-2">
                    <Clock className="h-4 w-4 text-muted-foreground" />
                    <div>
                      <div className="font-medium">{team.totalHours?.toFixed(1) || 0}h</div>
                      <div className="text-muted-foreground">总时间</div>
                    </div>
                  </div>
                  <div className="flex items-center gap-2">
                    <TrendingUp className="h-4 w-4 text-muted-foreground" />
                    <div>
                      <div className="font-medium">{team.activeProjects || 0}</div>
                      <div className="text-muted-foreground">活跃项目</div>
                    </div>
                  </div>
                </div>

                {/* 最近活跃成员 */}
                {team.members && team.members.length > 0 && (
                  <div className="space-y-2">
                    <div className="text-sm font-medium">最近活跃</div>
                    <div className="space-y-1">
                      {team.members.slice(0, 3).map((member) => (
                        <div key={member.id} className="flex items-center justify-between text-sm">
                          <div className="flex items-center gap-2">
                            <Avatar className="h-6 w-6">
                              <AvatarImage src={member.user.profileImage} />
                              <AvatarFallback className="text-xs">
                                {member.user.displayName?.charAt(0) || member.user.email.charAt(0).toUpperCase()}
                              </AvatarFallback>
                            </Avatar>
                            <span className="truncate">
                              {member.user.displayName || member.user.email}
                            </span>
                          </div>
                          <Badge className={cn("text-xs", roleColors[member.role])}>
                            {roleLabels[member.role]}
                          </Badge>
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          ))
        )}
      </div>

      {/* 团队对话框 */}
      <TeamDialog
        open={showDialog}
        onOpenChange={handleCloseDialog}
        team={editingTeam}
      />
    </div>
  )
}