package com.lifee.knowledge.application.controllers

import com.lifee.common.cqrs.commands.CommandBus
import com.lifee.common.cqrs.queries.QueryBus
import com.lifee.knowledge.application.commands.CreateKnowledgeBaseCommand
import com.lifee.knowledge.application.commands.DeleteKnowledgeBaseCommand
import com.lifee.knowledge.application.dto.KnowledgeBaseDto
import com.lifee.knowledge.application.queries.GetKnowledgeBaseQuery
import com.lifee.knowledge.application.queries.GetUserKnowledgeBasesQuery
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 * 知识库REST控制器
 */
@RestController
@RequestMapping("/api/v1/knowledge-bases")
class KnowledgeBaseController(
    private val commandBus: CommandBus,
    private val queryBus: QueryBus
) {
    
    /**
     * 创建知识库
     */
    @PostMapping
    suspend fun createKnowledgeBase(
        @Valid @RequestBody request: CreateKnowledgeBaseRequest,
        @RequestHeader("X-User-Id") userId: String
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
     */
    @GetMapping("/{knowledgeBaseId}")
    suspend fun getKnowledgeBase(
        @PathVariable knowledgeBaseId: String,
        @RequestHeader("X-User-Id") userId: String
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
     */
    @GetMapping
    suspend fun getUserKnowledgeBases(
        @RequestHeader("X-User-Id") userId: String,
        @RequestParam(defaultValue = "0") offset: Int,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<KnowledgeBaseDto>> {
        val query = GetUserKnowledgeBasesQuery(userId, offset, limit)
        val result = queryBus.send<GetUserKnowledgeBasesQuery, List<KnowledgeBaseDto>>(query)
        
        return ResponseEntity.ok(result)
    }
    
    /**
     * 删除知识库
     */
    @DeleteMapping("/{knowledgeBaseId}")
    suspend fun deleteKnowledgeBase(
        @PathVariable knowledgeBaseId: String,
        @RequestHeader("X-User-Id") userId: String
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