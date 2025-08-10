"use client"

import { groupChatsByDate } from "@/app/components/history/utils"
import { useBreakpoint } from "@/app/hooks/use-breakpoint"
import { ScrollArea } from "@/components/ui/scroll-area"
import { Button } from "@/components/ui/button"
import { useChats } from "@/lib/chat-store/chats/provider"
import {
  ChatTeardropText,
  MagnifyingGlass,
  Plus,
} from "@phosphor-icons/react"
import { useParams, useRouter } from "next/navigation"
import { useMemo } from "react"
import { ChatHistoryItem } from "../layout/sidebar/chat-history-item"
import { cn } from "@/lib/utils"

interface ChatHistoryPanelProps {
  className?: string
}

export function ChatHistoryPanel({ className }: ChatHistoryPanelProps) {
  const isMobile = useBreakpoint(768)
  const { chats, isLoading } = useChats()
  const params = useParams<{ chatId: string }>()
  const currentChatId = params.chatId
  const router = useRouter()

  const groupedChats = useMemo(() => {
    const result = groupChatsByDate(chats, "")
    return result
  }, [chats])

  // Hide on mobile to save space
  if (isMobile) {
    return null
  }

  return (
    <div className={cn(
      "flex flex-col h-full bg-background/50 backdrop-blur-sm border-r border-border/40",
      className
    )}>
      {/* Header */}
      <div className="flex items-center justify-between p-4 border-b border-border/40">
        <h2 className="text-lg font-semibold">对话记录</h2>
        <Button
          size="sm"
          variant="ghost"
          className="h-8 w-8 p-0"
          onClick={() => router.push("/chat")}
        >
          <Plus className="h-4 w-4" />
        </Button>
      </div>
      
      {/* Search */}
      <div className="p-4 border-b border-border/40">
        <div className="relative">
          <MagnifyingGlass className="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-muted-foreground" />
          <input
            type="text"
            placeholder="搜索对话..."
            className="w-full pl-10 pr-4 py-2 text-sm bg-muted/50 border border-border/40 rounded-md focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary/40"
          />
        </div>
      </div>
      
      {/* Chat History */}
      <ScrollArea className="flex-1 p-4">
        <div className="space-y-1">
          {!isLoading && groupedChats && groupedChats.length > 0 ? (
            groupedChats.map((group) => (
              <ChatHistoryItem
                key={group.name}
                group={group}
                currentChatId={currentChatId}
              />
            ))
          ) : (
            <div className="flex flex-col items-center justify-center py-12">
              <ChatTeardropText
                size={32}
                className="text-muted-foreground mb-4 opacity-40"
              />
              <div className="text-muted-foreground text-center">
                <p className="mb-2 text-sm font-medium">暂无对话</p>
                <p className="text-xs opacity-70">开始一个新的对话</p>
              </div>
            </div>
          )}
        </div>
      </ScrollArea>
    </div>
  )
}