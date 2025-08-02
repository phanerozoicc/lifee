package com.github.phanerozoicc.knowledge.domain.conversation

import org.springframework.stereotype.Service

class CreateChatCmd()

@Service
class CreateChatCmdHandler(
    private val qaChatFactory: QAChatFactory,
    private val qaChatRepository: QAChatRepository,
) {
    fun handle(cmd: CreateChatCmd): String {
        val qaChat = qaChatFactory.create(cmd)
        qaChatRepository.save(qaChat)
        return qaChat.
    }
}
