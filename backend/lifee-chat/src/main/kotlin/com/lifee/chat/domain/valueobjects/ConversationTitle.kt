package com.lifee.chat.domain.valueobjects

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 对话标题值对象
 */
data class ConversationTitle(
    @field:NotBlank(message = "对话标题不能为空")
    @field:Size(min = 1, max = 100, message = "对话标题长度必须在1-100个字符之间")
    val value: String
) {
    init {
        require(value.isNotBlank()) { "对话标题不能为空" }
        require(value.length <= 100) { "对话标题长度不能超过100个字符" }
    }
    
    companion object {
        /**
         * 创建默认标题
         */
        fun default(): ConversationTitle {
            return ConversationTitle("新对话")
        }
        
        /**
         * 从消息内容生成标题
         */
        fun fromContent(content: String): ConversationTitle {
            val title = if (content.length > 50) {
                content.substring(0, 50) + "..."
            } else {
                content
            }
            return ConversationTitle(title.trim())
        }
    }
}