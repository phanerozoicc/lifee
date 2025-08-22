"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select"
import { Switch } from "@/components/ui/switch"
import { Badge } from "@/components/ui/badge"
import { X } from "lucide-react"
import { Project, ProjectStatus, BillingType } from "@/lib/time/types"
import { useProjectStore } from "@/lib/time/store"
import { useClientStore } from "@/lib/time/store"
import { useTagStore } from "@/lib/time/store"
import { toast } from "sonner"

interface ProjectDialogProps {
  open: boolean
  onOpenChange: (open: boolean) => void
  project?: Project | null
}

interface ProjectFormData {
  name: string
  description: string
  clientId: string
  status: ProjectStatus
  billingType: BillingType
  hourlyRate: number
  budget: number
  estimatedHours: number
  isPublic: boolean
  tags: string[]
}

const initialFormData: ProjectFormData = {
  name: "",
  description: "",
  clientId: "",
  status: ProjectStatus.ACTIVE,
  billingType: BillingType.HOURLY,
  hourlyRate: 0,
  budget: 0,
  estimatedHours: 0,
  isPublic: false,
  tags: []
}

export function ProjectDialog({ open, onOpenChange, project }: ProjectDialogProps) {
  const [formData, setFormData] = useState<ProjectFormData>(initialFormData)
  const [loading, setLoading] = useState(false)
  const [newTag, setNewTag] = useState("")
  
  const { createProject, updateProject } = useProjectStore()
  const { clients, fetchClients } = useClientStore()
  const { tags, fetchTags } = useTagStore()

  const isEditing = !!project

  useEffect(() => {
    if (open) {
      fetchClients()
      fetchTags()
      
      if (project) {
        setFormData({
          name: project.name,
          description: project.description || "",
          clientId: project.clientId || "",
          status: project.status,
          billingType: project.billingType,
          hourlyRate: project.hourlyRate || 0,
          budget: project.budget || 0,
          estimatedHours: project.estimatedHours || 0,
          isPublic: project.isPublic,
          tags: project.tags?.map(tag => tag.id) || []
        })
      } else {
        setFormData(initialFormData)
      }
    }
  }, [open, project, fetchClients, fetchTags])

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!formData.name.trim()) {
      toast.error("请输入项目名称")
      return
    }

    setLoading(true)
    try {
      const projectData = {
        ...formData,
        hourlyRate: formData.hourlyRate || undefined,
        budget: formData.budget || undefined,
        estimatedHours: formData.estimatedHours || undefined,
        clientId: formData.clientId || undefined
      }

      if (isEditing && project) {
        await updateProject(project.id, projectData)
        toast.success("项目更新成功")
      } else {
        await createProject(projectData)
        toast.success("项目创建成功")
      }
      
      onOpenChange(false)
    } catch {
      toast.error(isEditing ? "项目更新失败" : "项目创建失败")
    } finally {
      setLoading(false)
    }
  }

  const handleAddTag = () => {
    if (newTag.trim() && !formData.tags.includes(newTag.trim())) {
      setFormData(prev => ({
        ...prev,
        tags: [...prev.tags, newTag.trim()]
      }))
      setNewTag("")
    }
  }

  const handleRemoveTag = (tagToRemove: string) => {
    setFormData(prev => ({
      ...prev,
      tags: prev.tags.filter(tag => tag !== tagToRemove)
    }))
  }

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') {
      e.preventDefault()
      handleAddTag()
    }
  }

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-[600px] max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle>{isEditing ? "编辑项目" : "新建项目"}</DialogTitle>
          <DialogDescription>
            {isEditing ? "修改项目信息" : "创建一个新的项目来组织您的工作"}
          </DialogDescription>
        </DialogHeader>
        
        <form onSubmit={handleSubmit} className="space-y-6">
          {/* 基本信息 */}
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="name">项目名称 *</Label>
                <Input
                  id="name"
                  value={formData.name}
                  onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                  placeholder="输入项目名称"
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="client">客户</Label>
                <Select value={formData.clientId} onValueChange={(value) => setFormData(prev => ({ ...prev, clientId: value }))}>
                  <SelectTrigger>
                    <SelectValue placeholder="选择客户" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="">无客户</SelectItem>
                    {clients.map((client) => (
                      <SelectItem key={client.id} value={client.id}>
                        {client.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="description">项目描述</Label>
              <Textarea
                id="description"
                value={formData.description}
                onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
                placeholder="输入项目描述"
                rows={3}
              />
            </div>
          </div>

          {/* 状态和计费 */}
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div className="space-y-2">
                <Label htmlFor="status">项目状态</Label>
                <Select value={formData.status} onValueChange={(value: ProjectStatus) => setFormData(prev => ({ ...prev, status: value }))}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={ProjectStatus.ACTIVE}>进行中</SelectItem>
                    <SelectItem value={ProjectStatus.COMPLETED}>已完成</SelectItem>
                    <SelectItem value={ProjectStatus.ON_HOLD}>暂停</SelectItem>
                    <SelectItem value={ProjectStatus.CANCELLED}>已取消</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="space-y-2">
                <Label htmlFor="billingType">计费方式</Label>
                <Select value={formData.billingType} onValueChange={(value: BillingType) => setFormData(prev => ({ ...prev, billingType: value }))}>
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value={BillingType.HOURLY}>按小时</SelectItem>
                    <SelectItem value={BillingType.FIXED}>固定价格</SelectItem>
                    <SelectItem value={BillingType.NON_BILLABLE}>不计费</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            {formData.billingType === BillingType.HOURLY && (
              <div className="grid grid-cols-3 gap-4">
                <div className="space-y-2">
                  <Label htmlFor="hourlyRate">时薪 (¥)</Label>
                  <Input
                    id="hourlyRate"
                    type="number"
                    min="0"
                    step="0.01"
                    value={formData.hourlyRate}
                    onChange={(e) => setFormData(prev => ({ ...prev, hourlyRate: parseFloat(e.target.value) || 0 }))}
                    placeholder="0.00"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="budget">预算 (¥)</Label>
                  <Input
                    id="budget"
                    type="number"
                    min="0"
                    step="0.01"
                    value={formData.budget}
                    onChange={(e) => setFormData(prev => ({ ...prev, budget: parseFloat(e.target.value) || 0 }))}
                    placeholder="0.00"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="estimatedHours">预估时间 (小时)</Label>
                  <Input
                    id="estimatedHours"
                    type="number"
                    min="0"
                    step="0.5"
                    value={formData.estimatedHours}
                    onChange={(e) => setFormData(prev => ({ ...prev, estimatedHours: parseFloat(e.target.value) || 0 }))}
                    placeholder="0"
                  />
                </div>
              </div>
            )}
          </div>

          {/* 标签 */}
          <div className="space-y-4">
            <div className="space-y-2">
              <Label>项目标签</Label>
              <div className="flex gap-2">
                <Input
                  value={newTag}
                  onChange={(e) => setNewTag(e.target.value)}
                  onKeyPress={handleKeyPress}
                  placeholder="添加标签"
                  className="flex-1"
                />
                <Button type="button" variant="outline" onClick={handleAddTag}>
                  添加
                </Button>
              </div>
              {formData.tags.length > 0 && (
                <div className="flex flex-wrap gap-2 mt-2">
                  {formData.tags.map((tagId) => {
                    const tag = tags.find(t => t.id === tagId)
                    return (
                      <Badge key={tagId} variant="secondary" className="flex items-center gap-1">
                        {tag?.name || tagId}
                        <X 
                          className="h-3 w-3 cursor-pointer" 
                          onClick={() => handleRemoveTag(tagId)}
                        />
                      </Badge>
                    )
                  })}
                </div>
              )}
            </div>
          </div>

          {/* 其他设置 */}
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <div className="space-y-0.5">
                <Label htmlFor="isPublic">公开项目</Label>
                <div className="text-sm text-muted-foreground">
                  允许团队成员查看此项目
                </div>
              </div>
              <Switch
                id="isPublic"
                checked={formData.isPublic}
                onCheckedChange={(checked) => setFormData(prev => ({ ...prev, isPublic: checked }))}
              />
            </div>
          </div>

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