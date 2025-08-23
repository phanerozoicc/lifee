"use client"

import { LayoutApp } from "@/app/components/layout/layout-app"
import { MessagesProvider } from "@/lib/chat-store/messages/provider"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Badge } from "@/components/ui/badge"
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { 
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
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
import { 
  FolderPlus, 
  Upload, 
  Search, 
  FileText, 
  Folder,
  MoreHorizontal,
  Download,
  Plus,
  Edit,
  Trash2,
  Eye,
  Share,
  Star,
  Clock,
  Filter,
  Grid,
  List,
  ChevronRight,
  ChevronDown,
  BookOpen,
  AlertCircle
} from "lucide-react"
import { useState, useMemo, useEffect } from "react"
import { useUser } from "@/lib/user/provider"
import { apiService } from "@/lib/services/api-service"
import type { KnowledgeBase, Document } from "@/lib/types/api"
import { toast } from "sonner"

export default function KnowledgePage() {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedKnowledge, setSelectedKnowledge] = useState<string | null>(null)
  const [selectedFile, setSelectedFile] = useState<string | null>(null)
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('list')
  const [showCreateDialog, setShowCreateDialog] = useState(false)
  const [expandedFolders, setExpandedFolders] = useState<Set<string>>(new Set(['1', '2']))
  const [filterType, setFilterType] = useState<'all' | 'recent' | 'starred'>('all')
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [knowledgeBases, setKnowledgeBases] = useState<KnowledgeBase[]>([])
  const [documents, setDocuments] = useState<Document[]>([])
  const [newKnowledgeBaseName, setNewKnowledgeBaseName] = useState('')
  const [newKnowledgeBaseDescription, setNewKnowledgeBaseDescription] = useState('')
  const { user } = useUser()

  // 加载数据
  const loadData = async () => {
    try {
      setIsLoading(true)
      setError(null)
      
      const [kbResponse] = await Promise.all([
        apiService.getUserKnowledgeBases()
      ])
      
      if (kbResponse.data) {
        setKnowledgeBases(kbResponse.data)
      }
      
    } catch (err) {
      console.error('Failed to load knowledge bases:', err)
      setError('加载知识库失败，请稍后重试')
      toast.error('加载知识库失败')
    } finally {
      setIsLoading(false)
    }
  }
  
  // 创建知识库
  const handleCreateKnowledgeBase = async () => {
    if (!newKnowledgeBaseName.trim()) {
      toast.error('请输入知识库名称')
      return
    }
    
    try {
      const response = await apiService.createKnowledgeBase({
        name: newKnowledgeBaseName,
        description: newKnowledgeBaseDescription,
        isPublic: false
      })
      
      if (response.data) {
        setKnowledgeBases(prev => [...prev, response.data!])
        setNewKnowledgeBaseName('')
        setNewKnowledgeBaseDescription('')
        setShowCreateDialog(false)
        toast.success('知识库创建成功')
      }
    } catch (error) {
      console.error('Failed to create knowledge base:', error)
      toast.error('创建知识库失败')
    }
  }
  
  // 删除知识库
  const handleDeleteKnowledgeBase = async (id: string) => {
    try {
      await apiService.deleteKnowledgeBase(id)
      setKnowledgeBases(prev => prev.filter(kb => kb.id !== id))
      toast.success('知识库删除成功')
    } catch (error) {
      console.error('Failed to delete knowledge base:', error)
      toast.error('删除知识库失败')
    }
  }
  
  // 格式化日期
  const formatDate = (dateString: string): string => {
    return new Date(dateString).toLocaleDateString('zh-CN')
  }

  useEffect(() => {
    if (user) {
      loadData()
    }
  }, [user])
  
  // 过滤知识库
  const filteredKnowledgeBases = useMemo(() => {
    let filtered = knowledgeBases

    // 搜索过滤
    if (searchQuery) {
      filtered = filtered.filter(kb => 
        kb.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        (kb.description && kb.description.toLowerCase().includes(searchQuery.toLowerCase()))
      )
    }

    // 类型过滤
    switch (filterType) {
      case 'recent':
        filtered = filtered.sort((a, b) => new Date(b.updatedAt).getTime() - new Date(a.updatedAt).getTime())
        break
      default:
        break
    }

    return filtered
  }, [knowledgeBases, searchQuery, filterType])

  // 切换文件夹展开状态
  const toggleFolder = (folderId: string) => {
    const newExpanded = new Set(expandedFolders)
    if (newExpanded.has(folderId)) {
      newExpanded.delete(folderId)
    } else {
      newExpanded.add(folderId)
    }
    setExpandedFolders(newExpanded)
  }

  // 获取文件图标
  const getFileIcon = (fileType: string) => {
    switch (fileType) {
      case 'markdown':
        return <FileText className="h-4 w-4 text-blue-500" />
      case 'pdf':
        return <FileText className="h-4 w-4 text-red-500" />
      case 'figma':
        return <FileText className="h-4 w-4 text-purple-500" />
      case 'svg':
        return <FileText className="h-4 w-4 text-green-500" />
      default:
        return <FileText className="h-4 w-4 text-gray-500" />
    }
  }

  // 渲染文件树
  const renderFileTree = (items: any[], level = 0) => {
    return items.map((item) => (
      <div key={item.id} style={{ marginLeft: `${level * 16}px` }}>
        {item.type === 'folder' ? (
          <div>
            <div 
              className="flex items-center gap-2 p-2 rounded hover:bg-accent cursor-pointer"
              onClick={() => toggleFolder(item.id)}
            >
              {expandedFolders.has(item.id) ? (
                <ChevronDown className="h-4 w-4" />
              ) : (
                <ChevronRight className="h-4 w-4" />
              )}
              <Folder className="h-4 w-4 text-blue-500" />
              <span className="text-sm">{item.name}</span>
            </div>
            {expandedFolders.has(item.id) && item.children && (
              <div>
                {renderFileTree(item.children, level + 1)}
              </div>
            )}
          </div>
        ) : (
          <div 
            className={`flex items-center justify-between p-2 rounded hover:bg-accent cursor-pointer group ${
              selectedFile === item.id ? 'bg-accent' : ''
            }`}
            onClick={() => setSelectedFile(item.id)}
          >
            <div className="flex items-center gap-2">
              {getFileIcon(item.fileType)}
              <span className="text-sm">{item.name}</span>
              {item.isStarred && <Star className="h-3 w-3 text-yellow-500 fill-current" />}
            </div>
            <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100">
              <span className="text-xs text-muted-foreground">{item.size}</span>
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button variant="ghost" size="sm" className="h-6 w-6 p-0">
                    <MoreHorizontal className="h-3 w-3" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent>
                  <DropdownMenuItem>
                    <Eye className="h-4 w-4 mr-2" />
                    预览
                  </DropdownMenuItem>
                  <DropdownMenuItem>
                    <Edit className="h-4 w-4 mr-2" />
                    编辑
                  </DropdownMenuItem>
                  <DropdownMenuItem>
                    <Share className="h-4 w-4 mr-2" />
                    分享
                  </DropdownMenuItem>
                  <DropdownMenuItem>
                    <Download className="h-4 w-4 mr-2" />
                    下载
                  </DropdownMenuItem>
                  <DropdownMenuItem 
                      className="text-red-600"
                      onClick={(e) => {
                        e.stopPropagation()
                        handleDeleteKnowledgeBase(kb.id)
                      }}
                    >
                      <Trash2 className="h-4 w-4 mr-2" />
                      删除
                    </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          </div>
        )}
      </div>
    ))
  }

  // Mock data for file tree
  const fileTree = [
    {
      id: "1",
      name: "前端开发",
      type: "folder",
      children: [
        { 
          id: "1-1", 
          name: "React.md", 
          type: "file", 
          size: "45 KB", 
          lastModified: "2024-01-15",
          isStarred: true,
          fileType: "markdown"
        },
        { 
          id: "1-2", 
          name: "Vue.md", 
          type: "file", 
          size: "32 KB", 
          lastModified: "2024-01-14",
          isStarred: false,
          fileType: "markdown"
        },
        { 
          id: "1-3", 
          name: "TypeScript.md", 
          type: "file", 
          size: "67 KB", 
          lastModified: "2024-01-13",
          isStarred: true,
          fileType: "markdown"
        },
        {
          id: "1-4",
          name: "组件设计",
          type: "folder",
          children: [
            { 
              id: "1-4-1", 
              name: "Button组件.md", 
              type: "file", 
              size: "23 KB", 
              lastModified: "2024-01-12",
              isStarred: false,
              fileType: "markdown"
            }
          ]
        }
      ]
    },
    {
      id: "2",
      name: "后端开发",
      type: "folder",
      children: [
        { 
          id: "2-1", 
          name: "Spring Boot.md", 
          type: "file", 
          size: "89 KB", 
          lastModified: "2024-01-11",
          isStarred: true,
          fileType: "markdown"
        },
        { 
          id: "2-2", 
          name: "数据库设计.md", 
          type: "file", 
          size: "123 KB", 
          lastModified: "2024-01-10",
          isStarred: false,
          fileType: "markdown"
        },
        { 
          id: "2-3", 
          name: "API文档.pdf", 
          type: "file", 
          size: "2.1 MB", 
          lastModified: "2024-01-09",
          isStarred: true,
          fileType: "pdf"
        }
      ]
    },
    {
      id: "3",
      name: "设计资源",
      type: "folder",
      children: [
        { 
          id: "3-1", 
          name: "UI设计规范.figma", 
          type: "file", 
          size: "15.6 MB", 
          lastModified: "2024-01-08",
          isStarred: true,
          fileType: "figma"
        },
        { 
          id: "3-2", 
          name: "图标库.svg", 
          type: "file", 
          size: "856 KB", 
          lastModified: "2024-01-07",
          isStarred: false,
          fileType: "svg"
        }
      ]
    }
  ]

  return (
    <MessagesProvider>
      <LayoutApp>
        <div className="flex h-full pt-4">
      {/* Left Panel - Knowledge Base List */}
      <div className="w-80 border-r bg-background p-4">
        <div className="mb-4">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold">知识库</h2>
            <Dialog open={showCreateDialog} onOpenChange={setShowCreateDialog}>
              <DialogTrigger asChild>
                <Button size="sm" className="gap-2">
                  <FolderPlus className="h-4 w-4" />
                  新建
                </Button>
              </DialogTrigger>
            </Dialog>
          </div>
          
          <div className="relative mb-4">
            <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
            <Input
              placeholder="搜索知识库..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="pl-9"
            />
          </div>
        </div>

        <div className="space-y-2">
          {isLoading ? (
            [...Array(3)].map((_, i) => (
              <Card key={i}>
                <CardHeader className="pb-2">
                  <div className="animate-pulse space-y-2">
                    <div className="h-4 bg-gray-200 rounded w-3/4"></div>
                    <div className="h-3 bg-gray-200 rounded w-1/2"></div>
                  </div>
                </CardHeader>
              </Card>
            ))
          ) : filteredKnowledgeBases.length === 0 ? (
            <Card>
              <CardContent className="p-8 text-center">
                <BookOpen className="h-12 w-12 mx-auto text-muted-foreground mb-4" />
                <h3 className="font-medium mb-2">暂无知识库</h3>
                <p className="text-sm text-muted-foreground mb-4">
                  创建您的第一个知识库来组织文档和资料！
                </p>
                <Button onClick={() => setShowCreateDialog(true)}>
                  <FolderPlus className="h-4 w-4 mr-2" />
                  新建知识库
                </Button>
              </CardContent>
            </Card>
          ) : (
            filteredKnowledgeBases.map((kb) => (
              <Card 
                 key={kb.id} 
                 className={`cursor-pointer transition-colors hover:bg-accent ${
                   selectedKnowledge === kb.id ? "bg-accent" : ""
                 }`}
                 onClick={() => setSelectedKnowledge(kb.id)}
               >
              <CardHeader className="pb-2">
                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-2">
                    <div 
                      className="w-3 h-3 rounded-full" 
                      style={{ backgroundColor: kb.color }}
                    />
                    <CardTitle className="text-sm">{kb.name}</CardTitle>
                    {kb.isStarred && <Star className="h-3 w-3 text-yellow-500 fill-current" />}
                    {kb.isPublic && <Eye className="h-3 w-3 text-blue-500" />}
                  </div>
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <Button variant="ghost" size="sm" className="h-6 w-6 p-0">
                        <MoreHorizontal className="h-3 w-3" />
                      </Button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent>
                      <DropdownMenuItem>
                        <Edit className="h-4 w-4 mr-2" />
                        编辑
                      </DropdownMenuItem>
                      <DropdownMenuItem>
                        <Share className="h-4 w-4 mr-2" />
                        分享
                      </DropdownMenuItem>
                      <DropdownMenuItem className="text-red-600">
                        <Trash2 className="h-4 w-4 mr-2" />
                        删除
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
                <CardDescription className="text-xs">
                  {kb.description || '暂无描述'}
                </CardDescription>
              </CardHeader>
              <CardContent className="pt-0">
                <div className="flex items-center gap-2 mb-2">
                  <Badge variant={kb.isPublic ? "secondary" : "outline"} className="text-xs">
                    {kb.isPublic ? "公开" : "私有"}
                  </Badge>
                </div>
                <div className="flex justify-between text-xs text-muted-foreground">
                  <span>创建于 {formatDate(kb.createdAt)}</span>
                </div>
                <div className="text-xs text-muted-foreground mt-1">
                  更新于 {formatDate(kb.updatedAt)}
                </div>
              </CardContent>
              </Card>
            ))
          )}
        </div>
      </div>

      {/* Right Panel - File Management */}
      <div className="flex-1 flex flex-col">
        {selectedKnowledge ? (
          <>
            {/* Toolbar */}
            <div className="border-b p-4">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold">
                  {knowledgeBases.find(kb => kb.id === selectedKnowledge)?.name}
                </h3>
                <div className="flex gap-2">
                  <Button variant="outline" size="sm" className="gap-2">
                    <Upload className="h-4 w-4" />
                    上传文件
                  </Button>
                  <Button variant="outline" size="sm" className="gap-2">
                    <Download className="h-4 w-4" />
                    导出
                  </Button>
                </div>
              </div>
            </div>

            {/* Content Area */}
            <div className="flex-1 flex">
              {/* File Tree */}
              <div className="w-80 border-r p-4">
                <div className="flex items-center justify-between mb-3">
                  <h4 className="font-medium">文件目录</h4>
                  <div className="flex items-center gap-2">
                    <div className="flex items-center border rounded-lg">
                      <Button
                        variant={viewMode === 'list' ? 'default' : 'ghost'}
                        size="sm"
                        onClick={() => setViewMode('list')}
                        className="rounded-r-none"
                      >
                        <List className="h-4 w-4" />
                      </Button>
                      <Button
                        variant={viewMode === 'grid' ? 'default' : 'ghost'}
                        size="sm"
                        onClick={() => setViewMode('grid')}
                        className="rounded-l-none"
                      >
                        <Grid className="h-4 w-4" />
                      </Button>
                    </div>
                    <Button size="sm">
                      <Plus className="h-4 w-4 mr-2" />
                      上传文件
                    </Button>
                  </div>
                </div>
                <Tabs defaultValue="files" className="w-full">
                  <TabsList className="grid w-full grid-cols-3">
                    <TabsTrigger value="files">文件</TabsTrigger>
                    <TabsTrigger value="recent">最近访问</TabsTrigger>
                    <TabsTrigger value="shared">共享文件</TabsTrigger>
                  </TabsList>
                  <TabsContent value="files" className="mt-4">
                    <div className="space-y-1 max-h-96 overflow-y-auto">
                      {renderFileTree(fileTree)}
                    </div>
                  </TabsContent>
                  <TabsContent value="recent" className="mt-4">
                    <div className="text-center text-muted-foreground py-8">
                      <Clock className="h-12 w-12 mx-auto mb-4" />
                      <p>最近访问的文件将显示在这里</p>
                    </div>
                  </TabsContent>
                  <TabsContent value="shared" className="mt-4">
                    <div className="text-center text-muted-foreground py-8">
                      <Share className="h-12 w-12 mx-auto mb-4" />
                      <p>共享文件将显示在这里</p>
                    </div>
                  </TabsContent>
                </Tabs>
              </div>

              {/* File Preview */}
              <div className="flex-1 p-4">
                {selectedFile ? (
                  <Card className="h-full">
                    <CardHeader>
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-2">
                          {getFileIcon(fileTree.find(f => f.id === selectedFile)?.fileType || 'default')}
                          <CardTitle className="text-base">
                            {fileTree.find(f => f.id === selectedFile)?.name}
                          </CardTitle>
                        </div>
                        <div className="flex items-center gap-2">
                          <Button variant="outline" size="sm">
                            <Download className="h-4 w-4 mr-2" />
                            下载
                          </Button>
                          <Button variant="outline" size="sm">
                            <Share className="h-4 w-4 mr-2" />
                            分享
                          </Button>
                        </div>
                      </div>
                    </CardHeader>
                    <CardContent>
                      <div className="h-80 bg-muted/50 rounded-lg p-4">
                        <div className="flex items-center justify-center h-full text-muted-foreground">
                          <div className="text-center">
                            <FileText className="h-12 w-12 mx-auto mb-2" />
                            <p>文件预览功能开发中...</p>
                            <p className="text-sm mt-1">支持 Markdown、PDF、图片等格式</p>
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                ) : (
                  <Card className="h-full">
                    <CardHeader>
                      <CardTitle className="text-base">文件预览</CardTitle>
                      <CardDescription>
                        选择左侧文件查看内容
                      </CardDescription>
                    </CardHeader>
                    <CardContent>
                      <div className="text-center text-muted-foreground py-12">
                        <FileText className="h-12 w-12 mx-auto mb-4 opacity-50" />
                        <p>请选择文件进行预览</p>
                      </div>
                    </CardContent>
                  </Card>
                )}
              </div>
            </div>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center">
            <div className="text-center text-muted-foreground">
              <Folder className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p>请选择知识库查看内容</p>
            </div>
          </div>
        )}
        </div>
      </div>

      {/* 创建知识库对话框 */}
      <DialogContent>
        <DialogHeader>
          <DialogTitle>创建新知识库</DialogTitle>
          <DialogDescription>
            创建一个新的知识库来组织您的文档和资料
          </DialogDescription>
        </DialogHeader>
        <div className="space-y-4">
          <div>
            <label className="text-sm font-medium">知识库名称</label>
            <Input 
              placeholder="输入知识库名称" 
              className="mt-1" 
              value={newKnowledgeBaseName}
              onChange={(e) => setNewKnowledgeBaseName(e.target.value)}
            />
          </div>
          <div>
            <label className="text-sm font-medium">描述</label>
            <Input 
              placeholder="简要描述知识库内容" 
              className="mt-1" 
              value={newKnowledgeBaseDescription}
              onChange={(e) => setNewKnowledgeBaseDescription(e.target.value)}
            />
          </div>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => setShowCreateDialog(false)}>
            取消
          </Button>
          <Button onClick={handleCreateKnowledgeBase}>
            创建
          </Button>
        </DialogFooter>
      </DialogContent>
      </LayoutApp>
    </MessagesProvider>
  )
}