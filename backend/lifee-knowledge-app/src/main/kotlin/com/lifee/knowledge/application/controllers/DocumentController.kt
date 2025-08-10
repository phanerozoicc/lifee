package com.lifee.knowledge.application.controllers

import com.lifee.common.cqrs.commands.CommandBus
import com.lifee.common.cqrs.queries.QueryBus
import com.lifee.knowledge.application.commands.AddDocumentCommand
import com.lifee.knowledge.application.commands.RemoveDocumentCommand
import com.lifee.knowledge.application.commands.UpdateDocumentCommand
import com.lifee.knowledge.application.dto.DocumentDto
import com.lifee.knowledge.application.queries.GetDocumentQuery
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * 文档REST控制器
 */
@RestController
@RequestMapping("/api/v1/knowledge-bases/{knowledgeBaseId}/documents")
class DocumentController(
    private val commandBus: CommandBus,
    private val queryBus: QueryBus
) {
    
    /**
     * 添加文档
     */
    @PostMapping
    suspend fun addDocument(
        @PathVariable knowledgeBaseId: String,
        @Valid @RequestBody request: AddDocumentRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<AddDocumentResponse> {
        val documentId = UUID.randomUUID().toString()
        
        val command = AddDocumentCommand(
            knowledgeBaseId = knowledgeBaseId,
            documentId = documentId,
            title = request.title,
            content = request.content,
            type = request.type,
            userId = userId
        )
        
        commandBus.send<AddDocumentCommand, Unit>(command)
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AddDocumentResponse(documentId))
    }
    
    /**
     * 获取文档详情
     */
    @GetMapping("/{documentId}")
    suspend fun getDocument(
        @PathVariable knowledgeBaseId: String,
        @PathVariable documentId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<DocumentDto> {
        val query = GetDocumentQuery(knowledgeBaseId, documentId, userId)
        val result = queryBus.send<GetDocumentQuery, DocumentDto?>(query)
        
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * 更新文档
     */
    @PutMapping("/{documentId}")
    suspend fun updateDocument(
        @PathVariable knowledgeBaseId: String,
        @PathVariable documentId: String,
        @Valid @RequestBody request: UpdateDocumentRequest,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<Void> {
        val command = UpdateDocumentCommand(
            knowledgeBaseId = knowledgeBaseId,
            documentId = documentId,
            newTitle = request.title,
            newContent = request.content,
            userId = userId
        )
        
        commandBus.send<UpdateDocumentCommand, Unit>(command)
        
        return ResponseEntity.ok().build()
    }
    
    /**
     * 删除文档
     */
    @DeleteMapping("/{documentId}")
    suspend fun removeDocument(
        @PathVariable knowledgeBaseId: String,
        @PathVariable documentId: String,
        @RequestHeader("X-User-Id") userId: String
    ): ResponseEntity<Void> {
        val command = RemoveDocumentCommand(
            knowledgeBaseId = knowledgeBaseId,
            documentId = documentId,
            userId = userId
        )
        
        commandBus.send<RemoveDocumentCommand, Unit>(command)
        
        return ResponseEntity.noContent().build()
    }
}

/**
 * 添加文档请求
 */
data class AddDocumentRequest(
    val title: String,
    val content: String,
    val type: String
)

/**
 * 添加文档响应
 */
data class AddDocumentResponse(
    val documentId: String
)

/**
 * 更新文档请求
 */
data class UpdateDocumentRequest(
    val title: String,
    val content: String
)