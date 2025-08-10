"use client"

import { LayoutApp } from "@/app/components/layout/layout-app"
import { MessagesProvider } from "@/lib/chat-store/messages/provider"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { 
  FolderPlus, 
  Upload, 
  Search, 
  FileText, 
  Folder,
  MoreHorizontal,
  Download
} from "lucide-react"
import { useState } from "react"

export default function KnowledgePage() {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedKnowledge, setSelectedKnowledge] = useState<string | null>(null)

  // Mock data for knowledge bases
  const knowledgeBases = [
    {
      id: "1",
      name: "技术文档",
      description: "编程相关的技术文档和教程",
      fileCount: 156,
      size: "2.3 GB",
      lastUpdated: "2024-01-15"
    },
    {
      id: "2", 
      name: "学习笔记",
      description: "个人学习记录和总结",
      fileCount: 89,
      size: "1.2 GB",
      lastUpdated: "2024-01-14"
    },
    {
      id: "3",
      name: "项目资料",
      description: "项目相关的文档和资源",
      fileCount: 234,
      size: "3.1 GB",
      lastUpdated: "2024-01-13"
    }
  ]

  // Mock data for file tree
  const fileTree = [
    {
      id: "1",
      name: "前端开发",
      type: "folder",
      children: [
        { id: "1-1", name: "React.md", type: "file", size: "45 KB" },
        { id: "1-2", name: "Vue.md", type: "file", size: "32 KB" },
        { id: "1-3", name: "TypeScript.md", type: "file", size: "67 KB" }
      ]
    },
    {
      id: "2",
      name: "后端开发",
      type: "folder",
      children: [
        { id: "2-1", name: "Spring Boot.md", type: "file", size: "89 KB" },
        { id: "2-2", name: "数据库设计.md", type: "file", size: "123 KB" }
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
            <Button size="sm" className="gap-2">
              <FolderPlus className="h-4 w-4" />
              新建
            </Button>
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
          {knowledgeBases.map((kb) => (
            <Card 
              key={kb.id} 
              className={`cursor-pointer transition-colors hover:bg-accent ${
                selectedKnowledge === kb.id ? "bg-accent" : ""
              }`}
              onClick={() => setSelectedKnowledge(kb.id)}
            >
              <CardHeader className="pb-2">
                <CardTitle className="text-sm">{kb.name}</CardTitle>
                <CardDescription className="text-xs">
                  {kb.description}
                </CardDescription>
              </CardHeader>
              <CardContent className="pt-0">
                <div className="flex justify-between text-xs text-muted-foreground">
                  <span>{kb.fileCount} 文件</span>
                  <span>{kb.size}</span>
                </div>
                <div className="text-xs text-muted-foreground mt-1">
                  更新于 {kb.lastUpdated}
                </div>
              </CardContent>
            </Card>
          ))}
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
                <h4 className="font-medium mb-3">文件目录</h4>
                <div className="space-y-1">
                  {fileTree.map((item) => (
                    <div key={item.id}>
                      <div className="flex items-center gap-2 p-2 rounded hover:bg-accent cursor-pointer">
                        <Folder className="h-4 w-4 text-blue-500" />
                        <span className="text-sm">{item.name}</span>
                      </div>
                      {item.children && (
                        <div className="ml-6 space-y-1">
                          {item.children.map((child) => (
                            <div key={child.id} className="flex items-center justify-between p-2 rounded hover:bg-accent cursor-pointer group">
                              <div className="flex items-center gap-2">
                                <FileText className="h-4 w-4 text-gray-500" />
                                <span className="text-sm">{child.name}</span>
                              </div>
                              <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100">
                                <span className="text-xs text-muted-foreground">{child.size}</span>
                                <Button variant="ghost" size="sm" className="h-6 w-6 p-0">
                                  <MoreHorizontal className="h-3 w-3" />
                                </Button>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  ))}
                </div>
              </div>

              {/* File Preview */}
              <div className="flex-1 p-4">
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
      </LayoutApp>
    </MessagesProvider>
  )
}