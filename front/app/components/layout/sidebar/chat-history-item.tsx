"use client"

import { Button } from "@/components/ui/button"
import { useChats } from "@/lib/chat-store/chats/provider"
import { cn } from "@/lib/utils"
import { DotsThreeVertical, Trash } from "@phosphor-icons/react"
import { useRouter } from "next/navigation"
import { useState } from "react"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import type { Chats } from "@/lib/chat-store/types"

type TimeGroup = {
  name: string
  chats: Chats[]
}

interface ChatHistoryItemProps {
  group: TimeGroup
  currentChatId?: string
}

export function ChatHistoryItem({ group, currentChatId }: ChatHistoryItemProps) {
  const { deleteChat } = useChats()
  const router = useRouter()
  const [isExpanded, setIsExpanded] = useState(true)

  const handleChatClick = (chatId: string) => {
    router.push(`/c/${chatId}`)
  }

  const handleDeleteChat = async (chatId: string, e: React.MouseEvent) => {
    e.stopPropagation()
    await deleteChat(chatId)
  }

  return (
    <div className="space-y-1">
      <button
        onClick={() => setIsExpanded(!isExpanded)}
        className="w-full text-left px-2 py-1 text-xs font-medium text-muted-foreground hover:text-foreground transition-colors"
      >
        {group.name}
      </button>
      
      {isExpanded && (
        <div className="space-y-1 ml-2">
          {group.chats.map((chat) => (
            <div
              key={chat.id}
              className={cn(
                "group flex items-center gap-2 rounded-md px-2 py-1.5 text-sm cursor-pointer transition-colors hover:bg-accent",
                currentChatId === chat.id && "bg-accent"
              )}
              onClick={() => handleChatClick(chat.id)}
            >
              <div className="flex-1 truncate">
                {chat.title || "新对话"}
              </div>
              
              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button
                    variant="ghost"
                    size="sm"
                    className="h-6 w-6 p-0 opacity-0 group-hover:opacity-100"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <DotsThreeVertical className="h-3 w-3" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end">
                  <DropdownMenuItem
                    onClick={(e) => handleDeleteChat(chat.id, e)}
                    className="text-destructive"
                  >
                    <Trash className="h-4 w-4 mr-2" />
                    删除对话
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}