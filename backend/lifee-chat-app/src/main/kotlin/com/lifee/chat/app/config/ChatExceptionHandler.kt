package com.lifee.chat.app.config

import com.lifee.chat.domain.exceptions.*
import com.lifee.common.exceptions.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class ChatExceptionHandler {

    @ExceptionHandler(ConversationNotFoundException::class)
    fun handleConversationNotFound(ex: ConversationNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "CONVERSATION_NOT_FOUND",
            message = ex.message ?: "Conversation not found",
            timestamp = LocalDateTime.now(),
            path = "/api/v1/chat",
            status = HttpStatus.NOT_FOUND.value()
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(MessageNotFoundException::class)
    fun handleMessageNotFound(ex: MessageNotFoundException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "MESSAGE_NOT_FOUND",
            message = ex.message ?: "Message not found",
            timestamp = LocalDateTime.now(),
            path = "/api/v1/chat",
            status = HttpStatus.NOT_FOUND.value()
        )
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(UnauthorizedConversationAccessException::class)
    fun handleUnauthorizedAccess(ex: UnauthorizedConversationAccessException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "UNAUTHORIZED_ACCESS",
            message = ex.message ?: "Unauthorized access to conversation",
            timestamp = LocalDateTime.now(),
            path = "/api/v1/chat",
            status = HttpStatus.FORBIDDEN.value()
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    @ExceptionHandler(ConversationCapacityExceededException::class)
    fun handleConversationCapacityExceeded(ex: ConversationCapacityExceededException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "CONVERSATION_CAPACITY_EXCEEDED",
            message = ex.message ?: "Conversation capacity exceeded",
            timestamp = LocalDateTime.now(),
            path = "/api/v1/chat",
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(InvalidMessageContentException::class)
    fun handleInvalidMessageContent(ex: InvalidMessageContentException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "INVALID_MESSAGE_CONTENT",
            message = ex.message ?: "Invalid message content",
            timestamp = LocalDateTime.now(),
            path = "/api/v1/chat",
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(UnsupportedMessageTypeException::class)
    fun handleUnsupportedMessageType(ex: UnsupportedMessageTypeException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "UNSUPPORTED_MESSAGE_TYPE",
            message = ex.message ?: "Unsupported message type",
            timestamp = LocalDateTime.now(),
            path = "/api/v1/chat",
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(InvalidConversationTitleException::class)
    fun handleInvalidConversationTitle(ex: InvalidConversationTitleException): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "INVALID_CONVERSATION_TITLE",
            message = ex.message ?: "Invalid conversation title",
            timestamp = LocalDateTime.now(),
            path = "/api/v1/chat",
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        val errorResponse = ErrorResponse(
            code = "VALIDATION_FAILED",
            message = errors.joinToString(", "),
            timestamp = LocalDateTime.now(),
            path = "/api/v1/chat",
            status = HttpStatus.BAD_REQUEST.value()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            code = "INTERNAL_SERVER_ERROR",
            message = "An unexpected error occurred",
            timestamp = LocalDateTime.now(),
            path = "/api/v1/chat",
            status = HttpStatus.INTERNAL_SERVER_ERROR.value()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }
}