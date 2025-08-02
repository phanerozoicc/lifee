package com.github.phanerozoicc.knowledge.domain.conversation

import org.springframework.stereotype.Service

class RaiseQuestionCmd(
    val sessionId: String,
    val question: String,
    val model: ChatModel
)

@Service
class RaiseQuestionCmdHandler(
//    private val llmService: LLMService
) {
//    fun handle(cmd: RaiseQuestionCmd): String {
//    }
}
//
//
//interface LLMService {
//    suspend fun askQuestion(question: String)
//}


// startANewConversation