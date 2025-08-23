"use client"

import { ModelSelector } from "@/components/common/model-selector/base"
import {
  PromptInput,
  PromptInputAction,
  PromptInputActions,
  PromptInputTextarea,
} from "@/components/prompt-kit/prompt-input"
import { Button } from "@/components/ui/button"
import { Badge } from "@/components/ui/badge"
import { Tooltip, TooltipContent, TooltipProvider, TooltipTrigger } from "@/components/ui/tooltip"
import { getModelInfo } from "@/lib/models"
import { ArrowUpIcon, StopIcon } from "@phosphor-icons/react"
import { Mic, MicOff, Keyboard, X } from "lucide-react"
import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import { toast } from "sonner"
import { PromptSystem } from "../suggestions/prompt-system"
import { ButtonFileUpload } from "./button-file-upload"
import { ButtonKnowledgeBase } from "./button-knowledge-base"
import { ButtonSearch } from "./button-search"
import { FileList } from "./file-list"

type KnowledgeBase = {
  id: string
  name: string
  description?: string
}

type ChatInputProps = {
  value: string
  onValueChange: (value: string) => void
  onSend: () => void
  isSubmitting?: boolean
  hasMessages?: boolean
  files: File[]
  onFileUpload: (files: File[]) => void
  onFileRemove: (file: File) => void
  onSuggestion: (suggestion: string) => void
  hasSuggestions?: boolean
  onSelectModel: (model: string) => void
  selectedModel: string
  isUserAuthenticated: boolean
  stop: () => void
  status?: "submitted" | "streaming" | "ready" | "error"
  setEnableSearch: (enabled: boolean) => void
  enableSearch: boolean
  quotedText?: { text: string; messageId: string } | null
  selectedKnowledgeBase?: KnowledgeBase | null
  onSelectKnowledgeBase?: (knowledgeBase: KnowledgeBase | null) => void
}

export function ChatInput({
  value,
  onValueChange,
  onSend,
  isSubmitting,
  files,
  onFileUpload,
  onFileRemove,
  onSuggestion,
  hasSuggestions,
  onSelectModel,
  selectedModel,
  isUserAuthenticated,
  stop,
  status,
  setEnableSearch,
  enableSearch,
  quotedText,
  selectedKnowledgeBase,
  onSelectKnowledgeBase,
}: ChatInputProps) {
  const selectModelConfig = getModelInfo(selectedModel)
  const hasSearchSupport = Boolean(selectModelConfig?.webSearch)
  const isOnlyWhitespace = (text: string) => !/[^\s]/.test(text)
  const textareaRef = useRef<HTMLTextAreaElement>(null)
  
  // 语音输入状态
  const [isListening, setIsListening] = useState(false)
  const [speechRecognition, setSpeechRecognition] = useState<SpeechRecognition | null>(null)
  
  // 快捷键提示状态
  const [showShortcuts, setShowShortcuts] = useState(false)
  
  // 初始化语音识别
  useEffect(() => {
    if (typeof window !== 'undefined' && 'webkitSpeechRecognition' in window) {
      const recognition = new (window as any).webkitSpeechRecognition()
      recognition.continuous = true
      recognition.interimResults = true
      recognition.lang = 'zh-CN'
      
      recognition.onresult = (event: any) => {
        let finalTranscript = ''
        for (let i = event.resultIndex; i < event.results.length; i++) {
          if (event.results[i].isFinal) {
            finalTranscript += event.results[i][0].transcript
          }
        }
        if (finalTranscript) {
          onValueChange(value + finalTranscript)
        }
      }
      
      recognition.onerror = (event: any) => {
        console.error('语音识别错误:', event.error)
        setIsListening(false)
        toast.error('语音识别失败，请重试')
      }
      
      recognition.onend = () => {
        setIsListening(false)
      }
      
      setSpeechRecognition(recognition)
    }
  }, [])
  
  // 切换语音输入
  const toggleSpeechRecognition = useCallback(() => {
    if (!speechRecognition) {
      toast.error('您的浏览器不支持语音输入')
      return
    }
    
    if (isListening) {
      speechRecognition.stop()
      setIsListening(false)
    } else {
      speechRecognition.start()
      setIsListening(true)
      toast.success('开始语音输入...')
    }
  }, [speechRecognition, isListening])
  
  // 键盘快捷键处理
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Ctrl/Cmd + / 显示快捷键帮助
      if ((e.ctrlKey || e.metaKey) && e.key === '/') {
        e.preventDefault()
        setShowShortcuts(!showShortcuts)
      }
      
      // Ctrl/Cmd + K 聚焦输入框
      if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
        e.preventDefault()
        textareaRef.current?.focus()
      }
      
      // Ctrl/Cmd + Shift + V 开启语音输入
      if ((e.ctrlKey || e.metaKey) && e.shiftKey && e.key === 'V') {
        e.preventDefault()
        toggleSpeechRecognition()
      }
    }
    
    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [showShortcuts, toggleSpeechRecognition])

  const handleSend = useCallback(() => {
    if (isSubmitting) {
      return
    }

    if (status === "streaming") {
      stop()
      return
    }

    onSend()
  }, [isSubmitting, onSend, status, stop])

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent) => {
      if (isSubmitting) {
        e.preventDefault()
        return
      }

      if (e.key === "Enter" && status === "streaming") {
        e.preventDefault()
        return
      }

      if (e.key === "Enter" && !e.shiftKey) {
        if (isOnlyWhitespace(value)) {
          return
        }

        e.preventDefault()
        onSend()
      }
    },
    [isSubmitting, onSend, status, value]
  )

  const handlePaste = useCallback(
    async (e: React.ClipboardEvent) => {
      const items = e.clipboardData?.items
      if (!items) return

      const hasImageContent = Array.from(items).some((item) =>
        item.type.startsWith("image/")
      )

      if (!isUserAuthenticated && hasImageContent) {
        e.preventDefault()
        return
      }

      if (isUserAuthenticated && hasImageContent) {
        const imageFiles: File[] = []

        for (const item of Array.from(items)) {
          if (item.type.startsWith("image/")) {
            const file = item.getAsFile()
            if (file) {
              const newFile = new File(
                [file],
                `pasted-image-${Date.now()}.${file.type.split("/")[1]}`,
                { type: file.type }
              )
              imageFiles.push(newFile)
            }
          }
        }

        if (imageFiles.length > 0) {
          onFileUpload(imageFiles)
        }
      }
      // Text pasting will work by default for everyone
    },
    [isUserAuthenticated, onFileUpload]
  )

  useEffect(() => {
    if (quotedText) {
      const quoted = quotedText.text
        .split("\n")
        .map((line) => `> ${line}`)
        .join("\n")
      onValueChange(value ? `${value}\n\n${quoted}\n\n` : `${quoted}\n\n`)

      requestAnimationFrame(() => {
        textareaRef.current?.focus()
      })
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [quotedText, onValueChange])

  useMemo(() => {
    if (!hasSearchSupport && enableSearch) {
      setEnableSearch?.(false)
    }
  }, [hasSearchSupport, enableSearch, setEnableSearch])

  return (
    <TooltipProvider>
      <div className="relative flex w-full flex-col gap-4">
        {/* 快捷键提示 */}
        {showShortcuts && (
          <div className="absolute bottom-full left-0 right-0 mb-2 z-50">
            <div className="bg-popover border rounded-lg p-4 shadow-lg">
              <div className="flex items-center justify-between mb-3">
                <h3 className="font-medium">键盘快捷键</h3>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setShowShortcuts(false)}
                  className="h-6 w-6 p-0"
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
              <div className="grid grid-cols-2 gap-3 text-sm">
                <div className="flex items-center gap-2">
                  <Badge variant="outline" className="text-xs">⌘/Ctrl + K</Badge>
                  <span>聚焦输入框</span>
                </div>
                <div className="flex items-center gap-2">
                  <Badge variant="outline" className="text-xs">⌘/Ctrl + /</Badge>
                  <span>显示快捷键</span>
                </div>
                <div className="flex items-center gap-2">
                  <Badge variant="outline" className="text-xs">⌘/Ctrl + ⇧ + V</Badge>
                  <span>语音输入</span>
                </div>
                <div className="flex items-center gap-2">
                  <Badge variant="outline" className="text-xs">Enter</Badge>
                  <span>发送消息</span>
                </div>
              </div>
            </div>
          </div>
        )}
        
        {/* 语音输入状态提示 */}
        {isListening && (
          <div className="absolute bottom-full left-0 right-0 mb-2 z-40">
            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 flex items-center gap-2">
              <div className="flex items-center gap-2">
                <div className="w-2 h-2 bg-red-500 rounded-full animate-pulse"></div>
                <Mic className="h-4 w-4 text-blue-600" />
                <span className="text-sm text-blue-800">正在监听语音输入...</span>
              </div>
              <Button
                variant="ghost"
                size="sm"
                onClick={toggleSpeechRecognition}
                className="ml-auto h-6 px-2 text-blue-600"
              >
                停止
              </Button>
            </div>
          </div>
        )}
        
        {hasSuggestions && (
          <PromptSystem
            onValueChange={onValueChange}
            onSuggestion={onSuggestion}
            value={value}
          />
        )}
        
        <div className="relative order-2 px-2 pb-3 sm:pb-4 md:order-1">
          <PromptInput
            className="bg-popover relative z-10 p-0 pt-1 shadow-xs backdrop-blur-xl"
            maxHeight={200}
            value={value}
            onValueChange={onValueChange}
          >
            <FileList files={files} onFileRemove={onFileRemove} />
            
            {/* 知识库选择提示 */}
            {selectedKnowledgeBase && (
              <div className="px-4 py-2 border-b">
                <div className="flex items-center gap-2">
                  <Badge variant="secondary" className="text-xs">
                    知识库: {selectedKnowledgeBase.name}
                  </Badge>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={() => onSelectKnowledgeBase?.(null)}
                    className="h-4 w-4 p-0"
                  >
                    <X className="h-3 w-3" />
                  </Button>
                </div>
              </div>
            )}
            
            <PromptInputTextarea
              ref={textareaRef}
              placeholder={isListening ? "正在监听语音输入..." : "Ask Zola"}
              onKeyDown={handleKeyDown}
              onPaste={handlePaste}
              className="min-h-[44px] pt-3 pl-4 text-base leading-[1.3] sm:text-base md:text-base"
              disabled={isListening}
            />
            
            <PromptInputActions className="mt-3 w-full justify-between p-2">
              <div className="flex gap-2">
                <ButtonFileUpload
                  onFileUpload={onFileUpload}
                  isUserAuthenticated={isUserAuthenticated}
                  model={selectedModel}
                />
                <ButtonKnowledgeBase
                  selectedKnowledgeBase={selectedKnowledgeBase}
                  onSelect={onSelectKnowledgeBase || (() => {})}
                  isAuthenticated={isUserAuthenticated}
                />
                
                {/* 语音输入按钮 */}
                <Tooltip>
                  <TooltipTrigger asChild>
                    <Button
                      variant={isListening ? "default" : "ghost"}
                      size="sm"
                      onClick={toggleSpeechRecognition}
                      className="rounded-full"
                      disabled={!isUserAuthenticated}
                    >
                      {isListening ? (
                        <MicOff className="h-4 w-4" />
                      ) : (
                        <Mic className="h-4 w-4" />
                      )}
                    </Button>
                  </TooltipTrigger>
                  <TooltipContent>
                    <p>{isListening ? '停止语音输入' : '开始语音输入'}</p>
                  </TooltipContent>
                </Tooltip>
                
                {/* 快捷键帮助按钮 */}
                <Tooltip>
                  <TooltipTrigger asChild>
                    <Button
                      variant="ghost"
                      size="sm"
                      onClick={() => setShowShortcuts(!showShortcuts)}
                      className="rounded-full"
                    >
                      <Keyboard className="h-4 w-4" />
                    </Button>
                  </TooltipTrigger>
                  <TooltipContent>
                    <p>键盘快捷键</p>
                  </TooltipContent>
                </Tooltip>
                
                <ModelSelector
                  selectedModelId={selectedModel}
                  setSelectedModelId={onSelectModel}
                  isUserAuthenticated={isUserAuthenticated}
                  className="rounded-full"
                />
                {hasSearchSupport ? (
                  <ButtonSearch
                    isSelected={enableSearch}
                    onToggle={setEnableSearch}
                    isAuthenticated={isUserAuthenticated}
                  />
                ) : null}
              </div>
              <PromptInputAction
                tooltip={status === "streaming" ? "Stop" : "Send"}
              >
                <Button
                  size="sm"
                  className="size-9 rounded-full transition-all duration-300 ease-out"
                  disabled={!value || isSubmitting || isOnlyWhitespace(value) || isListening}
                  type="button"
                  onClick={handleSend}
                  aria-label={status === "streaming" ? "Stop" : "Send message"}
                >
                  {status === "streaming" ? (
                    <StopIcon className="size-4" />
                  ) : (
                    <ArrowUpIcon className="size-4" />
                  )}
                </Button>
              </PromptInputAction>
            </PromptInputActions>
          </PromptInput>
        </div>
      </div>
    </TooltipProvider>
  )
}
