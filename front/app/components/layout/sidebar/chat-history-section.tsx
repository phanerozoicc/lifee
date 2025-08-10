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
import { ChatHistoryItem } from "./chat-history-item"

export function ChatHistorySection() {
  const isMobile = useBreakpoint(768)
  const { chats, isLoading } = useChats()
  const params = useParams<{ chatId: string }>()
  const currentChatId = params.chatId
  const router = useRouter()

  const groupedChats = useMemo(() => {
    const result = groupChatsByDate(chats, "")
    return result
  }, [chats])

  return (
    <div className="mt-4">
      <div className="mb-3 flex items-center justify-between px-2">
        <h3 className="text-sm font-medium text-muted-foreground">对话记录</h3>
        <Button
          size="sm"
          variant="ghost"
          className="h-6 w-6 p-0"
          onClick={() => router.push("/chat")}
        >
          <Plus className="h-4 w-4" />
        </Button>
      </div>
      
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
          <div className="flex flex-col items-center justify-center py-8">
            <ChatTeardropText
              size={20}
              className="text-muted-foreground mb-2 opacity-40"
            />
            <div className="text-muted-foreground text-center">
              <p className="mb-1 text-sm font-medium">暂无对话</p>
              <p className="text-xs opacity-70">开始一个新的对话</p>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}