"use client"

import { DropdownMenuItem } from "@/components/ui/dropdown-menu"
import { User } from "@phosphor-icons/react"
import { useRouter } from "next/navigation"

type SettingsTriggerProps = {
  onOpenChange: (open: boolean) => void
}

export function SettingsTrigger({ onOpenChange }: SettingsTriggerProps) {
  const router = useRouter()

  const handleClick = () => {
    onOpenChange(false) // 关闭下拉菜单
    router.push("/settings") // 跳转到设置页面
  }

  return (
    <DropdownMenuItem onClick={handleClick}>
      <User className="size-4" />
      <span>用户配置</span>
    </DropdownMenuItem>
  )
}
