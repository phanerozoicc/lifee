export type LayoutType = "sidebar" | "fullscreen"

export type UserPreferences = {
  layout: LayoutType
  promptSuggestions: boolean
  showToolInvocations: boolean
  showConversationPreviews: boolean
  multiModelEnabled: boolean
  hiddenModels: string[]
}

export const defaultPreferences: UserPreferences = {
  layout: "sidebar",
  promptSuggestions: true,
  showToolInvocations: true,
  showConversationPreviews: true,
  multiModelEnabled: false,
  hiddenModels: [],
}

// Helper functions to convert between API format (snake_case) and frontend format (camelCase)
export function convertFromApiFormat(apiData: Record<string, unknown>): UserPreferences {
  return {
    layout: apiData.layout || "fullscreen",
    promptSuggestions: apiData.prompt_suggestions ?? true,
    showToolInvocations: apiData.show_tool_invocations ?? true,
    showConversationPreviews: apiData.show_conversation_previews ?? true,
    multiModelEnabled: apiData.multi_model_enabled ?? false,
    hiddenModels: apiData.hidden_models || [],
  }
}

export function convertToApiFormat(preferences: Partial<UserPreferences>) {
  const apiData: Record<string, unknown> = {}
  if (preferences.layout !== undefined) apiData.layout = preferences.layout
  if (preferences.promptSuggestions !== undefined)
    apiData.prompt_suggestions = preferences.promptSuggestions
  if (preferences.showToolInvocations !== undefined)
    apiData.show_tool_invocations = preferences.showToolInvocations
  if (preferences.showConversationPreviews !== undefined)
    apiData.show_conversation_previews = preferences.showConversationPreviews
  if (preferences.multiModelEnabled !== undefined)
    apiData.multi_model_enabled = preferences.multiModelEnabled
  if (preferences.hiddenModels !== undefined)
    apiData.hidden_models = preferences.hiddenModels
  return apiData
}

export function mergePreferences(current: UserPreferences, updates: Partial<UserPreferences>): UserPreferences {
  const merged = { ...current }
  
  for (const [key, value] of Object.entries(updates)) {
    if (value !== undefined) {
      ;(merged as Record<string, unknown>)[key] = value
    }
  }
  
  return merged
}

export function validatePreferences(preferences: Record<string, unknown>): UserPreferences {
  return {
    layout: preferences.layout as LayoutType || "sidebar",
    promptSuggestions: preferences.promptSuggestions as boolean ?? true,
    showToolInvocations: preferences.showToolInvocations as boolean ?? true,
    showConversationPreviews: preferences.showConversationPreviews as boolean ?? true,
    multiModelEnabled: preferences.multiModelEnabled as boolean ?? false,
    hiddenModels: preferences.hiddenModels as string[] || [],
  }
}
