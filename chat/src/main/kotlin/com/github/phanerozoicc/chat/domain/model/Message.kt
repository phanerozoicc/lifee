package com.github.phanerozoicc.chat.domain.model

import jakarta.validation.constraints.NotBlank


data class MessageId(val id: String) {
    override fun toString(): String {
        return id
    }
    companion object {
        fun generate(): MessageId {
            return MessageId(java.util.UUID.randomUUID().toString())
        }
        fun fromString(id: String): MessageId {
            return MessageId(id)
        }
    }
}

class Message(
    val id: MessageId,
    val content: MessageContent,
    val type: MessageType,
    val userId: String,
    val createdAt: java.time.Instant = java.time.Instant.now(),
    var updatedAt: java.time.Instant? = null
) {

}

data class MessageContent(
    @field:NotBlank(message = "消息内容不能为空")
    val value: String
) {
    init {
        require(value.isNotBlank()) {"消息内容不能为空"}
    }

    /**
     * 获取消息长度
     */
    fun getLength(): Int {
        return value.length
    }

    /**
     * 获取内容摘要(前100个字符)
     */
    fun getSummary(): String {
        return if (value.length <= 100) value else value.take(100) + "..."
    }

    /**
     * 是否为空
     */
    fun isEmpty(): Boolean {
        return value.isBlank()
    }

    // TODO 添加向量存储

}