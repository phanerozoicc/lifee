"use client"

import { useState, useEffect } from "react"
import { DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import { useClientStore } from "@/lib/time/store"
import { Client } from "@/lib/time/types"
import { Building2, Mail, Phone, MapPin, FileText } from "lucide-react"

interface ClientDialogProps {
  client?: Client | null
  onClose: () => void
}

export function ClientDialog({ client, onClose }: ClientDialogProps) {
  const { createClient, updateClient, loading } = useClientStore()
  
  const [formData, setFormData] = useState({
    name: "",
    company: "",
    email: "",
    phone: "",
    address: "",
    notes: ""
  })
  
  const [errors, setErrors] = useState<Record<string, string>>({})

  useEffect(() => {
    if (client) {
      setFormData({
        name: client.name || "",
        company: client.company || "",
        email: client.email || "",
        phone: client.phone || "",
        address: client.address || "",
        notes: client.notes || ""
      })
    } else {
      setFormData({
        name: "",
        company: "",
        email: "",
        phone: "",
        address: "",
        notes: ""
      })
    }
    setErrors({})
  }, [client])

  const validateForm = () => {
    const newErrors: Record<string, string> = {}
    
    if (!formData.name.trim()) {
      newErrors.name = "客户名称不能为空"
    }
    
    if (formData.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = "请输入有效的邮箱地址"
    }
    
    if (formData.phone && !/^[\d\s\-\+\(\)]+$/.test(formData.phone)) {
      newErrors.phone = "请输入有效的电话号码"
    }
    
    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    
    if (!validateForm()) {
      return
    }

    try {
      const clientData = {
        name: formData.name.trim(),
        company: formData.company.trim() || undefined,
        email: formData.email.trim() || undefined,
        phone: formData.phone.trim() || undefined,
        address: formData.address.trim() || undefined,
        notes: formData.notes.trim() || undefined
      }

      if (client) {
        await updateClient(client.id, clientData)
      } else {
        await createClient(clientData)
      }
      
      onClose()
    } catch (error) {
      console.error('Failed to save client:', error)
    }
  }

  const handleInputChange = (field: string, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }))
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: "" }))
    }
  }

  return (
    <DialogContent className="sm:max-w-[500px]">
      <DialogHeader>
        <DialogTitle className="flex items-center gap-2">
          <Building2 className="h-5 w-5" />
          {client ? "编辑客户" : "添加客户"}
        </DialogTitle>
        <DialogDescription>
          {client ? "修改客户信息" : "添加新的客户信息"}
        </DialogDescription>
      </DialogHeader>
      
      <form onSubmit={handleSubmit} className="space-y-4">
        {/* 基本信息 */}
        <div className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="name" className="flex items-center gap-2">
              <Building2 className="h-4 w-4" />
              客户名称 *
            </Label>
            <Input
              id="name"
              value={formData.name}
              onChange={(e) => handleInputChange("name", e.target.value)}
              placeholder="输入客户名称"
              className={errors.name ? "border-destructive" : ""}
            />
            {errors.name && (
              <p className="text-sm text-destructive">{errors.name}</p>
            )}
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="company" className="flex items-center gap-2">
              <Building2 className="h-4 w-4" />
              公司名称
            </Label>
            <Input
              id="company"
              value={formData.company}
              onChange={(e) => handleInputChange("company", e.target.value)}
              placeholder="输入公司名称"
            />
          </div>
        </div>
        
        {/* 联系信息 */}
        <div className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="email" className="flex items-center gap-2">
              <Mail className="h-4 w-4" />
              邮箱地址
            </Label>
            <Input
              id="email"
              type="email"
              value={formData.email}
              onChange={(e) => handleInputChange("email", e.target.value)}
              placeholder="输入邮箱地址"
              className={errors.email ? "border-destructive" : ""}
            />
            {errors.email && (
              <p className="text-sm text-destructive">{errors.email}</p>
            )}
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="phone" className="flex items-center gap-2">
              <Phone className="h-4 w-4" />
              电话号码
            </Label>
            <Input
              id="phone"
              value={formData.phone}
              onChange={(e) => handleInputChange("phone", e.target.value)}
              placeholder="输入电话号码"
              className={errors.phone ? "border-destructive" : ""}
            />
            {errors.phone && (
              <p className="text-sm text-destructive">{errors.phone}</p>
            )}
          </div>
          
          <div className="space-y-2">
            <Label htmlFor="address" className="flex items-center gap-2">
              <MapPin className="h-4 w-4" />
              地址
            </Label>
            <Input
              id="address"
              value={formData.address}
              onChange={(e) => handleInputChange("address", e.target.value)}
              placeholder="输入地址"
            />
          </div>
        </div>
        
        {/* 备注 */}
        <div className="space-y-2">
          <Label htmlFor="notes" className="flex items-center gap-2">
            <FileText className="h-4 w-4" />
            备注
          </Label>
          <Textarea
            id="notes"
            value={formData.notes}
            onChange={(e) => handleInputChange("notes", e.target.value)}
            placeholder="输入备注信息"
            rows={3}
          />
        </div>
        
        <DialogFooter>
          <Button type="button" variant="outline" onClick={onClose}>
            取消
          </Button>
          <Button type="submit" disabled={loading}>
            {loading ? "保存中..." : (client ? "更新" : "创建")}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  )
}