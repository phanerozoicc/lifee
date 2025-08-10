package com.lifee.chat.app.web

import com.lifee.chat.app.application.commands.*
import com.lifee.chat.app.application.queries.*
import com.lifee.chat.app.application.dtos.*
import com.lifee.chat.app.application.handlers.*
import com.lifee.chat.domain.valueobjects.ConversationTitle
import com.lifee.chat.domain.valueobjects.MessageType
import com.lifee.common.valueobjects.UserId
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1/chat")
class ChatController(
    private val createConversationCommandHandler: CreateConversationCommandHandler,
    private val addMessageCommandHandler: AddMessageCommandHandler,
    private val updateConversationTitleCommandHandler: UpdateConversationTitleCommandHandler,
    private val deleteConversationCommandHandler: DeleteConversationCommandHandler,
    private val deleteMessageCommandHandler: DeleteMessageCommandHandler,
    private val getConversationQueryHandler: GetConversationQueryHandler,
    private val getUserConversationsQueryHandler: GetUserConversationsQueryHandler,
    private val getConversationMessagesQueryHandler: GetConversationMessagesQueryHandler,
    private val getMessageQueryHandler: GetMessageQueryHandler
) {

    @PostMapping("/conversations")
    suspend fun createConversation(
        @Valid @RequestBody request: CreateConversationRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ConversationDto> {
        val command = CreateConversationCommand(
            title = request.title?.let { ConversationTitle(it) },
            userId = UserId(UUID.fromString(userId))
        )
        val result = createConversationCommandHandler.handle(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @PostMapping("/conversations/{conversationId}/messages")
    suspend fun addMessage(
        @PathVariable conversationId: String,
        @Valid @RequestBody request: AddMessageRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<MessageDto> {
        val command = AddMessageCommand(
            conversationId = UUID.fromString(conversationId),
            content = request.content,
            type = MessageType.valueOf(request.type),
            userId = UserId(UUID.fromString(userId))
        )
        val result = addMessageCommandHandler.handle(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @PutMapping("/conversations/{conversationId}/title")
    suspend fun updateConversationTitle(
        @PathVariable conversationId: String,
        @Valid @RequestBody request: UpdateConversationTitleRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ConversationDto> {
        val command = UpdateConversationTitleCommand(
            conversationId = UUID.fromString(conversationId),
            newTitle = ConversationTitle(request.title),
            userId = UserId(UUID.fromString(userId))
        )
        val result = updateConversationTitleCommandHandler.handle(command)
        return ResponseEntity.ok(result)
    }

    @DeleteMapping("/conversations/{conversationId}")
    suspend fun deleteConversation(
        @PathVariable conversationId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<Unit> {
        val command = DeleteConversationCommand(
            conversationId = UUID.fromString(conversationId),
            userId = UserId(UUID.fromString(userId))
        )
        deleteConversationCommandHandler.handle(command)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping("/conversations/{conversationId}/messages/{messageId}")
    suspend fun deleteMessage(
        @PathVariable conversationId: String,
        @PathVariable messageId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<Unit> {
        val command = DeleteMessageCommand(
            conversationId = UUID.fromString(conversationId),
            messageId = UUID.fromString(messageId),
            userId = UserId(UUID.fromString(userId))
        )
        deleteMessageCommandHandler.handle(command)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/conversations/{conversationId}")
    suspend fun getConversation(
        @PathVariable conversationId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ConversationDetailDto> {
        val query = GetConversationQuery(
            conversationId = UUID.fromString(conversationId),
            userId = UserId(UUID.fromString(userId))
        )
        val result = getConversationQueryHandler.handle(query)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/conversations")
    suspend fun getUserConversations(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<ConversationPageDto> {
        val query = GetUserConversationsQuery(
            userId = UserId(UUID.fromString(userId)),
            page = page,
            size = size
        )
        val result = getUserConversationsQueryHandler.handle(query)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/conversations/{conversationId}/messages")
    suspend fun getConversationMessages(
        @PathVariable conversationId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<List<MessageDto>> {
        val query = GetConversationMessagesQuery(
            conversationId = UUID.fromString(conversationId),
            userId = UserId(UUID.fromString(userId))
        )
        val result = getConversationMessagesQueryHandler.handle(query)
        return ResponseEntity.ok(result)
    }

    @GetMapping("/conversations/{conversationId}/messages/{messageId}")
    suspend fun getMessage(
        @PathVariable conversationId: String,
        @PathVariable messageId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<MessageDto> {
        val query = GetMessageQuery(
            conversationId = UUID.fromString(conversationId),
            messageId = UUID.fromString(messageId),
            userId = UserId(UUID.fromString(userId))
        )
        val result = getMessageQueryHandler.handle(query)
        return ResponseEntity.ok(result)
    }
}

// Request DTOs
data class CreateConversationRequest(
    val title: String?
)

data class AddMessageRequest(
    val content: String,
    val type: String
)

data class UpdateConversationTitleRequest(
    val title: String
)