"use client"

import { Button } from "@/components/ui/button"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import { Database, Check } from "lucide-react"
import { useState } from "react"

type KnowledgeBase = {
  id: string
  name: string
  description?: string
}

type ButtonKnowledgeBaseProps = {
  selectedKnowledgeBase?: KnowledgeBase | null
  onSelect: (knowledgeBase: KnowledgeBase | null) => void
  isAuthenticated: boolean
}

// Mock knowledge bases - in real app, this would come from API
const mockKnowledgeBases: KnowledgeBase[] = [
  {
    id: "kb-1",
    name: "技术文档",
    description: "包含API文档、架构设计等技术资料"
  },
  {
    id: "kb-2",
    name: "产品手册",
    description: "产品功能说明和用户指南"
  },
  {
    id: "kb-3",
    name: "学习资料",
    description: "编程教程和最佳实践"
  }
]

export function ButtonKnowledgeBase({
  selectedKnowledgeBase,
  onSelect,
  isAuthenticated,
}: ButtonKnowledgeBaseProps) {
  const [open, setOpen] = useState(false)

  if (!isAuthenticated) {
    return null
  }

  const handleSelect = (knowledgeBase: KnowledgeBase | null) => {
    onSelect(knowledgeBase)
    setOpen(false)
  }

  return (
    <DropdownMenu open={open} onOpenChange={setOpen}>
      <DropdownMenuTrigger asChild>
        <Button
          variant={selectedKnowledgeBase ? "default" : "outline"}
          size="sm"
          className="h-9 rounded-full px-3 text-xs"
        >
          <Database className="mr-1 h-3 w-3" />
          {selectedKnowledgeBase ? selectedKnowledgeBase.name : "选择知识库"}
        </Button>
      </DropdownMenuTrigger>
      <DropdownMenuContent align="start" className="w-64">
        <DropdownMenuItem
          onClick={() => handleSelect(null)}
          className="flex items-center justify-between"
        >
          <div>
            <div className="font-medium">无知识库</div>
            <div className="text-xs text-muted-foreground">使用通用AI对话</div>
          </div>
          {!selectedKnowledgeBase && <Check className="h-4 w-4" />}
        </DropdownMenuItem>
        {mockKnowledgeBases.map((kb) => (
          <DropdownMenuItem
            key={kb.id}
            onClick={() => handleSelect(kb)}
            className="flex items-center justify-between"
          >
            <div>
              <div className="font-medium">{kb.name}</div>
              {kb.description && (
                <div className="text-xs text-muted-foreground">
                  {kb.description}
                </div>
              )}
            </div>
            {selectedKnowledgeBase?.id === kb.id && <Check className="h-4 w-4" />}
          </DropdownMenuItem>
        ))}
      </DropdownMenuContent>
    </DropdownMenu>
  )
}