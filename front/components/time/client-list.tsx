"use client"

import { useState, useEffect } from "react"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table"
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "@/components/ui/dropdown-menu"
import {
  Dialog,
  DialogTrigger,
} from "@/components/ui/dialog"
import { AlertDialog, AlertDialogAction, AlertDialogCancel, AlertDialogContent, AlertDialogDescription, AlertDialogFooter, AlertDialogHeader, AlertDialogTitle, AlertDialogTrigger } from "@/components/ui/alert-dialog"
import { Search, Plus, MoreHorizontal, Edit, Trash2, Building2, Mail, Phone, MapPin } from "lucide-react"
import { useClientStore } from "@/lib/time/store"
import { Client } from "@/lib/time/types"
import { ClientDialog } from "./client-dialog"
import { cn } from "@/lib/utils"

interface ClientListProps {
  className?: string
}

export function ClientList({ className }: ClientListProps) {
  const { 
    clients, 
    loading, 
    searchTerm, 
    setSearchTerm, 
    fetchClients, 
    deleteClient 
  } = useClientStore()
  
  const [selectedClient, setSelectedClient] = useState<Client | null>(null)
  const [showDialog, setShowDialog] = useState(false)
  // const [deleteClientId, setDeleteClientId] = useState<string | null>(null)

  useEffect(() => {
    fetchClients()
  }, [fetchClients])

  const handleEdit = (client: Client) => {
    setSelectedClient(client)
    setShowDialog(true)
  }

  const handleDelete = async (clientId: string) => {
    await deleteClient(clientId)
    setDeleteClientId(null)
  }

  const handleDialogClose = () => {
    setShowDialog(false)
    setSelectedClient(null)
  }

  const filteredClients = clients.filter(client =>
    client.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
    client.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    client.company?.toLowerCase().includes(searchTerm.toLowerCase())
  )

  const getClientStats = () => {
    // 这里应该从项目数据中计算统计信息
    // 暂时使用模拟数据
    return {
      activeProjects: Math.floor(Math.random() * 5) + 1,
      totalHours: Math.floor(Math.random() * 500) + 50,
      totalEarnings: Math.floor(Math.random() * 50000) + 10000
    }
  }

  if (loading) {
    return (
      <Card className={cn("animate-pulse", className)}>
        <CardHeader>
          <div className="h-6 bg-muted rounded w-1/4"></div>
          <div className="h-4 bg-muted rounded w-1/2"></div>
        </CardHeader>
        <CardContent>
          <div className="space-y-4">
            <div className="h-10 bg-muted rounded"></div>
            <div className="space-y-3">
              {[...Array(5)].map((_, i) => (
                <div key={i} className="h-16 bg-muted rounded"></div>
              ))}
            </div>
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className={className}>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="flex items-center gap-2">
              <Building2 className="h-5 w-5" />
              客户管理
            </CardTitle>
            <CardDescription>
              管理您的客户信息和联系方式
            </CardDescription>
          </div>
          <Dialog open={showDialog} onOpenChange={setShowDialog}>
            <DialogTrigger asChild>
              <Button onClick={() => setSelectedClient(null)}>
                <Plus className="h-4 w-4 mr-2" />
                添加客户
              </Button>
            </DialogTrigger>
            <ClientDialog 
              client={selectedClient} 
              onClose={handleDialogClose}
            />
          </Dialog>
        </div>
      </CardHeader>
      
      <CardContent className="space-y-4">
        {/* 搜索栏 */}
        <div className="flex items-center gap-4">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
            <Input
              placeholder="搜索客户名称、邮箱或公司..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="pl-10"
            />
          </div>
        </div>

        {/* 客户列表 */}
        {filteredClients.length === 0 ? (
          <div className="text-center py-8">
            <Building2 className="h-12 w-12 text-muted-foreground mx-auto mb-4" />
            <h3 className="text-lg font-medium mb-2">暂无客户</h3>
            <p className="text-muted-foreground mb-4">
              {searchTerm ? '没有找到匹配的客户' : '开始添加您的第一个客户'}
            </p>
            {!searchTerm && (
              <Button onClick={() => setShowDialog(true)}>
                <Plus className="h-4 w-4 mr-2" />
                添加客户
              </Button>
            )}
          </div>
        ) : (
          <div className="border rounded-lg">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>客户信息</TableHead>
                  <TableHead>联系方式</TableHead>
                  <TableHead>项目统计</TableHead>
                  <TableHead>状态</TableHead>
                  <TableHead className="w-[50px]"></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredClients.map((client) => {
                  const stats = getClientStats(client)
                  return (
                    <TableRow key={client.id}>
                      <TableCell>
                        <div>
                          <div className="font-medium">{client.name}</div>
                          {client.company && (
                            <div className="text-sm text-muted-foreground">
                              {client.company}
                            </div>
                          )}
                        </div>
                      </TableCell>
                      
                      <TableCell>
                        <div className="space-y-1">
                          {client.email && (
                            <div className="flex items-center gap-1 text-sm">
                              <Mail className="h-3 w-3 text-muted-foreground" />
                              <span>{client.email}</span>
                            </div>
                          )}
                          {client.phone && (
                            <div className="flex items-center gap-1 text-sm">
                              <Phone className="h-3 w-3 text-muted-foreground" />
                              <span>{client.phone}</span>
                            </div>
                          )}
                          {client.address && (
                            <div className="flex items-center gap-1 text-sm">
                              <MapPin className="h-3 w-3 text-muted-foreground" />
                              <span className="truncate max-w-[200px]">{client.address}</span>
                            </div>
                          )}
                        </div>
                      </TableCell>
                      
                      <TableCell>
                        <div className="space-y-1 text-sm">
                          <div>{stats.activeProjects} 个活跃项目</div>
                          <div className="text-muted-foreground">
                            {stats.totalHours}h / ¥{stats.totalEarnings.toLocaleString()}
                          </div>
                        </div>
                      </TableCell>
                      
                      <TableCell>
                        <Badge variant={stats.activeProjects > 0 ? "default" : "secondary"}>
                          {stats.activeProjects > 0 ? "活跃" : "非活跃"}
                        </Badge>
                      </TableCell>
                      
                      <TableCell>
                        <DropdownMenu>
                          <DropdownMenuTrigger asChild>
                            <Button variant="ghost" size="sm">
                              <MoreHorizontal className="h-4 w-4" />
                            </Button>
                          </DropdownMenuTrigger>
                          <DropdownMenuContent align="end">
                            <DropdownMenuItem onClick={() => handleEdit(client)}>
                              <Edit className="h-4 w-4 mr-2" />
                              编辑
                            </DropdownMenuItem>
                            <AlertDialog>
                              <AlertDialogTrigger asChild>
                                <DropdownMenuItem 
                                  onSelect={(e) => e.preventDefault()}
                                  className="text-destructive"
                                >
                                  <Trash2 className="h-4 w-4 mr-2" />
                                  删除
                                </DropdownMenuItem>
                              </AlertDialogTrigger>
                              <AlertDialogContent>
                                <AlertDialogHeader>
                                  <AlertDialogTitle>确认删除</AlertDialogTitle>
                                  <AlertDialogDescription>
                                    确定要删除客户 &quot;{client.name}&quot; 吗？此操作无法撤销。
                                  </AlertDialogDescription>
                                </AlertDialogHeader>
                                <AlertDialogFooter>
                                  <AlertDialogCancel>取消</AlertDialogCancel>
                                  <AlertDialogAction 
                                    onClick={() => handleDelete(client.id)}
                                    className="bg-destructive text-destructive-foreground hover:bg-destructive/90"
                                  >
                                    删除
                                  </AlertDialogAction>
                                </AlertDialogFooter>
                              </AlertDialogContent>
                            </AlertDialog>
                          </DropdownMenuContent>
                        </DropdownMenu>
                      </TableCell>
                    </TableRow>
                  )
                })}
              </TableBody>
            </Table>
          </div>
        )}
      </CardContent>
    </Card>
  )
}