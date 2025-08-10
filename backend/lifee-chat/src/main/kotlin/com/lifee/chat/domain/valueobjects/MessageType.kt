package com.lifee.chat.domain.valueobjects

/**
 * 消息类型枚举
 */
enum class MessageType(val value: String, val description: String) {
    USER("USER", "用户消息"),
    ASSISTANT("ASSISTANT", "AI助手消息"),
    SYSTEM("SYSTEM", "系统消息");
    
    companion object {
        /**
         * 从字符串值获取消息类型
         */
        fun fromValue(value: String): MessageType {
            return values().find { it.value == value.uppercase() }
                ?: throw IllegalArgumentException("不支持的消息类型: $value")
        }
        
        /**
         * 获取所有支持的消息类型
         */
        fun getSupportedTypes(): List<String> {
            return values().map { it.value }
        }
    }
    
    override fun toString(): String {
        return value
    }
}