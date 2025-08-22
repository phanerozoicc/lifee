"use client"

import { useState } from 'react'
import { useUser } from '@/lib/user-store/provider'
import { useUserTimeSettings } from '@/lib/user/hooks'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Textarea } from '@/components/ui/textarea'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { Switch } from '@/components/ui/switch'
import { Separator } from '@/components/ui/separator'
import { Badge } from '@/components/ui/badge'
import { toast } from 'sonner'
import { User, Clock, Shield, Building, Mail, Phone, MapPin, Save, Loader2 } from 'lucide-react'
import type { UserRole, UserPermission } from '@/lib/user/types'

export default function ProfilePage() {
  const { user, updateUser, isLoading: userLoading } = useUser()
  const { timeSettings, updateTimeSettings, loading: settingsLoading, updating } = useUserTimeSettings()
  const [saving, setSaving] = useState(false)

  // 表单状态
  const [profile, setProfile] = useState({
    full_name: user?.full_name || '',
    email: user?.email || '',
    phone: user?.phone || '',
    address: user?.address || '',
    bio: user?.bio || '',
    department: user?.department || '',
    job_title: user?.job_title || ''
  })

  const [timeConfig, setTimeConfig] = useState({
    timezone: timeSettings?.timezone || 'UTC',
    hourly_rate: timeSettings?.hourly_rate || 0,
    weekly_capacity: timeSettings?.weekly_capacity || 40,
    auto_start_timer: timeSettings?.auto_start_timer || false,
    reminder_enabled: timeSettings?.reminder_enabled || false,
    reminder_interval: timeSettings?.reminder_interval || 30
  })

  // 保存个人信息
  const handleSaveProfile = async () => {
    setSaving(true)
    try {
      await updateUser(profile)
      toast.success('个人信息已更新')
    } catch {
      toast.error('更新失败，请重试')
    } finally {
      setSaving(false)
    }
  }

  // 保存时间设置
  const handleSaveTimeSettings = async () => {
    try {
      const success = await updateTimeSettings(timeConfig)
      if (success) {
        toast.success('时间设置已更新')
      } else {
        toast.error('更新失败，请重试')
      }
    } catch {
      toast.error('更新失败，请重试')
    }
  }

  // 角色显示名称
  const getRoleDisplayName = (role: UserRole) => {
    const roleNames = {
      admin: '管理员',
      project_manager: '项目经理',
      team_leader: '团队领导',
      member: '团队成员'
    }
    return roleNames[role] || role
  }

  // 权限显示名称
  const getPermissionDisplayName = (permission: UserPermission) => {
    const permissionNames = {
      manage_users: '用户管理',
      manage_projects: '项目管理',
      manage_teams: '团队管理',
      manage_time_entries: '时间条目管理',
      view_time_entries: '查看时间条目',
      view_reports: '查看报告',
      view_detailed_reports: '查看详细报告',
      export_data: '导出数据',
      manage_billing: '账单管理'
    }
    return permissionNames[permission] || permission
  }

  if (userLoading || settingsLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        <Loader2 className="h-8 w-8 animate-spin" />
      </div>
    )
  }

  return (
    <div className="container mx-auto py-6 space-y-6">
      <div className="flex items-center gap-2">
        <User className="h-6 w-6" />
        <h1 className="text-2xl font-bold">个人设置</h1>
      </div>

      <div className="grid gap-6 md:grid-cols-2">
        {/* 基本信息 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User className="h-5 w-5" />
              基本信息
            </CardTitle>
            <CardDescription>
              管理您的个人资料和联系信息
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="full_name">姓名</Label>
              <Input
                id="full_name"
                value={profile.full_name}
                onChange={(e) => setProfile(prev => ({ ...prev, full_name: e.target.value }))}
                placeholder="请输入您的姓名"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">邮箱</Label>
              <div className="flex items-center gap-2">
                <Mail className="h-4 w-4 text-muted-foreground" />
                <Input
                  id="email"
                  type="email"
                  value={profile.email}
                  onChange={(e) => setProfile(prev => ({ ...prev, email: e.target.value }))}
                  placeholder="请输入邮箱地址"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="phone">电话</Label>
              <div className="flex items-center gap-2">
                <Phone className="h-4 w-4 text-muted-foreground" />
                <Input
                  id="phone"
                  value={profile.phone}
                  onChange={(e) => setProfile(prev => ({ ...prev, phone: e.target.value }))}
                  placeholder="请输入电话号码"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="address">地址</Label>
              <div className="flex items-center gap-2">
                <MapPin className="h-4 w-4 text-muted-foreground" />
                <Input
                  id="address"
                  value={profile.address}
                  onChange={(e) => setProfile(prev => ({ ...prev, address: e.target.value }))}
                  placeholder="请输入地址"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="bio">个人简介</Label>
              <Textarea
                id="bio"
                value={profile.bio}
                onChange={(e) => setProfile(prev => ({ ...prev, bio: e.target.value }))}
                placeholder="请输入个人简介"
                rows={3}
              />
            </div>

            <Separator />

            <div className="space-y-2">
              <Label htmlFor="department">部门</Label>
              <div className="flex items-center gap-2">
                <Building className="h-4 w-4 text-muted-foreground" />
                <Input
                  id="department"
                  value={profile.department}
                  onChange={(e) => setProfile(prev => ({ ...prev, department: e.target.value }))}
                  placeholder="请输入部门"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="job_title">职位</Label>
              <Input
                id="job_title"
                value={profile.job_title}
                onChange={(e) => setProfile(prev => ({ ...prev, job_title: e.target.value }))}
                placeholder="请输入职位"
              />
            </div>

            <Button 
              onClick={handleSaveProfile} 
              disabled={saving}
              className="w-full"
            >
              {saving ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  保存中...
                </>
              ) : (
                <>
                  <Save className="mr-2 h-4 w-4" />
                  保存基本信息
                </>
              )}
            </Button>
          </CardContent>
        </Card>

        {/* 时间设置 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Clock className="h-5 w-5" />
              时间设置
            </CardTitle>
            <CardDescription>
              配置您的时间追踪偏好设置
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="timezone">时区</Label>
              <Select
                value={timeConfig.timezone}
                onValueChange={(value) => setTimeConfig(prev => ({ ...prev, timezone: value }))}
              >
                <SelectTrigger>
                  <SelectValue placeholder="选择时区" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="UTC">UTC</SelectItem>
                  <SelectItem value="Asia/Shanghai">Asia/Shanghai</SelectItem>
                  <SelectItem value="America/New_York">America/New_York</SelectItem>
                  <SelectItem value="Europe/London">Europe/London</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-2">
              <Label htmlFor="hourly_rate">时薪 (¥)</Label>
              <Input
                id="hourly_rate"
                type="number"
                min="0"
                step="0.01"
                value={timeConfig.hourly_rate}
                onChange={(e) => setTimeConfig(prev => ({ ...prev, hourly_rate: parseFloat(e.target.value) || 0 }))}
                placeholder="请输入时薪"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="weekly_capacity">每周工作时长 (小时)</Label>
              <Input
                id="weekly_capacity"
                type="number"
                min="1"
                max="168"
                value={timeConfig.weekly_capacity}
                onChange={(e) => setTimeConfig(prev => ({ ...prev, weekly_capacity: parseInt(e.target.value) || 40 }))}
                placeholder="请输入每周工作时长"
              />
            </div>

            <Separator />

            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>自动启动计时器</Label>
                  <p className="text-sm text-muted-foreground">
                    登录时自动启动上次的计时器
                  </p>
                </div>
                <Switch
                  checked={timeConfig.auto_start_timer}
                  onCheckedChange={(checked) => setTimeConfig(prev => ({ ...prev, auto_start_timer: checked }))}
                />
              </div>

              <div className="flex items-center justify-between">
                <div className="space-y-0.5">
                  <Label>提醒功能</Label>
                  <p className="text-sm text-muted-foreground">
                    定期提醒记录时间
                  </p>
                </div>
                <Switch
                  checked={timeConfig.reminder_enabled}
                  onCheckedChange={(checked) => setTimeConfig(prev => ({ ...prev, reminder_enabled: checked }))}
                />
              </div>

              {timeConfig.reminder_enabled && (
                <div className="space-y-2">
                  <Label htmlFor="reminder_interval">提醒间隔 (分钟)</Label>
                  <Select
                    value={timeConfig.reminder_interval.toString()}
                    onValueChange={(value) => setTimeConfig(prev => ({ ...prev, reminder_interval: parseInt(value) }))}
                  >
                    <SelectTrigger>
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="15">15分钟</SelectItem>
                      <SelectItem value="30">30分钟</SelectItem>
                      <SelectItem value="60">1小时</SelectItem>
                      <SelectItem value="120">2小时</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              )}
            </div>

            <Button 
              onClick={handleSaveTimeSettings} 
              disabled={updating}
              className="w-full"
            >
              {updating ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  保存中...
                </>
              ) : (
                <>
                  <Save className="mr-2 h-4 w-4" />
                  保存时间设置
                </>
              )}
            </Button>
          </CardContent>
        </Card>

        {/* 权限信息 */}
        <Card className="md:col-span-2">
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Shield className="h-5 w-5" />
              权限信息
            </CardTitle>
            <CardDescription>
              查看您当前的角色和权限
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label>当前角色</Label>
              <div>
                <Badge variant="secondary" className="text-sm">
                  {user?.role ? getRoleDisplayName(user.role) : '未设置'}
                </Badge>
              </div>
            </div>

            <div className="space-y-2">
              <Label>权限列表</Label>
              <div className="flex flex-wrap gap-2">
                {user?.permissions && user.permissions.length > 0 ? (
                  user.permissions.map((permission) => (
                    <Badge key={permission} variant="outline" className="text-xs">
                      {getPermissionDisplayName(permission)}
                    </Badge>
                  ))
                ) : (
                  <p className="text-sm text-muted-foreground">暂无特殊权限</p>
                )}
              </div>
            </div>

            <div className="space-y-2">
              <Label>账户状态</Label>
              <div>
                <Badge variant={user?.is_active ? "default" : "destructive"}>
                  {user?.is_active ? '活跃' : '已停用'}
                </Badge>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}