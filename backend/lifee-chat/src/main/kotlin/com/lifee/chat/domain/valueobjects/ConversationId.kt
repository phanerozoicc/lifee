package com.lifee.chat.domain.valueobjects

import java.util.*

/**
 * 对话ID值对象
 */
data class ConversationId(val value: UUID) {
    
    companion object {
        /**
         * 生成新的对话ID
         */
        fun generate(): ConversationId {
            return ConversationId(UUID.randomUUID())
        }
        
        /**
         * 从字符串创建对话ID
         */
        fun fromString(id: String): ConversationId {
            return ConversationId(UUID.fromString(id))
        }
    }
    
    override fun toString(): String {
        return value.toString()
    }
}