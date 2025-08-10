"use client"

import { LayoutApp } from "@/app/components/layout/layout-app"
import { MessagesProvider } from "@/lib/chat-store/messages/provider"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { 
  Settings, 
  Bot, 
  User, 
  Database,
  Plug,
  Save,
  RotateCcw,
  Download,
  Upload,
  Key,
  Shield,
  MessageSquare,
  Search,
  Layers
} from "lucide-react"
import { useState } from "react"
import { cn } from "@/lib/utils"

export default function SettingsPage() {
  const [activeSection, setActiveSection] = useState("chat-models")
  
  // Mock configuration data
  const [chatModels, setChatModels] = useState([
    {
      id: "gpt-4",
      name: "GPT-4",
      provider: "OpenAI",
      apiUrl: "https://api.openai.com/v1/chat/completions",
      token: "sk-...",
      modelId: "gpt-4",
      maxTokens: 8192,
      enabled: true
    },
    {
      id: "claude-3",
      name: "Claude-3 Sonnet",
      provider: "Anthropic",
      apiUrl: "https://api.anthropic.com/v1/messages",
      token: "sk-ant-...",
      modelId: "claude-3-sonnet-20240229",
      maxTokens: 4096,
      enabled: false
    }
  ])

  const [embeddingModels, setEmbeddingModels] = useState([
    {
      id: "ada-002",
      name: "Text Embedding Ada 002",
      provider: "OpenAI",
      apiUrl: "https://api.openai.com/v1/embeddings",
      token: "sk-...",
      modelId: "text-embedding-ada-002",
      dimensions: 1536,
      enabled: true
    }
  ])

  const [rerankModels, setRerankModels] = useState([
    {
      id: "cohere-v3",
      name: "Cohere Rerank v3",
      provider: "Cohere",
      apiUrl: "https://api.cohere.ai/v1/rerank",
      token: "co-...",
      modelId: "rerank-multilingual-v3.0",
      maxDocuments: 1000,
      enabled: true
    }
  ])

  const [generalConfig, setGeneralConfig] = useState({
    temperature: 0.7,
    topP: 0.9,
    frequencyPenalty: 0,
    presencePenalty: 0
  })

  const [userInfo, setUserInfo] = useState({
    username: "用户名",
    email: "user@example.com",
    avatar: "",
    timezone: "Asia/Shanghai",
    language: "zh-CN"
  })

  const menuItems = [
    {
      id: "models",
      label: "模型配置",
      icon: Bot,
      children: [
        { id: "chat-models", label: "对话模型", icon: MessageSquare },
        { id: "embedding-models", label: "嵌入模型", icon: Search },
        { id: "rerank-models", label: "重排序模型", icon: Layers },
        { id: "general-params", label: "通用参数", icon: Settings }
      ]
    },
    {
      id: "mcp",
      label: "MCP服务",
      icon: Plug,
      children: []
    },
    {
      id: "user",
      label: "用户设置",
      icon: User,
      children: []
    },
    {
      id: "data",
      label: "数据管理",
      icon: Database,
      children: []
    }
  ]

  const [mcpServices] = useState([
    {
      id: "1",
      name: "文件系统服务",
      endpoint: "mcp://filesystem",
      status: "connected",
      description: "提供文件读写功能"
    },
    {
      id: "2",
      name: "搜索服务",
      endpoint: "mcp://search",
      status: "disconnected",
      description: "提供网络搜索功能"
    }
  ])

  const renderModelConfigForm = (model: any, models: any[], setModels: any) => (
    <Card key={model.id}>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle className="flex items-center gap-2">
              {model.name}
              <span className={`px-2 py-1 rounded-full text-xs ${
                model.enabled ? "bg-green-100 text-green-700" : "bg-gray-100 text-gray-700"
              }`}>
                {model.enabled ? "已启用" : "已禁用"}
              </span>
            </CardTitle>
            <CardDescription>{model.provider}</CardDescription>
          </div>
          <Button
            variant={model.enabled ? "destructive" : "default"}
            size="sm"
            onClick={() => {
              const updated = models.map(m => 
                m.id === model.id ? {...m, enabled: !m.enabled} : m
              )
              setModels(updated)
            }}
          >
            {model.enabled ? "禁用" : "启用"}
          </Button>
        </div>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <label className="text-sm font-medium">API URL</label>
            <Input 
              value={model.apiUrl}
              onChange={(e) => {
                const updated = models.map(m => 
                  m.id === model.id ? {...m, apiUrl: e.target.value} : m
                )
                setModels(updated)
              }}
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium">Token</label>
            <Input 
              type="password"
              value={model.token}
              onChange={(e) => {
                const updated = models.map(m => 
                  m.id === model.id ? {...m, token: e.target.value} : m
                )
                setModels(updated)
              }}
            />
          </div>
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div className="space-y-2">
            <label className="text-sm font-medium">模型ID</label>
            <Input 
              value={model.modelId}
              onChange={(e) => {
                const updated = models.map(m => 
                  m.id === model.id ? {...m, modelId: e.target.value} : m
                )
                setModels(updated)
              }}
            />
          </div>
          <div className="space-y-2">
            <label className="text-sm font-medium">
              {model.maxTokens ? "最大Tokens" : model.dimensions ? "维度" : "最大文档数"}
            </label>
            <Input 
              type="number"
              value={model.maxTokens || model.dimensions || model.maxDocuments}
              onChange={(e) => {
                const updated = models.map(m => {
                  if (m.id === model.id) {
                    const value = parseInt(e.target.value)
                    if (model.maxTokens) return {...m, maxTokens: value}
                    if (model.dimensions) return {...m, dimensions: value}
                    if (model.maxDocuments) return {...m, maxDocuments: value}
                  }
                  return m
                })
                setModels(updated)
              }}
            />
          </div>
        </div>
        <div className="flex gap-2 pt-2">
          <Button size="sm" className="gap-2">
            <Save className="h-4 w-4" />
            保存
          </Button>
          <Button variant="outline" size="sm">
            测试连接
          </Button>
        </div>
      </CardContent>
    </Card>
  )

  const renderContent = () => {
    switch (activeSection) {
      case "chat-models":
        return (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-semibold">对话模型配置</h2>
                <p className="text-muted-foreground">配置用于对话的AI模型</p>
              </div>
              <Button className="gap-2">
                <Bot className="h-4 w-4" />
                添加模型
              </Button>
            </div>
            <div className="space-y-4">
              {chatModels.map(model => renderModelConfigForm(model, chatModels, setChatModels))}
            </div>
          </div>
        )
      
      case "embedding-models":
        return (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-semibold">嵌入模型配置</h2>
                <p className="text-muted-foreground">配置用于文本嵌入的模型</p>
              </div>
              <Button className="gap-2">
                <Search className="h-4 w-4" />
                添加模型
              </Button>
            </div>
            <div className="space-y-4">
              {embeddingModels.map(model => renderModelConfigForm(model, embeddingModels, setEmbeddingModels))}
            </div>
          </div>
        )
      
      case "rerank-models":
        return (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-semibold">重排序模型配置</h2>
                <p className="text-muted-foreground">配置用于结果重排序的模型</p>
              </div>
              <Button className="gap-2">
                <Layers className="h-4 w-4" />
                添加模型
              </Button>
            </div>
            <div className="space-y-4">
              {rerankModels.map(model => renderModelConfigForm(model, rerankModels, setRerankModels))}
            </div>
          </div>
        )
      
      case "general-params":
        return (
          <div className="space-y-6">
            <div>
              <h2 className="text-xl font-semibold">通用参数配置</h2>
              <p className="text-muted-foreground">配置模型的通用参数</p>
            </div>
            <Card>
              <CardHeader>
                <CardTitle>生成参数</CardTitle>
                <CardDescription>控制模型生成行为的参数</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Temperature: {generalConfig.temperature}</label>
                    <input 
                      type="range" 
                      min="0" 
                      max="2" 
                      step="0.1"
                      value={generalConfig.temperature}
                      onChange={(e) => setGeneralConfig({...generalConfig, temperature: parseFloat(e.target.value)})}
                      className="w-full"
                    />
                    <p className="text-xs text-muted-foreground">控制输出的随机性</p>
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Top P: {generalConfig.topP}</label>
                    <input 
                      type="range" 
                      min="0" 
                      max="1" 
                      step="0.1"
                      value={generalConfig.topP}
                      onChange={(e) => setGeneralConfig({...generalConfig, topP: parseFloat(e.target.value)})}
                      className="w-full"
                    />
                    <p className="text-xs text-muted-foreground">核采样参数</p>
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Frequency Penalty: {generalConfig.frequencyPenalty}</label>
                    <input 
                      type="range" 
                      min="-2" 
                      max="2" 
                      step="0.1"
                      value={generalConfig.frequencyPenalty}
                      onChange={(e) => setGeneralConfig({...generalConfig, frequencyPenalty: parseFloat(e.target.value)})}
                      className="w-full"
                    />
                    <p className="text-xs text-muted-foreground">频率惩罚</p>
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium">Presence Penalty: {generalConfig.presencePenalty}</label>
                    <input 
                      type="range" 
                      min="-2" 
                      max="2" 
                      step="0.1"
                      value={generalConfig.presencePenalty}
                      onChange={(e) => setGeneralConfig({...generalConfig, presencePenalty: parseFloat(e.target.value)})}
                      className="w-full"
                    />
                    <p className="text-xs text-muted-foreground">存在惩罚</p>
                  </div>
                </div>
                <div className="flex gap-2 pt-4">
                  <Button className="gap-2">
                    <Save className="h-4 w-4" />
                    保存配置
                  </Button>
                  <Button variant="outline" className="gap-2">
                    <RotateCcw className="h-4 w-4" />
                    重置默认
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        )
      
      case "mcp":
        return (
          <div className="space-y-6">
            <div className="flex items-center justify-between">
              <div>
                <h2 className="text-xl font-semibold">MCP服务管理</h2>
                <p className="text-muted-foreground">管理模型上下文协议(MCP)服务连接</p>
              </div>
              <Button className="gap-2">
                <Plug className="h-4 w-4" />
                添加服务
              </Button>
            </div>
            <div className="space-y-4">
              {mcpServices.map((service) => (
                <Card key={service.id}>
                  <CardContent className="p-4">
                    <div className="flex items-center justify-between">
                      <div className="flex-1">
                        <div className="flex items-center gap-3">
                          <h4 className="font-medium">{service.name}</h4>
                          <span className={`px-2 py-1 rounded-full text-xs ${
                            service.status === "connected" 
                              ? "bg-green-100 text-green-700" 
                              : "bg-red-100 text-red-700"
                          }`}>
                            {service.status === "connected" ? "已连接" : "未连接"}
                          </span>
                        </div>
                        <p className="text-sm text-muted-foreground mt-1">
                          {service.description}
                        </p>
                        <p className="text-xs text-muted-foreground mt-1">
                          端点: {service.endpoint}
                        </p>
                      </div>
                      <div className="flex gap-2">
                        <Button variant="outline" size="sm">
                          {service.status === "connected" ? "断开" : "连接"}
                        </Button>
                        <Button variant="outline" size="sm">
                          配置
                        </Button>
                      </div>
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          </div>
        )
      
      case "user":
        return (
          <div className="space-y-6">
            <div>
              <h2 className="text-xl font-semibold">用户信息</h2>
              <p className="text-muted-foreground">管理个人账户信息和偏好设置</p>
            </div>
            <Card>
              <CardHeader>
                <CardTitle>个人信息</CardTitle>
                <CardDescription>更新您的个人资料</CardDescription>
              </CardHeader>
              <CardContent className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">用户名</label>
                    <Input 
                      value={userInfo.username}
                      onChange={(e) => setUserInfo({...userInfo, username: e.target.value})}
                    />
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium">邮箱</label>
                    <Input 
                      type="email"
                      value={userInfo.email}
                      onChange={(e) => setUserInfo({...userInfo, email: e.target.value})}
                    />
                  </div>
                </div>
                
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <label className="text-sm font-medium">时区</label>
                    <select 
                      className="w-full p-2 border rounded-md"
                      value={userInfo.timezone}
                      onChange={(e) => setUserInfo({...userInfo, timezone: e.target.value})}
                    >
                      <option value="Asia/Shanghai">Asia/Shanghai</option>
                      <option value="UTC">UTC</option>
                      <option value="America/New_York">America/New_York</option>
                    </select>
                  </div>
                  <div className="space-y-2">
                    <label className="text-sm font-medium">语言</label>
                    <select 
                      className="w-full p-2 border rounded-md"
                      value={userInfo.language}
                      onChange={(e) => setUserInfo({...userInfo, language: e.target.value})}
                    >
                      <option value="zh-CN">简体中文</option>
                      <option value="en-US">English</option>
                    </select>
                  </div>
                </div>

                <div className="flex gap-2 pt-4">
                  <Button className="gap-2">
                    <Save className="h-4 w-4" />
                    保存设置
                  </Button>
                  <Button variant="outline" className="gap-2">
                    <Key className="h-4 w-4" />
                    修改密码
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        )
      
      case "data":
        return (
          <div className="space-y-6">
            <div>
              <h2 className="text-xl font-semibold">数据管理</h2>
              <p className="text-muted-foreground">备份、恢复和管理系统数据</p>
            </div>
            <Card>
              <CardHeader>
                <CardTitle>数据操作</CardTitle>
                <CardDescription>导入导出系统数据</CardDescription>
              </CardHeader>
              <CardContent className="space-y-6">
                <div className="grid grid-cols-2 gap-6">
                  <div className="space-y-4">
                    <h4 className="font-medium flex items-center gap-2">
                      <Download className="h-4 w-4" />
                      数据导出
                    </h4>
                    <div className="space-y-2">
                      <Button variant="outline" className="w-full justify-start gap-2">
                        <Download className="h-4 w-4" />
                        导出对话记录
                      </Button>
                      <Button variant="outline" className="w-full justify-start gap-2">
                        <Download className="h-4 w-4" />
                        导出知识库
                      </Button>
                      <Button variant="outline" className="w-full justify-start gap-2">
                        <Download className="h-4 w-4" />
                        导出系统配置
                      </Button>
                    </div>
                  </div>
                  
                  <div className="space-y-4">
                    <h4 className="font-medium flex items-center gap-2">
                      <Upload className="h-4 w-4" />
                      数据导入
                    </h4>
                    <div className="space-y-2">
                      <Button variant="outline" className="w-full justify-start gap-2">
                        <Upload className="h-4 w-4" />
                        导入对话记录
                      </Button>
                      <Button variant="outline" className="w-full justify-start gap-2">
                        <Upload className="h-4 w-4" />
                        导入知识库
                      </Button>
                      <Button variant="outline" className="w-full justify-start gap-2">
                        <Upload className="h-4 w-4" />
                        导入系统配置
                      </Button>
                    </div>
                  </div>
                </div>
                
                <div className="border-t pt-6">
                  <h4 className="font-medium flex items-center gap-2 mb-4">
                    <Shield className="h-4 w-4" />
                    危险操作
                  </h4>
                  <div className="space-y-2">
                    <Button variant="destructive" className="gap-2">
                      清空所有对话记录
                    </Button>
                    <Button variant="destructive" className="gap-2">
                      重置系统配置
                    </Button>
                  </div>
                </div>
              </CardContent>
            </Card>
          </div>
        )
      
      default:
        return null
    }
  }

  return (
    <MessagesProvider>
      <LayoutApp>
        <div className="flex h-screen pt-16">
          {/* Sidebar */}
          <div className="w-64 bg-background border-r border-border flex-shrink-0">
            <div className="p-4 border-b border-border">
              <h1 className="text-lg font-semibold flex items-center gap-2">
                <Settings className="h-5 w-5" />
                系统配置
              </h1>
            </div>
            <nav className="p-2">
              {menuItems.map((item) => (
                <div key={item.id} className="mb-2">
                  <div className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-muted-foreground">
                    <item.icon className="h-4 w-4" />
                    {item.label}
                  </div>
                  {item.children.length > 0 && (
                    <div className="ml-4 space-y-1">
                      {item.children.map((child) => (
                        <button
                          key={child.id}
                          onClick={() => setActiveSection(child.id)}
                          className={cn(
                            "w-full flex items-center gap-2 px-3 py-2 text-sm rounded-md transition-colors",
                            activeSection === child.id
                              ? "bg-primary text-primary-foreground"
                              : "text-muted-foreground hover:bg-muted hover:text-foreground"
                          )}
                        >
                          <child.icon className="h-4 w-4" />
                          {child.label}
                        </button>
                      ))}
                    </div>
                  )}
                  {item.children.length === 0 && (
                    <button
                      onClick={() => setActiveSection(item.id)}
                      className={cn(
                        "w-full flex items-center gap-2 px-3 py-2 text-sm rounded-md transition-colors ml-4",
                        activeSection === item.id
                          ? "bg-primary text-primary-foreground"
                          : "text-muted-foreground hover:bg-muted hover:text-foreground"
                      )}
                    >
                      <item.icon className="h-4 w-4" />
                      {item.label}
                    </button>
                  )}
                </div>
              ))}
            </nav>
          </div>

          {/* Main Content */}
          <div className="flex-1 overflow-y-auto">
            <div className="p-6">
              {renderContent()}
            </div>
          </div>
        </div>
      </LayoutApp>
    </MessagesProvider>
  )
}