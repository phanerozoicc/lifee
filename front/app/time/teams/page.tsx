"use client"

import { useEffect, useState } from "react"
import { Plus, Users, Crown, Shield, User, MoreHorizontal } from "lucide-react"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { TeamForm } from "@/components/time/team-form"
import { TeamMemberForm } from "@/components/time/team-member-form"
import { WorkloadChart } from "@/components/time/workload-chart"
import { useTeamStore } from "@/lib/time/store"
import { useUserStore } from "@/lib/user/store"
import type { Team } from "@/lib/time/types"
import { cn } from "@/lib/utils"

const roleIcons = {
  owner: Crown,
  admin: Shield,
  member: User
}

const roleLabels = {
  owner: "所有者",
  admin: "管理员",
  member: "成员"
}

const roleColors = {
  owner: "bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300",
  admin: "bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300",
  member: "bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-300"
}

export default function TeamsPage() {
  const { user } = useUserStore()
  const {
    teams,
    teamMembers,
    loading,
    fetchTeams,
    fetchTeamMembers
  } = useTeamStore()

  const [selectedTeam, setSelectedTeam] = useState<Team | null>(null)
  const [showTeamForm, setShowTeamForm] = useState(false)
  const [showMemberForm, setShowMemberForm] = useState(false)

  useEffect(() => {
    if (user) {
      fetchTeams()
    }
  }, [user, fetchTeams])

  useEffect(() => {
    if (selectedTeam) {
      fetchTeamMembers(selectedTeam.id)
    }
  }, [selectedTeam, fetchTeamMembers])

  const handleTeamSelect = (team: Team) => {
    setSelectedTeam(team)
  }

  const currentTeamMembers = selectedTeam ? teamMembers[selectedTeam.id] || [] : []

  if (!user) {
    return (
      <div className="flex items-center justify-center min-h-screen">
        <Card className="w-96">
          <CardHeader>
            <CardTitle>请先登录</CardTitle>
            <CardDescription>
              您需要登录才能管理团队
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
          <h1 className="text-3xl font-bold tracking-tight">团队管理</h1>
          <p className="text-muted-foreground">
            管理团队成员、权限和工作负载
          </p>
        </div>
        <Dialog open={showTeamForm} onOpenChange={setShowTeamForm}>
          <DialogTrigger asChild>
            <Button>
              <Plus className="h-4 w-4 mr-2" />
              创建团队
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>创建新团队</DialogTitle>
              <DialogDescription>
                设置团队基本信息
              </DialogDescription>
            </DialogHeader>
            <TeamForm onSuccess={() => setShowTeamForm(false)} />
          </DialogContent>
        </Dialog>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* 左侧：团队列表 */}
        <div className="lg:col-span-1">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Users className="h-5 w-5" />
                我的团队
              </CardTitle>
              <CardDescription>
                选择一个团队查看详细信息
              </CardDescription>
            </CardHeader>
            <CardContent>
              {loading ? (
                <div className="space-y-3">
                  {[...Array(3)].map((_, i) => (
                    <div key={i} className="animate-pulse">
                      <div className="h-12 bg-muted rounded-lg"></div>
                    </div>
                  ))}
                </div>
              ) : teams.length === 0 ? (
                <div className="text-center py-8">
                  <Users className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                  <h3 className="font-semibold mb-2">暂无团队</h3>
                  <p className="text-sm text-muted-foreground mb-4">
                    创建您的第一个团队
                  </p>
                  <Button 
                    variant="outline" 
                    size="sm"
                    onClick={() => setShowTeamForm(true)}
                  >
                    <Plus className="h-4 w-4 mr-2" />
                    创建团队
                  </Button>
                </div>
              ) : (
                <div className="space-y-2">
                  {teams.map((team) => (
                    <div
                      key={team.id}
                      className={cn(
                        "p-3 rounded-lg border cursor-pointer transition-colors",
                        selectedTeam?.id === team.id
                          ? "bg-primary/10 border-primary"
                          : "hover:bg-muted/50"
                      )}
                      onClick={() => handleTeamSelect(team)}
                    >
                      <div className="flex items-center justify-between">
                        <div>
                          <h4 className="font-medium">{team.name}</h4>
                          <p className="text-sm text-muted-foreground">
                            {team.description}
                          </p>
                        </div>
                        <Badge variant="secondary">
                          {teamMembers[team.id]?.length || 0} 人
                        </Badge>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* 右侧：团队详情 */}
        <div className="lg:col-span-2">
          {selectedTeam ? (
            <Tabs defaultValue="members" className="w-full">
              <div className="flex items-center justify-between mb-4">
                <TabsList>
                  <TabsTrigger value="members">成员管理</TabsTrigger>
                  <TabsTrigger value="workload">工作负载</TabsTrigger>
                  <TabsTrigger value="settings">团队设置</TabsTrigger>
                </TabsList>
                <Dialog open={showMemberForm} onOpenChange={setShowMemberForm}>
                  <DialogTrigger asChild>
                    <Button size="sm">
                      <Plus className="h-4 w-4 mr-2" />
                      添加成员
                    </Button>
                  </DialogTrigger>
                  <DialogContent>
                    <DialogHeader>
                      <DialogTitle>添加团队成员</DialogTitle>
                      <DialogDescription>
                        邀请新成员加入 {selectedTeam.name}
                      </DialogDescription>
                    </DialogHeader>
                    <TeamMemberForm 
                      teamId={selectedTeam.id}
                      onSuccess={() => setShowMemberForm(false)} 
                    />
                  </DialogContent>
                </Dialog>
              </div>

              {/* 成员管理 */}
              <TabsContent value="members">
                <Card>
                  <CardHeader>
                    <CardTitle>{selectedTeam.name} - 成员</CardTitle>
                    <CardDescription>
                      管理团队成员和权限
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    {currentTeamMembers.length === 0 ? (
                      <div className="text-center py-8">
                        <User className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                        <h3 className="font-semibold mb-2">暂无成员</h3>
                        <p className="text-sm text-muted-foreground mb-4">
                          邀请成员加入团队
                        </p>
                        <Button 
                          variant="outline" 
                          size="sm"
                          onClick={() => setShowMemberForm(true)}
                        >
                          <Plus className="h-4 w-4 mr-2" />
                          添加成员
                        </Button>
                      </div>
                    ) : (
                      <div className="space-y-4">
                        {currentTeamMembers.map((member) => {
                          const RoleIcon = roleIcons[member.role]
                          return (
                            <div
                              key={member.id}
                              className="flex items-center justify-between p-4 border rounded-lg"
                            >
                              <div className="flex items-center gap-3">
                                <Avatar>
                                  <AvatarImage src={member.user?.profile_image} />
                                  <AvatarFallback>
                                    {member.user?.display_name?.charAt(0) || 'U'}
                                  </AvatarFallback>
                                </Avatar>
                                <div>
                                  <h4 className="font-medium">
                                    {member.user?.display_name || '未知用户'}
                                  </h4>
                                  <p className="text-sm text-muted-foreground">
                                    {member.user?.email}
                                  </p>
                                </div>
                              </div>
                              <div className="flex items-center gap-2">
                                <Badge 
                                  variant="secondary"
                                  className={roleColors[member.role]}
                                >
                                  <RoleIcon className="h-3 w-3 mr-1" />
                                  {roleLabels[member.role]}
                                </Badge>
                                <DropdownMenu>
                                  <DropdownMenuTrigger asChild>
                                    <Button variant="ghost" size="sm">
                                      <MoreHorizontal className="h-4 w-4" />
                                    </Button>
                                  </DropdownMenuTrigger>
                                  <DropdownMenuContent align="end">
                                    <DropdownMenuItem>
                                      编辑权限
                                    </DropdownMenuItem>
                                    <DropdownMenuItem className="text-destructive">
                                      移除成员
                                    </DropdownMenuItem>
                                  </DropdownMenuContent>
                                </DropdownMenu>
                              </div>
                            </div>
                          )
                        })}
                      </div>
                    )}
                  </CardContent>
                </Card>
              </TabsContent>

              {/* 工作负载 */}
              <TabsContent value="workload">
                <Card>
                  <CardHeader>
                    <CardTitle>工作负载分析</CardTitle>
                    <CardDescription>
                      查看团队成员的工作时间分布
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <WorkloadChart teamId={selectedTeam.id} />
                  </CardContent>
                </Card>
              </TabsContent>

              {/* 团队设置 */}
              <TabsContent value="settings">
                <Card>
                  <CardHeader>
                    <CardTitle>团队设置</CardTitle>
                    <CardDescription>
                      管理团队基本信息和权限设置
                    </CardDescription>
                  </CardHeader>
                  <CardContent>
                    <div className="space-y-6">
                      <div>
                        <h4 className="font-medium mb-2">基本信息</h4>
                        <div className="space-y-3">
                          <div>
                            <label className="text-sm font-medium">团队名称</label>
                            <p className="text-sm text-muted-foreground">
                              {selectedTeam.name}
                            </p>
                          </div>
                          <div>
                            <label className="text-sm font-medium">团队描述</label>
                            <p className="text-sm text-muted-foreground">
                              {selectedTeam.description || '暂无描述'}
                            </p>
                          </div>
                          <div>
                            <label className="text-sm font-medium">创建时间</label>
                            <p className="text-sm text-muted-foreground">
                              {new Date(selectedTeam.created_at).toLocaleDateString()}
                            </p>
                          </div>
                        </div>
                      </div>
                      
                      <div>
                        <h4 className="font-medium mb-2">权限设置</h4>
                        <div className="space-y-3">
                          <div className="flex items-center justify-between">
                            <div>
                              <p className="font-medium">成员可以邀请其他人</p>
                              <p className="text-sm text-muted-foreground">
                                允许团队成员邀请新成员加入
                              </p>
                            </div>
                            <Button variant="outline" size="sm">
                              配置
                            </Button>
                          </div>
                          <div className="flex items-center justify-between">
                            <div>
                              <p className="font-medium">成员可以创建项目</p>
                              <p className="text-sm text-muted-foreground">
                                允许团队成员创建新项目
                              </p>
                            </div>
                            <Button variant="outline" size="sm">
                              配置
                            </Button>
                          </div>
                          <div className="flex items-center justify-between">
                            <div>
                              <p className="font-medium">时间追踪可见性</p>
                              <p className="text-sm text-muted-foreground">
                                设置成员间时间记录的可见范围
                              </p>
                            </div>
                            <Button variant="outline" size="sm">
                              配置
                            </Button>
                          </div>
                        </div>
                      </div>
                      
                      <div className="pt-4 border-t">
                        <Button variant="destructive" size="sm">
                          删除团队
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              </TabsContent>
            </Tabs>
          ) : (
            <Card>
              <CardContent className="flex flex-col items-center justify-center py-12">
                <Users className="h-16 w-16 text-muted-foreground mb-4" />
                <h3 className="text-lg font-semibold mb-2">选择一个团队</h3>
                <p className="text-muted-foreground text-center">
                  从左侧列表中选择一个团队来查看详细信息
                </p>
              </CardContent>
            </Card>
          )}
        </div>
      </div>
    </div>
  )
}