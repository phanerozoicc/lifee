"use client"

import { useState } from "react"
import { MoreHorizontal, Building, Mail, Phone, MapPin, Clock, DollarSign } from "lucide-react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import { Avatar, AvatarFallback, AvatarImage } from "@/components/ui/avatar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { cn } from "@/lib/utils"
import type { Client } from "@/lib/time/types"

interface ClientCardProps {
  client: Client
  onEdit?: (client: Client) => void
  onDelete?: (client: Client) => void
  onView?: (client: Client) => void
  className?: string
}

export function ClientCard({ 
  client, 
  onEdit, 
  onDelete, 
  onView,
  className 
}: ClientCardProps) {
  const [isHovered, setIsHovered] = useState(false)

  // 获取客户状态颜色
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'active':
        return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300'
      case 'inactive':
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-300'
      case 'potential':
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300'
      default:
        return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-300'
    }
  }

  const getStatusLabel = (status: string) => {
    switch (status) {
      case 'active': return '活跃'
      case 'inactive': return '非活跃'
      case 'potential': return '潜在客户'
      default: return '未知'
    }
  }

  return (
    <Card 
      className={cn(
        "group transition-all duration-200 hover:shadow-lg cursor-pointer",
        "hover:border-primary/50",
        className
      )}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onClick={() => onView?.(client)}
    >
      <CardHeader className="pb-3">
        <div className="flex items-start justify-between">
          <div className="flex items-start gap-3 flex-1 min-w-0">
            <Avatar className="h-12 w-12 flex-shrink-0">
              <AvatarImage src={client.avatar} />
              <AvatarFallback className="bg-primary/10 text-primary font-semibold">
                {client.name.charAt(0)}
              </AvatarFallback>
            </Avatar>
            
            <div className="flex-1 min-w-0">
              <CardTitle className="text-lg font-semibold truncate">
                {client.name}
              </CardTitle>
              {client.company && (
                <div className="flex items-center gap-1 mt-1">
                  <Building className="h-3 w-3 text-muted-foreground" />
                  <CardDescription className="truncate">
                    {client.company}
                  </CardDescription>
                </div>
              )}
            </div>
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
                onView?.(client)
              }}>
                查看详情
              </DropdownMenuItem>
              <DropdownMenuItem onClick={(e) => {
                e.stopPropagation()
                onEdit?.(client)
              }}>
                编辑客户
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem 
                className="text-destructive"
                onClick={(e) => {
                  e.stopPropagation()
                  onDelete?.(client)
                }}
              >
                删除客户
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
        
        <div className="flex items-center gap-2 mt-2">
          <Badge className={getStatusColor(client.status || 'active')}>
            {getStatusLabel(client.status || 'active')}
          </Badge>
        </div>
      </CardHeader>
      
      <CardContent className="space-y-4">
        {/* 联系信息 */}
        <div className="space-y-2">
          {client.email && (
            <div className="flex items-center gap-2 text-sm">
              <Mail className="h-4 w-4 text-muted-foreground flex-shrink-0" />
              <span className="truncate">{client.email}</span>
            </div>
          )}
          {client.phone && (
            <div className="flex items-center gap-2 text-sm">
              <Phone className="h-4 w-4 text-muted-foreground flex-shrink-0" />
              <span>{client.phone}</span>
            </div>
          )}
          {client.address && (
            <div className="flex items-start gap-2 text-sm">
              <MapPin className="h-4 w-4 text-muted-foreground flex-shrink-0 mt-0.5" />
              <span className="line-clamp-2">{client.address}</span>
            </div>
          )}
        </div>

        {/* 项目统计 */}
        <div className="grid grid-cols-2 gap-4 pt-2 border-t">
          <div className="text-center">
            <div className="flex items-center justify-center gap-1 text-muted-foreground mb-1">
              <Clock className="h-3 w-3" />
              <span className="text-xs">项目数</span>
            </div>
            <div className="text-lg font-semibold">{client.projectCount || 0}</div>
          </div>
          <div className="text-center">
            <div className="flex items-center justify-center gap-1 text-muted-foreground mb-1">
              <DollarSign className="h-3 w-3" />
              <span className="text-xs">总收入</span>
            </div>
            <div className="text-lg font-semibold">
              ¥{client.totalRevenue?.toLocaleString() || 0}
            </div>
          </div>
        </div>

        {/* 备注 */}
        {client.notes && (
          <div className="pt-2 border-t">
            <p className="text-sm text-muted-foreground line-clamp-2">
              {client.notes}
            </p>
          </div>
        )}

        {/* 最后联系时间 */}
        {client.lastContact && (
          <div className="text-xs text-muted-foreground">
            最后联系: {new Date(client.lastContact).toLocaleDateString('zh-CN')}
          </div>
        )}
      </CardContent>
    </Card>
  )
}