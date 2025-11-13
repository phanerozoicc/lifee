package com.github.phanerozoicc.chat.domain.model

import com.github.phanerozoicc.base.domain.AggregateRoot
import java.time.LocalDateTime
import java.util.*


data class ConversationId(val value: String) {
    companion object {
        // 生成新的对话ID
        fun generate(): ConversationId {
            return ConversationId(UUID.randomUUID().toString())
        }
        // 从字符串创建对话ID
        fun fromString(id: String): ConversationId {
            return ConversationId(id)
        }
    }
}


/**
 * 对话聚合根
 * 关联对话的完整声明周期和消息流
 * 这里需要考虑是否应该保存的对应知识库文档id
 *  这里使用保存的方式, 考虑可以关联展示到具体的对话内容用于展示, 且重新保存文档可以直接修改原文档
 */
class Conversation(
    id: ConversationId, // 对话id
    val userId: String, // 关联用户的id
    var title: String?, // 标题 - 第一轮对话开始时由ai生成
    var modelConfig: ModelConfiguration, // 对话使用到的模型配置
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var updatedAt: LocalDateTime = LocalDateTime.now(),
    private val messages: List<Message> = mutableListOf()
): AggregateRoot<ConversationId>(id) {

    private var status = ConversationStatus.NEW // 对话状态

}

enum class ConversationStatus {
    NEW, // 新建
    ACTIVE, // 活跃中
    ARCHIVED // 已归档
}


