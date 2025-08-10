import { ChatContainer } from "@/app/components/chat/chat-container"
import { ChatLayout } from "@/app/components/layout/chat-layout"
import { MessagesProvider } from "@/lib/chat-store/messages/provider"

export const dynamic = "force-dynamic"

export default function ChatPage() {
  return (
    <MessagesProvider>
      <ChatLayout>
        <ChatContainer />
      </ChatLayout>
    </MessagesProvider>
  )
}