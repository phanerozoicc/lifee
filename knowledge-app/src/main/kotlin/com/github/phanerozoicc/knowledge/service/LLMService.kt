package com.github.phanerozoicc.knowledge.service

import com.github.phanerozoicc.knowledge.domain.conversation.ChatModel
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.messages.UserMessage
import org.springframework.ai.chat.model.ChatResponse
import org.springframework.ai.chat.prompt.Prompt
import org.springframework.ai.openai.OpenAiChatOptions
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class LLMService(val chatClient: ChatClient) {


    fun generateResponse(userInput: String, model: ChatModel): ChatResponse? {
        val prompt = Prompt.builder().content(userInput)
            .chatOptions(OpenAiChatOptions.builder().model(model.getModelName()).build())
            .build()
        return chatClient.prompt(prompt).call().chatResponse()
    }


    fun generateResponseStream(userInput: String, model: ChatModel, ragTag: String?=null): Flux<ChatResponse> {
        val prompt = Prompt.builder().messages(UserMessage(userInput))
            .chatOptions(
                OpenAiChatOptions.builder()
                    .model(model.getModelName()).streamUsage(true).build()
            )
            .build()
        return chatClient.prompt(prompt).stream().chatResponse()
    }

}