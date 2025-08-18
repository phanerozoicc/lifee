package com.github.phanerozoicc.knowledge.domain.conversation

import com.github.phanerozoicc.base.domain.AggregateRoot
import com.github.phanerozoicc.base.event.Event
import com.github.phanerozoicc.knowledge.domain.knowledgebase.KnowledgeBaseId
import com.github.phanerozoicc.knowledge.domain.knowledgebase.UserId
import com.github.phanerozoicc.knowledge.domain.document.DocumentChunk
import java.time.Duration
import java.time.Instant
import java.util.*

@JvmInline
value class ConversationId(val value: String) {
    companion object {
        fun generate(): ConversationId = ConversationId(UUID.randomUUID().toString())
    }
}

@JvmInline
value class MessageId(val value: String) {
    companion object {
        fun generate(): MessageId = MessageId(UUID.randomUUID().toString())
    }
}

enum class MessageRole {
    USER, ASSISTANT, SYSTEM
}

data class ConversationSettings(
    val llmSettings: LLMSettings = LLMSettings(),
    val ragSettings: RAGSettings = RAGSettings(),
    val autoSave: Boolean = true
)

data class LLMSettings(
    val model: String = "gpt-3.5-turbo",
    val temperature: Double = 0.7,
    val maxTokens: Int = 2000,
    val systemPrompt: String = "You are a helpful AI assistant."
)

data class RAGSettings(
    val similarityThreshold: Double = 0.7,
    val maxResults: Int = 5,
    val enableRAG: Boolean = true
)

data class RAGContext(
    val query: String,
    val retrievedChunks: List<DocumentChunk>,
    val totalChunks: Int,
    val retrievalTime: Duration
)

data class LLMMetadata(
    val model: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val finishReason: String
)

data class ChatMessage(
    val id: MessageId,
    val conversationId: ConversationId,
    val role: MessageRole,
    val content: String,
    val sequenceNumber: Int,
    val timestamp: Instant = Instant.now(),
    val ragContext: RAGContext? = null,
    val llmMetadata: LLMMetadata? = null
) {
    companion object {
        fun userMessage(conversationId: ConversationId, content: String, sequenceNumber: Int): ChatMessage {
            return ChatMessage(
                id = MessageId.generate(),
                conversationId = conversationId,
                role = MessageRole.USER,
                content = content,
                sequenceNumber = sequenceNumber
            )
        }
        
        fun assistantMessage(
            conversationId: ConversationId,
            content: String,
            sequenceNumber: Int,
            ragContext: RAGContext? = null,
            llmMetadata: LLMMetadata? = null
        ): ChatMessage {
            return ChatMessage(
                id = MessageId.generate(),
                conversationId = conversationId,
                role = MessageRole.ASSISTANT,
                content = content,
                sequenceNumber = sequenceNumber,
                ragContext = ragContext,
                llmMetadata = llmMetadata
            )
        }
        
        fun systemMessage(conversationId: ConversationId, content: String, sequenceNumber: Int): ChatMessage {
            return ChatMessage(
                id = MessageId.generate(),
                conversationId = conversationId,
                role = MessageRole.SYSTEM,
                content = content,
                sequenceNumber = sequenceNumber
            )
        }
    }
}

class Conversation(
    private val id: ConversationId,
    val userId: UserId,
    var knowledgeBaseId: KnowledgeBaseId? = null,
    var title: String,
    var settings: ConversationSettings = ConversationSettings(),
    private val messages: MutableList<ChatMessage> = mutableListOf(),
    var createdAt: Instant = Instant.now(),
    var updatedAt: Instant = Instant.now()
) : AggregateRoot<ConversationId>() {
    
    override fun getId(): ConversationId = id
    
    fun addUserMessage(content: String): ChatMessage {
        val message = ChatMessage.userMessage(id, content, messages.size + 1)
        messages.add(message)
        updatedAt = Instant.now()
        publish(MessageAddedEvent(id, message.id, MessageRole.USER))
        return message
    }
    
    fun addAssistantMessage(
        content: String,
        ragContext: RAGContext? = null,
        llmMetadata: LLMMetadata? = null
    ): ChatMessage {
        val message = ChatMessage.assistantMessage(
            conversationId = id,
            content = content,
            sequenceNumber = messages.size + 1,
            ragContext = ragContext,
            llmMetadata = llmMetadata
        )
        
        messages.add(message)
        updatedAt = Instant.now()
        publish(MessageAddedEvent(id, message.id, MessageRole.ASSISTANT))
        return message
    }
    
    fun addSystemMessage(content: String): ChatMessage {
        val message = ChatMessage.systemMessage(id, content, messages.size + 1)
        messages.add(message)
        updatedAt = Instant.now()
        publish(MessageAddedEvent(id, message.id, MessageRole.SYSTEM))
        return message
    }
    
    fun updateSettings(newSettings: ConversationSettings) {
        this.settings = newSettings
        this.updatedAt = Instant.now()
        publish(ConversationSettingsUpdatedEvent(id, newSettings))
    }
    
    fun changeKnowledgeBase(newKnowledgeBaseId: KnowledgeBaseId?) {
        val oldKnowledgeBaseId = this.knowledgeBaseId
        this.knowledgeBaseId = newKnowledgeBaseId
        this.updatedAt = Instant.now()
        publish(ConversationKnowledgeBaseChangedEvent(id, oldKnowledgeBaseId, newKnowledgeBaseId))
    }
    
    fun updateTitle(newTitle: String) {
        if (newTitle.isBlank()) {
            throw IllegalArgumentException("Conversation title cannot be blank")
        }
        this.title = newTitle
        this.updatedAt = Instant.now()
        publish(ConversationTitleUpdatedEvent(id, newTitle))
    }
    
    fun exportToMarkdown(): String {
        val sb = StringBuilder()
        sb.appendLine("# $title")
        sb.appendLine()
        sb.appendLine("**Created:** $createdAt")
        sb.appendLine("**Updated:** $updatedAt")
        if (knowledgeBaseId != null) {
            sb.appendLine("**Knowledge Base:** ${knowledgeBaseId!!.value}")
        }
        sb.appendLine()
        sb.appendLine("---")
        sb.appendLine()
        
        messages.sortedBy { it.sequenceNumber }.forEach { message ->
            when (message.role) {
                MessageRole.USER -> {
                    sb.appendLine("## 👤 User")
                    sb.appendLine()
                    sb.appendLine(message.content)
                    sb.appendLine()
                }
                MessageRole.ASSISTANT -> {
                    sb.appendLine("## 🤖 Assistant")
                    sb.appendLine()
                    sb.appendLine(message.content)
                    
                    // Add RAG context if available
                    message.ragContext?.let { rag ->
                        if (rag.retrievedChunks.isNotEmpty()) {
                            sb.appendLine()
                            sb.appendLine("### 📚 Knowledge Sources")
                            rag.retrievedChunks.take(3).forEachIndexed { index, chunk ->
                                sb.appendLine("${index + 1}. Document: ${chunk.documentId.value}")
                                sb.appendLine("   Content: ${chunk.content.take(100)}...")
                                sb.appendLine()
                            }
                        }
                    }
                    sb.appendLine()
                }
                MessageRole.SYSTEM -> {
                    sb.appendLine("## ⚙️ System")
                    sb.appendLine()
                    sb.appendLine(message.content)
                    sb.appendLine()
                }
            }
        }
        
        return sb.toString()
    }
    
    fun getMessages(): List<ChatMessage> = messages.toList()
    fun getMessageCount(): Int = messages.size
    fun getLastUserMessage(): ChatMessage? = messages.lastOrNull { it.role == MessageRole.USER }
    fun getLastAssistantMessage(): ChatMessage? = messages.lastOrNull { it.role == MessageRole.ASSISTANT }
}

// Domain Events
data class MessageAddedEvent(
    val conversationId: ConversationId,
    val messageId: MessageId,
    val role: MessageRole,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "MessageAdded"
) : Event

data class ConversationCreatedEvent(
    val conversationId: ConversationId,
    val userId: UserId,
    val title: String,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "ConversationCreated"
) : Event

data class ConversationSettingsUpdatedEvent(
    val conversationId: ConversationId,
    val newSettings: ConversationSettings,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "ConversationSettingsUpdated"
) : Event

data class ConversationKnowledgeBaseChangedEvent(
    val conversationId: ConversationId,
    val oldKnowledgeBaseId: KnowledgeBaseId?,
    val newKnowledgeBaseId: KnowledgeBaseId?,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "ConversationKnowledgeBaseChanged"
) : Event

data class ConversationTitleUpdatedEvent(
    val conversationId: ConversationId,
    val newTitle: String,
    override val eventId: String = UUID.randomUUID().toString(),
    override val occurredOn: Instant = Instant.now(),
    override val eventType: String = "ConversationTitleUpdated"
) : Event