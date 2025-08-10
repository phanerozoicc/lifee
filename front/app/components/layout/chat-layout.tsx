"use client"

import { Header } from "@/app/components/layout/header"
import { AppSidebar } from "@/app/components/layout/sidebar/app-sidebar"
import { ChatHistoryPanel } from "@/app/components/chat/chat-history-panel"
import { useUserPreferences } from "@/lib/user-preference-store/provider"
import { useBreakpoint } from "@/app/hooks/use-breakpoint"
import { cn } from "@/lib/utils"

export function ChatLayout({ children }: { children: React.ReactNode }) {
  const { preferences } = useUserPreferences()
  const hasSidebar = preferences.layout === "sidebar"
  const isMobile = useBreakpoint(768)

  return (
    <div className="bg-background flex h-dvh w-full overflow-hidden">
      {/* Left Sidebar */}
      {hasSidebar && <AppSidebar />}
      
      {/* Chat History Panel - responsive width */}
      <ChatHistoryPanel className={cn(
        "flex-shrink-0 transition-all duration-200",
        isMobile ? "w-0" : "w-80"
      )} />
      
      {/* Main Content */}
      <main className="@container relative h-dvh w-0 flex-shrink flex-grow overflow-y-auto">
        <Header hasSidebar={hasSidebar} />
        <div className="pt-16">
          {children}
        </div>
      </main>
    </div>
  )
}