package com.lifee.chat.domain.valueobjects

import jakarta.validation.constraints.NotBlank

/**
 * 消息内容值对象
 * 注意：具体的业务验证规则在MessageValidationService中实现
 */
data class MessageContent(
    @field:NotBlank(message = "消息内容不能为空")
    val value: String
) {
    init {
        require(value.isNotBlank()) { "消息内容不能为空" }
    }
    
    /**
     * 获取内容长度
     */
    fun getLength(): Int {
        return value.length
    }
    
    /**
     * 获取内容摘要（前100个字符）
     */
    fun getSummary(): String {
        return if (value.length > 100) {
            value.substring(0, 100) + "..."
        } else {
            value
        }
    }
    
    /**
     * 检查是否为空内容
     */
    fun isEmpty(): Boolean {
        return value.trim().isEmpty()
    }
}