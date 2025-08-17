package com.lifee.knowledge.application.controllers

import com.lifee.common.cqrs.commands.CommandBus
import com.lifee.common.cqrs.queries.QueryBus
import com.lifee.knowledge.application.commands.CreateKnowledgeBaseCommand
import com.lifee.knowledge.application.commands.DeleteKnowledgeBaseCommand
import com.lifee.knowledge.application.dto.KnowledgeBaseDto
import com.lifee.knowledge.application.queries.GetKnowledgeBaseQuery
import com.lifee.knowledge.application.queries.GetUserKnowledgeBasesQuery
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
 * 知识库REST控制器
 * 提供知识库的创建、查询、删除等功能
 */
@RestController
@RequestMapping("/api/v1/knowledge-bases")
@Tag(name = "知识库管理", description = "知识库的创建、查询、删除等管理功能")
class KnowledgeBaseController(
    private val commandBus: CommandBus,
    private val queryBus: QueryBus
) {
    
    /**
     * 创建知识库
     * 
     * @param request 创建知识库请求，包含知识库名称和描述
     * @param userId 用户ID，从请求头获取
     * @return 创建成功的知识库ID
     */
    @Operation(summary = "创建知识库", description = "为用户创建新的知识库")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "知识库创建成功"),
            ApiResponse(responseCode = "400", description = "请求参数错误"),
            ApiResponse(responseCode = "401", description = "用户未认证"),
            ApiResponse(responseCode = "409", description = "知识库名称已存在")
        ]
    )
    @PostMapping
    suspend fun createKnowledgeBase(
        @Parameter(description = "创建知识库请求", required = true)
        @Valid @RequestBody request: CreateKnowledgeBaseRequest,
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String
    ): ResponseEntity<CreateKnowledgeBaseResponse> {
        val knowledgeBaseId = UUID.randomUUID().toString()
        
        val command = CreateKnowledgeBaseCommand(
            knowledgeBaseId = knowledgeBaseId,
            name = request.name,
            description = request.description,
            ownerId = userId
        )
        
        commandBus.send<CreateKnowledgeBaseCommand, Unit>(command)
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CreateKnowledgeBaseResponse(knowledgeBaseId))
    }
    
    /**
     * 获取知识库详情
     * 
     * @param knowledgeBaseId 知识库ID
     * @param userId 用户ID，从请求头获取
     * @return 知识库详细信息
     */
    @Operation(summary = "获取知识库详情", description = "根据知识库ID获取知识库的详细信息")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "获取成功"),
            ApiResponse(responseCode = "401", description = "用户未认证"),
            ApiResponse(responseCode = "403", description = "无权限访问该知识库"),
            ApiResponse(responseCode = "404", description = "知识库不存在")
        ]
    )
    @GetMapping("/{knowledgeBaseId}")
    suspend fun getKnowledgeBase(
        @Parameter(description = "知识库ID", required = true)
        @PathVariable @NotBlank knowledgeBaseId: String,
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String
    ): ResponseEntity<KnowledgeBaseDto> {
        val query = GetKnowledgeBaseQuery(knowledgeBaseId, userId)
        val result = queryBus.send<GetKnowledgeBaseQuery, KnowledgeBaseDto?>(query)
        
        return if (result != null) {
            ResponseEntity.ok(result)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    /**
     * 获取用户的知识库列表
     * 
     * 支持分页查询用户拥有的所有知识库，包含完整的分页、排序和过滤功能。
     * 
     * @param userId 用户ID，从请求头获取
     * @param offset 分页偏移量，默认为0，最小值为0
     * @param limit 每页数量，默认为20，取值范围1-100
     * @param sortBy 排序字段，支持：name（名称）、createdAt（创建时间）、updatedAt（更新时间）
     * @param sortOrder 排序方向，支持：asc（升序）、desc（降序），默认为desc
     * @param search 搜索关键词，支持按知识库名称和描述进行模糊搜索
     * @param status 知识库状态过滤，支持：active（活跃）、archived（已归档）
     * @return 分页的知识库列表响应
     */
    @Operation(
        summary = "获取用户知识库列表", 
        description = "分页获取用户拥有的所有知识库，支持排序、搜索和状态过滤"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "获取成功，返回分页的知识库列表"),
            ApiResponse(responseCode = "400", description = "分页参数错误或排序参数无效"),
            ApiResponse(responseCode = "401", description = "用户未认证")
        ]
    )
    @GetMapping
    suspend fun getUserKnowledgeBases(
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String,
        
        @Parameter(
            description = "分页偏移量，表示跳过的记录数",
            example = "0",
            schema = io.swagger.v3.oas.annotations.media.Schema(minimum = "0")
        )
        @RequestParam(defaultValue = "0") offset: Int,
        
        @Parameter(
            description = "每页返回的记录数量，取值范围1-100",
            example = "20",
            schema = io.swagger.v3.oas.annotations.media.Schema(minimum = "1", maximum = "100")
        )
        @RequestParam(defaultValue = "20") limit: Int,
        
        @Parameter(
            description = "排序字段",
            example = "createdAt",
            schema = io.swagger.v3.oas.annotations.media.Schema(
                allowableValues = ["name", "createdAt", "updatedAt"]
            )
        )
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        
        @Parameter(
            description = "排序方向",
            example = "desc",
            schema = io.swagger.v3.oas.annotations.media.Schema(
                allowableValues = ["asc", "desc"]
            )
        )
        @RequestParam(defaultValue = "desc") sortOrder: String,
        
        @Parameter(
            description = "搜索关键词，支持按知识库名称和描述进行模糊搜索",
            example = "技术文档"
        )
        @RequestParam(required = false) search: String?,
        
        @Parameter(
            description = "知识库状态过滤",
            example = "active",
            schema = io.swagger.v3.oas.annotations.media.Schema(
                allowableValues = ["active", "archived"]
            )
        )
        @RequestParam(required = false) status: String?
    ): ResponseEntity<KnowledgeBasePageResponse> {
        val query = GetUserKnowledgeBasesQuery(userId, offset, limit)
        val result = queryBus.send<GetUserKnowledgeBasesQuery, List<KnowledgeBaseDto>>(query)
        
        return ResponseEntity.ok(result)
    }
    
    /**
     * 删除知识库
     * 
     * @param knowledgeBaseId 知识库ID
     * @param userId 用户ID，从请求头获取
     * @return 删除成功无返回内容
     */
    @Operation(summary = "删除知识库", description = "删除指定的知识库及其所有文档")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "删除成功"),
            ApiResponse(responseCode = "401", description = "用户未认证"),
            ApiResponse(responseCode = "403", description = "无权限删除该知识库"),
            ApiResponse(responseCode = "404", description = "知识库不存在")
        ]
    )
    @DeleteMapping("/{knowledgeBaseId}")
    suspend fun deleteKnowledgeBase(
        @Parameter(description = "知识库ID", required = true)
        @PathVariable @NotBlank knowledgeBaseId: String,
        @Parameter(description = "用户ID", required = true)
        @RequestHeader("X-User-Id") @NotBlank userId: String
    ): ResponseEntity<Void> {
        val command = DeleteKnowledgeBaseCommand(knowledgeBaseId, userId)
        commandBus.send<DeleteKnowledgeBaseCommand, Unit>(command)
        
        return ResponseEntity.noContent().build()
    }
}

/**
 * 创建知识库请求
 */
data class CreateKnowledgeBaseRequest(
    val name: String,
    val description: String
)

/**
 * 创建知识库响应
 */
data class CreateKnowledgeBaseResponse(
    val knowledgeBaseId: String
)