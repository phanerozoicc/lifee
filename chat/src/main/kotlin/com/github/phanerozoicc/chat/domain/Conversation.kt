package com.github.phanerozoicc.chat.domain

import com.github.phanerozoicc.base.domain.AggregateRoot
import com.github.phanerozoicc.chat.application.event.ConversationCreatedEvent
import java.time.Instant


data class ConversationId(val value: String) {
    companion object {
        // 生成新的对话ID
        fun generate(): ConversationId {
            return ConversationId(java.util.UUID.randomUUID().toString())
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
    val createdAt: Instant,
    val updatedAt: Instant,
    private val messages: List<Message> = mutableListOf()
): AggregateRoot<ConversationId>(id) {
    
}



class ConversationFactory {
    companion object {
        fun create(
            userId: String,
            modelConfig: ModelConfiguration
        ) : Conversation {
            require(userId.isNotBlank()) {"对话关联的用户id不能为空"}
            val conversation = Conversation(
                ConversationId.generate(),
                userId,
                null,
                modelConfig,
                Instant.now(),
                Instant.now()
            )
            conversation.addDomainEvent(
                ConversationCreatedEvent(
                    conversationId = conversation.id.value,
                    conversation.userId
                )
            )
            return conversation
        }
    }
}