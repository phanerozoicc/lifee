package com.lifee.chat.domain.valueobjects

import java.util.*

/**
 * 消息ID值对象
 */
data class MessageId(val value: UUID) {
    
    companion object {
        /**
         * 生成新的消息ID
         */
        fun generate(): MessageId {
            return MessageId(UUID.randomUUID())
        }
        
        /**
         * 从字符串创建消息ID
         */
        fun fromString(id: String): MessageId {
            return MessageId(UUID.fromString(id))
        }
    }
    
    override fun toString(): String {
        return value.toString()
    }
}