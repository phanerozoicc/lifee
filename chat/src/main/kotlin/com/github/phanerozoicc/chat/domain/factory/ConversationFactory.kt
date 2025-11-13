package com.github.phanerozoicc.chat.domain.factory

import com.github.phanerozoicc.chat.application.event.ConversationCreatedEvent
import com.github.phanerozoicc.chat.domain.model.Conversation
import com.github.phanerozoicc.chat.domain.model.ConversationId
import com.github.phanerozoicc.chat.domain.model.ModelConfiguration
import com.github.phanerozoicc.chat.domain.repository.ConversationRepository
import org.springframework.stereotype.Component

@Component
class ConversationFactory(
    private val conversationRepository: ConversationRepository
) {
    companion object {
        fun create(
            userId: String,
            modelConfig: ModelConfiguration
        ) : Conversation {
            require(userId.isNotBlank()) {"对话关联的用户id不能为空"}

            // 每个用户只能有一个新建的对话
            // 如果已经存在, 直接复用 否则创建新的
            val findNewByUserId = conversationRepository.findNewByUserId(userId)



            val conversation = Conversation(
                ConversationId.generate(),
                userId,
                null,
                modelConfig,
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
