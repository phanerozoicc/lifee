package com.lifee.knowledge.application.controllers

import com.lifee.common.cqrs.commands.CommandBus
import com.lifee.common.cqrs.queries.QueryBus
import com.lifee.knowledge.application.commands.AddDocumentCommand
import com.lifee.knowledge.application.commands.RemoveDocumentCommand
import com.lifee.knowledge.application.commands.UpdateDocumentCommand
import com.lifee.knowledge.application.dto.DocumentDto
import com.lifee.knowledge.application.queries.GetDocumentQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * 文档REST控制器
 * 提供知识库文档的增删改查功能
 */
@RestController
@RequestMapping("/api/v1/knowledge-bases/{knowledgeBaseId}/documents")
@Tag(name = "文档管理", description = "知识库文档的增删改查功能")
class DocumentController(
    private val commandBus: CommandBus,
    private val queryBus: QueryBus
) {
    
    /**
     * 添加文档
     * 
     * @param knowledgeBaseId 知识库ID
     * @param request 添加文档请求，包含标题、内容和类型
     * @param userId 用户ID，从请求头获取
     * @return 创建成功的文档ID
     */
    @Operation(summary = "添加文档", description = "向指定知识库添加新文档")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "文档添加成功"),
            ApiResponse(responseCode = "400", description = "请求参数错误"),
            ApiResponse(responseCode = "401", description = "用户未认证"),
            ApiResponse(responseCode = "403", description = "无权限操作该知识库"),
            ApiResponse(responseCode = "404", description = "知识库不存在")
        ]
    )
    @PostMapping
    suspend fun addDocument(
        @Parameter(description = "知识库ID", required = true)
        @PathVariable @NotBlank knowledgeBaseId: String,
        @Parameter(description = "添加文档请求", required = true)
        @Valid @RequestBody request: AddDocumentRequest,
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String
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
     * 
     * @param knowledgeBaseId 知识库ID
     * @param documentId 文档ID
     * @param userId 用户ID，从请求头获取
     * @return 文档详细信息
     */
    @Operation(summary = "获取文档详情", description = "根据文档ID获取文档的详细信息")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "获取成功"),
            ApiResponse(responseCode = "401", description = "用户未认证"),
            ApiResponse(responseCode = "403", description = "无权限访问该文档"),
            ApiResponse(responseCode = "404", description = "文档不存在")
        ]
    )
    @GetMapping("/{documentId}")
    suspend fun getDocument(
        @Parameter(description = "知识库ID", required = true)
        @PathVariable @NotBlank knowledgeBaseId: String,
        @Parameter(description = "文档ID", required = true)
        @PathVariable @NotBlank documentId: String,
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String
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
     * 
     * @param knowledgeBaseId 知识库ID
     * @param documentId 文档ID
     * @param request 更新文档请求，包含新的标题和内容
     * @param userId 用户ID，从请求头获取
     * @return 更新成功无返回内容
     */
    @Operation(summary = "更新文档", description = "更新指定文档的标题和内容")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "更新成功"),
            ApiResponse(responseCode = "400", description = "请求参数错误"),
            ApiResponse(responseCode = "401", description = "用户未认证"),
            ApiResponse(responseCode = "403", description = "无权限操作该文档"),
            ApiResponse(responseCode = "404", description = "文档不存在")
        ]
    )
    @PutMapping("/{documentId}")
    suspend fun updateDocument(
        @Parameter(description = "知识库ID", required = true)
        @PathVariable @NotBlank knowledgeBaseId: String,
        @Parameter(description = "文档ID", required = true)
        @PathVariable @NotBlank documentId: String,
        @Parameter(description = "更新文档请求", required = true)
        @Valid @RequestBody request: UpdateDocumentRequest,
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String
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
     * 
     * @param knowledgeBaseId 知识库ID
     * @param documentId 文档ID
     * @param userId 用户ID，从请求头获取
     * @return 删除成功无返回内容
     */
    @Operation(summary = "删除文档", description = "从知识库中删除指定文档")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "删除成功"),
            ApiResponse(responseCode = "401", description = "用户未认证"),
            ApiResponse(responseCode = "403", description = "无权限删除该文档"),
            ApiResponse(responseCode = "404", description = "文档不存在")
        ]
    )
    @DeleteMapping("/{documentId}")
    suspend fun removeDocument(
        @Parameter(description = "知识库ID", required = true)
        @PathVariable @NotBlank knowledgeBaseId: String,
        @Parameter(description = "文档ID", required = true)
        @PathVariable @NotBlank documentId: String,
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String
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