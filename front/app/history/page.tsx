"use client"

import { LayoutApp } from "@/app/components/layout/layout-app"
import { MessagesProvider } from "@/lib/chat-store/messages/provider"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs"
import { 
  Search, 
  MessageSquare, 
  Calendar,
  Clock,
  Download,
  Eye,
  Trash2,
  Bot
} from "lucide-react"
import { useState } from "react"

export default function HistoryPage() {
  const [searchQuery, setSearchQuery] = useState("")
  const [selectedChat, setSelectedChat] = useState<string | null>(null)
  const [filterType, setFilterType] = useState("all")

  // Mock data for chat history
  const chatHistory = [
    {
      id: "1",
      title: "React Hook 使用问题",
      preview: "如何在函数组件中正确使用 useEffect？",
      model: "GPT-4",
      knowledgeBase: "技术文档",
      messageCount: 8,
      createdAt: "2024-01-15 14:30",
      duration: "12分钟",
      status: "completed"
    },
    {
      id: "2",
      title: "数据库设计讨论",
      preview: "关于用户表和权限表的关系设计...",
      model: "Claude-3",
      knowledgeBase: "项目资料",
      messageCount: 15,
      createdAt: "2024-01-15 10:15",
      duration: "25分钟",
      status: "completed"
    },
    {
      id: "3",
      title: "TypeScript 类型定义",
      preview: "如何为复杂的嵌套对象定义类型？",
      model: "GPT-4",
      knowledgeBase: "学习笔记",
      messageCount: 6,
      createdAt: "2024-01-14 16:45",
      duration: "8分钟",
      status: "completed"
    },
    {
      id: "4",
      title: "API 接口设计",
      preview: "RESTful API 的最佳实践是什么？",
      model: "Claude-3",
      knowledgeBase: null,
      messageCount: 12,
      createdAt: "2024-01-14 09:20",
      duration: "18分钟",
      status: "completed"
    }
  ]

  // Mock data for selected chat detail
  const selectedChatDetail = {
    id: "1",
    title: "React Hook 使用问题",
    model: "GPT-4",
    knowledgeBase: "技术文档",
    createdAt: "2024-01-15 14:30",
    duration: "12分钟",
    messages: [
      {
        id: "1",
        role: "user",
        content: "如何在函数组件中正确使用 useEffect？",
        timestamp: "14:30"
      },
      {
        id: "2",
        role: "assistant",
        content: "useEffect 是 React 中用于处理副作用的 Hook。正确使用 useEffect 需要注意以下几点：\n\n1. **依赖数组**：第二个参数是依赖数组，只有当依赖项发生变化时，effect 才会重新执行。\n\n2. **清理函数**：如果 effect 返回一个函数，React 会在组件卸载时调用它。\n\n```javascript\nuseEffect(() => {\n  const timer = setInterval(() => {\n    console.log('Timer tick');\n  }, 1000);\n  \n  return () => clearInterval(timer);\n}, []);\n```",
        timestamp: "14:31"
      },
      {
        id: "3",
        role: "user",
        content: "那如果我想在组件挂载时只执行一次，应该怎么写？",
        timestamp: "14:32"
      },
      {
        id: "4",
        role: "assistant",
        content: "如果你想在组件挂载时只执行一次，可以传入一个空的依赖数组：\n\n```javascript\nuseEffect(() => {\n  // 这里的代码只会在组件挂载时执行一次\n  fetchData();\n}, []); // 空依赖数组\n```\n\n这相当于类组件中的 `componentDidMount`。",
        timestamp: "14:33"
      }
    ]
  }

  const filteredHistory = chatHistory.filter(chat => {
    const matchesSearch = chat.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
                         chat.preview.toLowerCase().includes(searchQuery.toLowerCase())
    
    if (filterType === "all") return matchesSearch
    if (filterType === "with-kb") return matchesSearch && chat.knowledgeBase
    if (filterType === "without-kb") return matchesSearch && !chat.knowledgeBase
    
    return matchesSearch
  })

  return (
    <MessagesProvider>
      <LayoutApp>
        <div className="flex h-full pt-4">
      {/* Left Panel - Chat History List */}
      <div className="w-96 border-r bg-background p-4">
        <div className="mb-4">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-lg font-semibold">对话历史</h2>
            <Button variant="outline" size="sm" className="gap-2">
              <Download className="h-4 w-4" />
              导出
            </Button>
          </div>
          
          <div className="space-y-3">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
              <Input
                placeholder="搜索对话..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="pl-9"
              />
            </div>
            
            <Tabs value={filterType} onValueChange={setFilterType}>
              <TabsList className="grid w-full grid-cols-3">
                <TabsTrigger value="all" className="text-xs">全部</TabsTrigger>
                <TabsTrigger value="with-kb" className="text-xs">有知识库</TabsTrigger>
                <TabsTrigger value="without-kb" className="text-xs">无知识库</TabsTrigger>
              </TabsList>
            </Tabs>
          </div>
        </div>

        <div className="space-y-2">
          {filteredHistory.map((chat) => (
            <Card 
              key={chat.id} 
              className={`cursor-pointer transition-colors hover:bg-accent ${
                selectedChat === chat.id ? "bg-accent" : ""
              }`}
              onClick={() => setSelectedChat(chat.id)}
            >
              <CardHeader className="pb-2">
                <div className="flex items-start justify-between">
                  <CardTitle className="text-sm line-clamp-1">{chat.title}</CardTitle>
                  <div className="flex gap-1">
                    <Button variant="ghost" size="sm" className="h-6 w-6 p-0">
                      <Eye className="h-3 w-3" />
                    </Button>
                    <Button variant="ghost" size="sm" className="h-6 w-6 p-0">
                      <Trash2 className="h-3 w-3" />
                    </Button>
                  </div>
                </div>
                <CardDescription className="text-xs line-clamp-2">
                  {chat.preview}
                </CardDescription>
              </CardHeader>
              <CardContent className="pt-0">
                <div className="space-y-2">
                  <div className="flex items-center gap-2 text-xs text-muted-foreground">
                    <Bot className="h-3 w-3" />
                    <span>{chat.model}</span>
                    {chat.knowledgeBase && (
                      <>
                        <span>•</span>
                        <span>{chat.knowledgeBase}</span>
                      </>
                    )}
                  </div>
                  <div className="flex items-center justify-between text-xs text-muted-foreground">
                    <div className="flex items-center gap-1">
                      <MessageSquare className="h-3 w-3" />
                      <span>{chat.messageCount} 条消息</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <Clock className="h-3 w-3" />
                      <span>{chat.duration}</span>
                    </div>
                  </div>
                  <div className="flex items-center gap-1 text-xs text-muted-foreground">
                    <Calendar className="h-3 w-3" />
                    <span>{chat.createdAt}</span>
                  </div>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      </div>

      {/* Right Panel - Chat Detail */}
      <div className="flex-1 flex flex-col">
        {selectedChat ? (
          <>
            {/* Header */}
            <div className="border-b p-4">
              <div className="flex items-center justify-between">
                <div>
                  <h3 className="text-lg font-semibold">{selectedChatDetail.title}</h3>
                  <div className="flex items-center gap-4 text-sm text-muted-foreground mt-1">
                    <div className="flex items-center gap-1">
                      <Bot className="h-4 w-4" />
                      <span>{selectedChatDetail.model}</span>
                    </div>
                    {selectedChatDetail.knowledgeBase && (
                      <div className="flex items-center gap-1">
                        <span>知识库: {selectedChatDetail.knowledgeBase}</span>
                      </div>
                    )}
                    <div className="flex items-center gap-1">
                      <Calendar className="h-4 w-4" />
                      <span>{selectedChatDetail.createdAt}</span>
                    </div>
                    <div className="flex items-center gap-1">
                      <Clock className="h-4 w-4" />
                      <span>{selectedChatDetail.duration}</span>
                    </div>
                  </div>
                </div>
                <div className="flex gap-2">
                  <Button variant="outline" size="sm" className="gap-2">
                    <Download className="h-4 w-4" />
                    导出对话
                  </Button>
                </div>
              </div>
            </div>

            {/* Messages */}
            <div className="flex-1 overflow-auto p-4">
              <div className="space-y-4 max-w-4xl mx-auto">
                {selectedChatDetail.messages.map((message) => (
                  <div key={message.id} className={`flex gap-3 ${
                    message.role === "user" ? "justify-end" : "justify-start"
                  }`}>
                    <div className={`max-w-[80%] ${
                      message.role === "user" ? "order-2" : "order-1"
                    }`}>
                      <div className={`rounded-lg p-3 ${
                        message.role === "user" 
                          ? "bg-primary text-primary-foreground" 
                          : "bg-muted"
                      }`}>
                        <div className="whitespace-pre-wrap text-sm">
                          {message.content}
                        </div>
                      </div>
                      <div className={`text-xs text-muted-foreground mt-1 ${
                        message.role === "user" ? "text-right" : "text-left"
                      }`}>
                        {message.timestamp}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </>
        ) : (
          <div className="flex-1 flex items-center justify-center">
            <div className="text-center text-muted-foreground">
              <MessageSquare className="h-12 w-12 mx-auto mb-4 opacity-50" />
              <p>请选择对话查看详情</p>
            </div>
          </div>
        )}
      </div>
        </div>
      </LayoutApp>
    </MessagesProvider>
  )
}