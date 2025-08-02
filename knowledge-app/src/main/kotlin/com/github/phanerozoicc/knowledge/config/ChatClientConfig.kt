package com.github.phanerozoicc.knowledge.config

import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.embedding.EmbeddingModel
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties
import org.springframework.ai.openai.OpenAiChatModel
import org.springframework.ai.openai.OpenAiEmbeddingModel
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.pgvector.PgVectorStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
class ChatClientConfig(val openAiChatProperties: OpenAiChatProperties) {


    @Bean
    fun openAiChatClient(openAiChatModel: OpenAiChatModel): ChatClient {
        return ChatClient.builder(openAiChatModel).build()
    }

    @Bean
    fun tokenTextSplitter(): TokenTextSplitter = TokenTextSplitter()

    @Bean
    fun pgVectorStore(jdbcTemplate: JdbcTemplate, openAiEmbeddingModel: OpenAiEmbeddingModel): PgVectorStore {
        return PgVectorStore.builder(jdbcTemplate, openAiEmbeddingModel)
            .vectorTableName("document_vector_store")
            .maxDocumentBatchSize(1000)
            .build()
    }
}