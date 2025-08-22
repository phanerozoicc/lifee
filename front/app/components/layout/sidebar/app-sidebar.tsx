"use client"

import { useBreakpoint } from "@/app/hooks/use-breakpoint"
import { ScrollArea } from "@/components/ui/scroll-area"
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  useSidebar,
} from "@/components/ui/sidebar"
import {
  GithubLogo,
  X,
} from "@phosphor-icons/react"
import {
  Brain,
  BookOpen,
  History,
  Settings,
  MessageSquare
} from "lucide-react"
import { useRouter } from "next/navigation"
import { SidebarProject } from "./sidebar-project"


export function AppSidebar() {
  const isMobile = useBreakpoint(768)
  const { setOpenMobile } = useSidebar()
  const router = useRouter()
  


  return (
    <Sidebar
      collapsible="offcanvas"
      variant="sidebar"
      className="border-border/40 border-r bg-transparent"
    >
      <SidebarHeader className="h-14 pl-3">
        <div className="flex justify-between">
          {isMobile ? (
            <button
              type="button"
              onClick={() => setOpenMobile(false)}
              className="text-muted-foreground hover:text-foreground hover:bg-muted inline-flex size-9 items-center justify-center rounded-md bg-transparent transition-colors focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:outline-none"
            >
              <X size={24} />
            </button>
          ) : (
            <div className="h-full" />
          )}
        </div>
      </SidebarHeader>
      <SidebarContent className="border-border/40 border-t">
        <ScrollArea className="flex h-full px-3 [&>div>div]:!block">
          <div className="mt-3 mb-5 flex w-full flex-col items-start gap-1">
            {/* Navigation Menu */}
            <button
              className="hover:bg-accent/80 hover:text-foreground text-primary group/nav relative inline-flex w-full items-center rounded-md bg-transparent px-2 py-2 text-sm transition-colors"
              type="button"
              onClick={() => router.push("/recommendations")}
            >
              <div className="flex items-center gap-2">
                <Brain size={20} />
                智能推荐
              </div>
            </button>
            
            <button
              className="hover:bg-accent/80 hover:text-foreground text-primary group/nav relative inline-flex w-full items-center rounded-md bg-transparent px-2 py-2 text-sm transition-colors"
              type="button"
              onClick={() => router.push("/chat")}
            >
              <div className="flex items-center gap-2">
                <MessageSquare size={20} />
                LLM对话
              </div>
              <div className="text-muted-foreground ml-auto text-xs opacity-0 duration-150 group-hover/nav:opacity-100">
                ⌘⇧U
              </div>
            </button>
            
            <button
              className="hover:bg-accent/80 hover:text-foreground text-primary group/nav relative inline-flex w-full items-center rounded-md bg-transparent px-2 py-2 text-sm transition-colors"
              type="button"
              onClick={() => router.push("/knowledge")}
            >
              <div className="flex items-center gap-2">
                <BookOpen size={20} />
                知识库管理
              </div>
            </button>
            
            <button
              className="hover:bg-accent/80 hover:text-foreground text-primary group/nav relative inline-flex w-full items-center rounded-md bg-transparent px-2 py-2 text-sm transition-colors"
              type="button"
              onClick={() => router.push("/history")}
            >
              <div className="flex items-center gap-2">
                <History size={20} />
                对话历史
              </div>
            </button>
            
            <button
              className="hover:bg-accent/80 hover:text-foreground text-primary group/nav relative inline-flex w-full items-center rounded-md bg-transparent px-2 py-2 text-sm transition-colors"
              type="button"
              onClick={() => router.push("/settings")}
            >
              <div className="flex items-center gap-2">
                <Settings size={20} />
                系统配置
              </div>
            </button>
            

          </div>
          
          {/* Project list */}
          <SidebarProject />
        </ScrollArea>
      </SidebarContent>
      <SidebarFooter className="border-border/40 mb-2 border-t p-3">
        <a
          href="https://github.com/ibelick/zola"
          className="hover:bg-muted flex items-center gap-2 rounded-md p-2"
          target="_blank"
          aria-label="Star the repo on GitHub"
        >
          <div className="rounded-full border p-1">
            <GithubLogo className="size-4" />
          </div>
          <div className="flex flex-col">
            <div className="text-sidebar-foreground text-sm font-medium">
              Zola is open source
            </div>
            <div className="text-sidebar-foreground/70 text-xs">
              Star the repo on GitHub!
            </div>
          </div>
        </a>
      </SidebarFooter>
    </Sidebar>
  )
}
