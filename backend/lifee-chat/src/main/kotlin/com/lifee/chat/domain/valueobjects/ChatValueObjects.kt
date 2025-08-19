package com.lifee.chat.domain.valueobjects

import com.lifee.common.domain.ValueObject
import java.util.*

/**
 * 对话ID值对象
 */
data class ConversationId(
    val value: UUID
) : ValueObject() {
    
    companion object {
        fun generate(): ConversationId = ConversationId(UUID.randomUUID())
        
        fun fromString(value: String): ConversationId {
            return ConversationId(UUID.fromString(value))
        }
    }
    
    override fun toString(): String = value.toString()
}

/**
 * 消息ID值对象
 */
data class MessageId(
    val value: UUID
) : ValueObject() {
    
    companion object {
        fun generate(): MessageId = MessageId(UUID.randomUUID())
        
        fun fromString(value: String): MessageId {
            return MessageId(UUID.fromString(value))
        }
    }
    
    override fun toString(): String = value.toString()
}

/**
 * 对话标题值对象
 */
data class ConversationTitle(
    val value: String
) : ValueObject() {
    
    init {
        require(value.isNotBlank()) { "对话标题不能为空" }
        require(value.length <= 200) { "对话标题长度不能超过200个字符" }
    }
    
    companion object {
        fun default(): ConversationTitle = ConversationTitle("新对话")
        
        fun fromUserMessage(message: String): ConversationTitle {
            val title = if (message.length > 50) {
                message.take(47) + "..."
            } else {
                message
            }
            return ConversationTitle(title)
        }
    }
}

/**
 * 消息内容值对象
 */
data class MessageContent(
    val value: String
) : ValueObject() {
    
    init {
        require(value.isNotBlank()) { "消息内容不能为空" }
        require(value.length <= 50000) { "消息内容长度不能超过50000个字符" }
    }
    
    val length: Int get() = value.length
    
    fun truncate(maxLength: Int): MessageContent {
        return if (value.length <= maxLength) {
            this
        } else {
            MessageContent(value.take(maxLength - 3) + "...")
        }
    }
}

/**
 * 消息类型枚举
 */
enum class MessageType {
    USER,      // 用户消息
    ASSISTANT  // 助手消息
}

/**
 * 对话状态枚举
 */
enum class ConversationStatus {
    ACTIVE,    // 活跃
    ARCHIVED,  // 归档
    DELETED    // 已删除
}

/**
 * 模型配置值对象
 */
data class ModelConfiguration(
    val modelName: String,
    val temperature: Double = 0.7,
    val maxTokens: Int = 2048,
    val topP: Double = 1.0,
    val frequencyPenalty: Double = 0.0,
    val presencePenalty: Double = 0.0
) : ValueObject() {
    
    init {
        require(modelName.isNotBlank()) { "模型名称不能为空" }
        require(temperature in 0.0..2.0) { "温度参数必须在0.0到2.0之间" }
        require(maxTokens > 0) { "最大令牌数必须大于0" }
        require(topP in 0.0..1.0) { "TopP参数必须在0.0到1.0之间" }
        require(frequencyPenalty in -2.0..2.0) { "频率惩罚必须在-2.0到2.0之间" }
        require(presencePenalty in -2.0..2.0) { "存在惩罚必须在-2.0到2.0之间" }
    }
    
    companion object {
        fun default(): ModelConfiguration {
            return ModelConfiguration(
                modelName = "gpt-3.5-turbo",
                temperature = 0.7,
                maxTokens = 2048,
                topP = 1.0
            )
        }
        
        fun creative(): ModelConfiguration {
            return ModelConfiguration(
                modelName = "gpt-4",
                temperature = 1.2,
                maxTokens = 4096,
                topP = 0.9
            )
        }
        
        fun precise(): ModelConfiguration {
            return ModelConfiguration(
                modelName = "gpt-4",
                temperature = 0.1,
                maxTokens = 2048,
                topP = 0.1
            )
        }
    }
}

/**
 * RAG上下文值对象
 */
data class RAGContext(
    val retrievedDocuments: List<RetrievedDocument>,
    val searchQuery: String,
    val relevanceThreshold: Double = 0.7
) : ValueObject() {
    
    init {
        require(searchQuery.isNotBlank()) { "搜索查询不能为空" }
        require(relevanceThreshold in 0.0..1.0) { "相关性阈值必须在0.0到1.0之间" }
    }
    
    val documentCount: Int get() = retrievedDocuments.size
    
    fun getTopDocuments(count: Int): List<RetrievedDocument> {
        return retrievedDocuments.sortedByDescending { it.relevanceScore }.take(count)
    }
}

/**
 * 检索到的文档值对象
 */
data class RetrievedDocument(
    val documentId: String,
    val title: String,
    val content: String,
    val relevanceScore: Double,
    val chunkIndex: Int = 0
) : ValueObject() {
    
    init {
        require(documentId.isNotBlank()) { "文档ID不能为空" }
        require(title.isNotBlank()) { "文档标题不能为空" }
        require(content.isNotBlank()) { "文档内容不能为空" }
        require(relevanceScore in 0.0..1.0) { "相关性分数必须在0.0到1.0之间" }
        require(chunkIndex >= 0) { "块索引必须大于等于0" }
    }
}

/**
 * 模型使用情况值对象
 */
data class ModelUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val cost: Double = 0.0
) : ValueObject() {
    
    init {
        require(promptTokens >= 0) { "提示令牌数不能为负数" }
        require(completionTokens >= 0) { "完成令牌数不能为负数" }
        require(totalTokens >= promptTokens + completionTokens) { "总令牌数必须大于等于提示令牌数和完成令牌数之和" }
        require(cost >= 0.0) { "成本不能为负数" }
    }
}

/**
 * 消息附件值对象
 */
data class MessageAttachment(
    val id: String,
    val name: String,
    val type: AttachmentType,
    val size: Long,
    val url: String
) : ValueObject() {
    
    init {
        require(id.isNotBlank()) { "附件ID不能为空" }
        require(name.isNotBlank()) { "附件名称不能为空" }
        require(size > 0) { "附件大小必须大于0" }
        require(url.isNotBlank()) { "附件URL不能为空" }
    }
}

/**
 * 附件类型枚举
 */
enum class AttachmentType {
    IMAGE,     // 图片
    DOCUMENT,  // 文档
    AUDIO,     // 音频
    VIDEO,     // 视频
    OTHER      // 其他
}