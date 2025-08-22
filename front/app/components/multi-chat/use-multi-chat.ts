import { useChat } from "@ai-sdk/react"
import { useMemo } from "react"

type ModelConfig = {
  id: string
  name: string
  provider: string
}

type ModelChat = {
  model: ModelConfig
  messages: unknown[]
  isLoading: boolean
  append: (message: unknown, options?: unknown) => void
  stop: () => void
}

// Maximum number of models we support
const MAX_MODELS = 10

export function useMultiChat(models: ModelConfig[]): ModelChat[] {
  // Create a fixed number of useChat hooks to avoid conditional hook calls
  const chat1 = useChat({ api: "/api/chat" })
  const chat2 = useChat({ api: "/api/chat" })
  const chat3 = useChat({ api: "/api/chat" })
  const chat4 = useChat({ api: "/api/chat" })
  const chat5 = useChat({ api: "/api/chat" })
  const chat6 = useChat({ api: "/api/chat" })
  const chat7 = useChat({ api: "/api/chat" })
  const chat8 = useChat({ api: "/api/chat" })
  const chat9 = useChat({ api: "/api/chat" })
  const chat10 = useChat({ api: "/api/chat" })
  
  // Map only the provided models to their corresponding chat hooks
  const activeChatInstances = useMemo(() => {
    const chatHooks = [chat1, chat2, chat3, chat4, chat5, chat6, chat7, chat8, chat9, chat10]
    const instances = models.slice(0, MAX_MODELS).map((model, index) => {
      const chatHook = chatHooks[index]

      return {
        model,
        messages: chatHook.messages,
        isLoading: chatHook.isLoading,
        append: (message: unknown, options?: unknown) => {
          return chatHook.append(message, options)
        },
        stop: chatHook.stop,
      }
    })

    return instances
  }, [models, chat1, chat2, chat3, chat4, chat5, chat6, chat7, chat8, chat9, chat10])

  return activeChatInstances
}
