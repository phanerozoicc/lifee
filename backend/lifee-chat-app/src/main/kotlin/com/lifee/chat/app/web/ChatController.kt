package com.lifee.chat.app.web

import com.lifee.chat.app.application.commands.*
import com.lifee.chat.app.application.queries.*
import com.lifee.chat.app.application.dtos.*
import com.lifee.chat.app.application.handlers.*
import com.lifee.chat.domain.valueobjects.ConversationId
import com.lifee.chat.domain.valueobjects.ConversationTitle
import com.lifee.chat.domain.valueobjects.MessageContent
import com.lifee.chat.domain.valueobjects.MessageId
import com.lifee.chat.domain.valueobjects.MessageType
import com.lifee.chat.domain.valueobjects.UserId
import com.lifee.chat.app.web.CreateConversationRequest
import com.lifee.chat.app.web.AddMessageRequest
import com.lifee.chat.app.web.UpdateConversationTitleRequest
import com.lifee.chat.app.application.commands.DeleteConversationCommand
import com.lifee.chat.app.application.commands.DeleteMessageCommand
import com.lifee.chat.app.application.commands.UpdateConversationTitleCommand
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
 * 对话控制器
 * 提供对话和消息的管理功能
 */
@RestController
@RequestMapping("/chat")
@Tag(name = "对话管理", description = "对话和消息的创建、查询、更新、删除功能")
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

    /**
     * 创建对话
     * 
     * @param request 创建对话请求，包含可选的对话标题
     * @param userId 用户ID，从请求头获取
     * @return 创建成功的对话信息
     */
    @Operation(summary = "创建对话", description = "为用户创建新的对话会话")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "对话创建成功"),
            ApiResponse(responseCode = "400", description = "请求参数错误"),
            ApiResponse(responseCode = "401", description = "用户未认证")
        ]
    )
    @PostMapping("/conversations")
    suspend fun createConversation(
        @Parameter(description = "创建对话请求", required = true)
        @Valid @RequestBody request: CreateConversationRequest,
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String
    ): ResponseEntity<ConversationDto> {
        val command = CreateConversationCommand(
            title = request.title?.let { ConversationTitle(it) },
            userId = UserId(UUID.fromString(userId))
        )
        val result = createConversationCommandHandler.handle(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    /**
     * 添加消息
     * 
     * @param conversationId 对话ID
     * @param request 添加消息请求，包含消息内容和类型
     * @param userId 用户ID，从请求头获取
     * @return 创建成功的消息信息
     */
    @Operation(summary = "添加消息", description = "向指定对话添加新消息")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "消息添加成功"),
            ApiResponse(responseCode = "400", description = "请求参数错误"),
            ApiResponse(responseCode = "401", description = "用户未认证"),
            ApiResponse(responseCode = "403", description = "无权限操作该对话"),
            ApiResponse(responseCode = "404", description = "对话不存在")
        ]
    )
    @PostMapping("/conversations/{conversationId}/messages")
    suspend fun addMessage(
        @Parameter(description = "对话ID", required = true)
        @PathVariable @NotBlank conversationId: String,
        @Parameter(description = "添加消息请求", required = true)
        @Valid @RequestBody request: AddMessageRequest,
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String
    ): ResponseEntity<MessageDto> {
        val command = AddMessageCommand(
            conversationId = ConversationId(UUID.fromString(conversationId)),
            content = MessageContent(request.content),
            type = MessageType.valueOf(request.type),
            userId = UserId(UUID.fromString(userId))
        )
        val result = addMessageCommandHandler.handle(command)
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    /**
     * 更新对话标题
     * 
     * @param conversationId 对话ID
     * @param request 更新对话标题请求，包含新的标题
     * @param userId 用户ID，从请求头获取
     * @return 更新后的对话信息
     */
    @Operation(summary = "更新对话标题", description = "更新指定对话的标题")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "标题更新成功"),
            ApiResponse(responseCode = "400", description = "请求参数错误"),
            ApiResponse(responseCode = "401", description = "用户未认证"),
            ApiResponse(responseCode = "403", description = "无权限操作该对话"),
            ApiResponse(responseCode = "404", description = "对话不存在")
        ]
    )
    @PutMapping("/conversations/{conversationId}/title")
    suspend fun updateConversationTitle(
        @Parameter(description = "对话ID", required = true)
        @PathVariable @NotBlank conversationId: String,
        @Parameter(description = "更新对话标题请求", required = true)
        @Valid @RequestBody request: UpdateConversationTitleRequest,
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String
    ): ResponseEntity<ConversationDto> {
        val command = UpdateConversationTitleCommand(
            conversationId = UUID.fromString(conversationId),
            newTitle = ConversationTitle(request.title),
            userId = UserId(UUID.fromString(userId))
        )
        val result = updateConversationTitleCommandHandler.handle(command)
        return ResponseEntity.ok(result)
    }

    /**
     * 删除对话
     * 
     * @param conversationId 对话ID
     * @param userId 用户ID，从请求头获取
     * @return 删除成功无返回内容
     */
    @Operation(summary = "删除对话", description = "删除指定的对话及其所有消息")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "删除成功"),
            ApiResponse(responseCode = "401", description = "用户未认证"),
            ApiResponse(responseCode = "403", description = "无权限删除该对话"),
            ApiResponse(responseCode = "404", description = "对话不存在")
        ]
    )
    @DeleteMapping("/conversations/{conversationId}")
    suspend fun deleteConversation(
        @Parameter(description = "对话ID", required = true)
        @PathVariable @NotBlank conversationId: String,
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String
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

    /**
     * 获取对话详情
     * 
     * @param conversationId 对话ID
     * @param userId 用户ID，从请求头获取
     * @return 对话详细信息
     */
    @Operation(summary = "获取对话详情", description = "根据对话ID获取对话的详细信息")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "获取成功"),
            ApiResponse(responseCode = "401", description = "用户未认证"),
            ApiResponse(responseCode = "403", description = "无权限访问该对话"),
            ApiResponse(responseCode = "404", description = "对话不存在")
        ]
    )
    @GetMapping("/conversations/{conversationId}")
    suspend fun getConversation(
        @Parameter(description = "对话ID", required = true)
        @PathVariable @NotBlank conversationId: String,
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String
    ): ResponseEntity<ConversationDetailDto> {
        val query = GetConversationQuery(
            conversationId = ConversationId(UUID.fromString(conversationId)),
            userId = UserId(UUID.fromString(userId))
        )
        val result = getConversationQueryHandler.handle(query)
        return ResponseEntity.ok(result)
    }

    /**
     * 获取用户对话列表
     * 
     * 支持分页查询用户的所有对话，包含完整的分页、排序和过滤功能。
     * 
     * @param page 页码，从0开始，默认为0，最小值为0
     * @param size 每页数量，默认为20，取值范围1-100
     * @param sortBy 排序字段，支持：createdAt（创建时间）、updatedAt（最后更新时间）、title（标题）
     * @param sortOrder 排序方向，支持：asc（升序）、desc（降序），默认为desc
     * @param search 搜索关键词，支持按对话标题进行模糊搜索
     * @param status 对话状态过滤，支持：active（活跃）、archived（已归档）
     * @param dateFrom 开始日期过滤，格式：yyyy-MM-dd
     * @param dateTo 结束日期过滤，格式：yyyy-MM-dd
     * @param userId 用户ID，从请求头获取
     * @return 分页的对话列表响应
     */
    @Operation(
        summary = "获取用户对话列表", 
        description = "分页获取用户的所有对话，支持排序、搜索、状态过滤和日期范围过滤"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "获取成功，返回分页的对话列表"),
            ApiResponse(responseCode = "400", description = "分页参数错误、排序参数无效或日期格式错误"),
            ApiResponse(responseCode = "401", description = "用户未认证")
        ]
    )
    @GetMapping("/conversations")
    suspend fun getUserConversations(
        @Parameter(
            description = "页码，从0开始",
            example = "0",
            schema = io.swagger.v3.oas.annotations.media.Schema(minimum = "0")
        )
        @RequestParam(defaultValue = "0") page: Int,
        
        @Parameter(
            description = "每页返回的记录数量，取值范围1-100",
            example = "20",
            schema = io.swagger.v3.oas.annotations.media.Schema(minimum = "1", maximum = "100")
        )
        @RequestParam(defaultValue = "20") size: Int,
        
        @Parameter(
            description = "排序字段",
            example = "updatedAt",
            schema = io.swagger.v3.oas.annotations.media.Schema(
                allowableValues = ["createdAt", "updatedAt", "title"]
            )
        )
        @RequestParam(defaultValue = "updatedAt") sortBy: String,
        
        @Parameter(
            description = "排序方向",
            example = "desc",
            schema = io.swagger.v3.oas.annotations.media.Schema(
                allowableValues = ["asc", "desc"]
            )
        )
        @RequestParam(defaultValue = "desc") sortOrder: String,
        
        @Parameter(
            description = "搜索关键词，支持按对话标题进行模糊搜索",
            example = "技术讨论"
        )
        @RequestParam(required = false) search: String?,
        
        @Parameter(
            description = "对话状态过滤",
            example = "active",
            schema = io.swagger.v3.oas.annotations.media.Schema(
                allowableValues = ["active", "archived"]
            )
        )
        @RequestParam(required = false) status: String?,
        
        @Parameter(
            description = "开始日期过滤，格式：yyyy-MM-dd",
            example = "2024-01-01"
        )
        @RequestParam(required = false) dateFrom: String?,
        
        @Parameter(
            description = "结束日期过滤，格式：yyyy-MM-dd",
            example = "2024-12-31"
        )
        @RequestParam(required = false) dateTo: String?,
        
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String
    ): ResponseEntity<ConversationPageDto> {
        val query = GetUserConversationsQuery(
            userId = UserId(UUID.fromString(userId)),
            page = page,
            size = size
        )
        val result = getUserConversationsQueryHandler.handle(query)
        return ResponseEntity.ok(result)
    }

    /**
     * 获取对话消息列表
     * 
     * @param conversationId 对话ID
     * @param userId 用户ID，从请求头获取
     * @return 对话中的所有消息
     */
    @Operation(summary = "获取对话消息列表", description = "获取指定对话中的所有消息")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "获取成功"),
            ApiResponse(responseCode = "401", description = "用户未认证"),
            ApiResponse(responseCode = "403", description = "无权限访问该对话"),
            ApiResponse(responseCode = "404", description = "对话不存在")
        ]
    )
    @GetMapping("/conversations/{conversationId}/messages")
    suspend fun getConversationMessages(
        @Parameter(description = "对话ID", required = true)
        @PathVariable @NotBlank conversationId: String,
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String
    ): ResponseEntity<List<MessageDto>> {
        val query = GetConversationMessagesQuery(
            conversationId = ConversationId(UUID.fromString(conversationId)),
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
            conversationId = ConversationId(UUID.fromString(conversationId)),
            messageId = MessageId(UUID.fromString(messageId)),
            userId = UserId(UUID.fromString(userId))
        )
        val result = getMessageQueryHandler.handle(query)
        return ResponseEntity.ok(result)
    }
}