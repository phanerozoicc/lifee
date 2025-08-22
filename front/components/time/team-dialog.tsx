"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"

import { Avatar, AvatarFallback } from "@/components/ui/avatar"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { Plus, Trash2 } from "lucide-react"
import { Team, TeamRole } from "@/lib/time/types"
import { useTeamStore } from "@/lib/time/store"
import { toast } from "sonner"


interface TeamDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  team?: Team | null
}

interface TeamFormData {
  name: string
  description: string
  members: {
    email: string
    role: TeamRole
  }[]
}

const initialFormData: TeamFormData = {
  name: "",
  description: "",
  members: []
}



export function TeamDialog({ open, onOpenChange, team }: TeamDialogProps) {
  const [formData, setFormData] = useState<TeamFormData>(initialFormData)
  const [loading, setLoading] = useState(false)
  const [newMemberEmail, setNewMemberEmail] = useState("")
  const [newMemberRole, setNewMemberRole] = useState<TeamRole>(TeamRole.MEMBER)
  
  const { createTeam, updateTeam } = useTeamStore()

  const isEditing = !!team

  useEffect(() => {
    if (open) {
      if (team) {
        setFormData({
          name: team.name,
          description: team.description || "",
          members: team.members?.map(member => ({
            email: member.user.email,
            role: member.role
          })) || []
        })
      } else {
        setFormData(initialFormData)
      }
    }
  }, [open, team])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!formData.name.trim()) {
      toast.error("请输入团队名称")
      return
    }

    setLoading(true)
    try {
      if (isEditing && team) {
        await updateTeam(team.id, formData)
        toast.success("团队更新成功")
      } else {
        await createTeam(formData)
        toast.success("团队创建成功")
      }
      
      onOpenChange(false)
    } catch {
      toast.error(isEditing ? "团队更新失败" : "团队创建失败")
    } finally {
      setLoading(false)
    }
  }

  const handleAddMember = () => {
    if (!newMemberEmail.trim()) {
      toast.error("请输入邮箱地址")
      return
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(newMemberEmail)) {
      toast.error("请输入有效的邮箱地址")
      return
    }

    if (formData.members.some(member => member.email === newMemberEmail)) {
      toast.error("该成员已存在")
      return
    }

    setFormData(prev => ({
      ...prev,
      members: [...prev.members, { email: newMemberEmail, role: newMemberRole }]
    }))
    setNewMemberEmail("")
    setNewMemberRole(TeamRole.MEMBER)
  }

  const handleRemoveMember = (emailToRemove: string) => {
    setFormData(prev => ({
      ...prev,
      members: prev.members.filter(member => member.email !== emailToRemove)
    }))
  }

  const handleUpdateMemberRole = (email: string, newRole: TeamRole) => {
    setFormData(prev => ({
      ...prev,
      members: prev.members.map(member => 
        member.email === email ? { ...member, role: newRole } : member
      )
    }))
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      handleAddMember()
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[700px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEditing ? "编辑团队" : "新建团队"}</DialogTitle>
          <DialogDescription>
            {isEditing ? "修改团队信息和成员" : "创建一个新的团队来协作管理项目"}
          </DialogDescription>
        </DialogHeader>
        
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* 基本信息 */}
          <div className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="name">团队名称 *</Label>
              <Input
                id="name"
                value={formData.name}
                onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                placeholder="输入团队名称"
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">团队描述</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                placeholder="输入团队描述"
                rows={3}
              />
            </div>
          </div>

          {/* 添加成员 */}
          <div className="space-y-4">
            <Label>团队成员</Label>
            <div className="flex gap-2">
              <Input
                value={newMemberEmail}
                onChange={(e) => setNewMemberEmail(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="输入成员邮箱"
                className="flex-1"
                type="email"
              />
              <Select value={newMemberRole} onValueChange={(value: TeamRole) => setNewMemberRole(value)}>
                <SelectTrigger className="w-32">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value={TeamRole.MEMBER}>成员</SelectItem>
                  <SelectItem value={TeamRole.MANAGER}>经理</SelectItem>
                  <SelectItem value={TeamRole.ADMIN}>管理员</SelectItem>
                  <SelectItem value={TeamRole.VIEWER}>查看者</SelectItem>
                </SelectContent>
              </Select>
              <Button type="button" variant="outline" onClick={handleAddMember}>
                <Plus className="h-4 w-4" />
              </Button>
            </div>
          </div>

          {/* 成员列表 */}
          {formData.members.length > 0 && (
            <div className="space-y-4">
              <div className="border rounded-lg">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>成员</TableHead>
                      <TableHead>角色</TableHead>
                      <TableHead className="w-12"></TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {formData.members.map((member, index) => (
                      <TableRow key={index}>
                        <TableCell>
                          <div className="flex items-center gap-3">
                            <Avatar className="h-8 w-8">
                              <AvatarFallback className="text-xs">
                                {member.email.charAt(0).toUpperCase()}
                              </AvatarFallback>
                            </Avatar>
                            <div>
                              <div className="font-medium">{member.email}</div>
                            </div>
                          </div>
                        </TableCell>
                        <TableCell>
                          <Select 
                            value={member.role} 
                            onValueChange={(value: TeamRole) => handleUpdateMemberRole(member.email, value)}
                          >
                            <SelectTrigger className="w-32">
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value={TeamRole.VIEWER}>查看者</SelectItem>
                              <SelectItem value={TeamRole.MEMBER}>成员</SelectItem>
                              <SelectItem value={TeamRole.MANAGER}>经理</SelectItem>
                              <SelectItem value={TeamRole.ADMIN}>管理员</SelectItem>
                              {!isEditing && (
                                <SelectItem value={TeamRole.OWNER}>所有者</SelectItem>
                              )}
                            </SelectContent>
                          </Select>
                        </TableCell>
                        <TableCell>
                          <Button 
                            type="button" 
                            variant="ghost" 
                            size="sm"
                            onClick={() => handleRemoveMember(member.email)}
                            className="text-destructive hover:text-destructive"
                          >
                            <Trash2 className="h-4 w-4" />
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            </div>
          )}

          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
              取消
            </Button>
            <Button type="submit" disabled={loading}>
              {loading ? "保存中..." : (isEditing ? "更新" : "创建")}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  )
}