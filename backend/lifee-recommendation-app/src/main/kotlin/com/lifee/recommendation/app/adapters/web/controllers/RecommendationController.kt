package com.lifee.recommendation.app.adapters.web.controllers

import com.lifee.common.cqrs.commands.CommandBus
import com.lifee.common.cqrs.queries.QueryBus
import com.lifee.recommendation.app.application.commands.*
import com.lifee.recommendation.app.application.dtos.*
import com.lifee.recommendation.app.application.queries.*
import com.lifee.recommendation.domain.valueobjects.*
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 推荐控制器
 */
@RestController
@RequestMapping("/api/recommendations")
class RecommendationController(
    private val commandBus: CommandBus,
    private val queryBus: QueryBus
) {
    
    /**
     * 创建推荐
     */
    @PostMapping
    suspend fun createRecommendation(
        @Valid @RequestBody request: CreateRecommendationRequest
    ): ResponseEntity<Void> {
        val command = CreateRecommendationCommand(
            recommendationId = RecommendationId.generate(),
            userId = UserId.from(request.userId)
        )
        
        commandBus.send<CreateRecommendationCommand, Unit>(command)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
    
    /**
     * 获取推荐详情
     */
    @GetMapping("/{recommendationId}")
    suspend fun getRecommendation(
        @PathVariable recommendationId: String
    ): ResponseEntity<RecommendationDetailDto> {
        val query = GetRecommendationQuery(
            recommendationId = RecommendationId.from(recommendationId)
        )
        
        val result = queryBus.send<GetRecommendationQuery, RecommendationDetailDto>(query)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 获取用户推荐
     */
    @GetMapping("/users/{userId}")
    suspend fun getUserRecommendation(
        @PathVariable userId: String
    ): ResponseEntity<RecommendationDetailDto> {
        val query = GetUserRecommendationQuery(
            userId = UserId.from(userId)
        )
        
        val result = queryBus.send<GetUserRecommendationQuery, RecommendationDetailDto>(query)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 添加推荐项
     */
    @PostMapping("/{recommendationId}/items")
    suspend fun addRecommendationItem(
        @PathVariable recommendationId: String,
        @Valid @RequestBody request: AddRecommendationItemRequest
    ): ResponseEntity<Void> {
        val command = AddRecommendationItemCommand(
            recommendationId = RecommendationId.from(recommendationId),
            contentId = ContentId.from(request.contentId),
            score = request.score,
            type = request.type,
            reason = request.reason
        )
        
        commandBus.send<AddRecommendationItemCommand, Unit>(command)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
    
    /**
     * 获取推荐项列表
     */
    @GetMapping("/{recommendationId}/items")
    suspend fun getRecommendationItems(
        @PathVariable recommendationId: String,
        @RequestParam(required = false) type: RecommendationType?,
        @RequestParam(defaultValue = "20") limit: Int
    ): ResponseEntity<List<RecommendationItemDto>> {
        val query = GetRecommendationItemsQuery(
            recommendationId = RecommendationId.from(recommendationId),
            type = type,
            limit = limit
        )
        
        val result = queryBus.send<GetRecommendationItemsQuery, List<RecommendationItemDto>>(query)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 获取高质量推荐
     */
    @GetMapping("/users/{userId}/high-quality")
    suspend fun getHighQualityRecommendations(
        @PathVariable userId: String,
        @RequestParam(defaultValue = "10") limit: Int
    ): ResponseEntity<List<RecommendationItemDto>> {
        val query = GetHighQualityRecommendationsQuery(
            userId = UserId.from(userId),
            limit = limit
        )
        
        val result = queryBus.send<GetHighQualityRecommendationsQuery, List<RecommendationItemDto>>(query)
        return ResponseEntity.ok(result)
    }
    
    /**
     * 移除推荐项
     */
    @DeleteMapping("/{recommendationId}/items/{contentId}")
    suspend fun removeRecommendationItem(
        @PathVariable recommendationId: String,
        @PathVariable contentId: String
    ): ResponseEntity<Void> {
        val command = RemoveRecommendationItemCommand(
            recommendationId = RecommendationId.from(recommendationId),
            contentId = ContentId.from(contentId)
        )
        
        commandBus.send<RemoveRecommendationItemCommand, Unit>(command)
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 更新推荐分数
     */
    @PutMapping("/{recommendationId}/items/{contentId}/score")
    suspend fun updateRecommendationScore(
        @PathVariable recommendationId: String,
        @PathVariable contentId: String,
        @Valid @RequestBody request: UpdateScoreRequest
    ): ResponseEntity<Void> {
        val command = UpdateRecommendationScoreCommand(
            recommendationId = RecommendationId.from(recommendationId),
            contentId = ContentId.from(contentId),
            newScore = request.score
        )
        
        commandBus.send<UpdateRecommendationScoreCommand, Unit>(command)
        return ResponseEntity.ok().build()
    }
    
    /**
     * 清空推荐
     */
    @DeleteMapping("/{recommendationId}/items")
    suspend fun clearRecommendation(
        @PathVariable recommendationId: String
    ): ResponseEntity<Void> {
        val command = ClearRecommendationCommand(
            recommendationId = RecommendationId.from(recommendationId)
        )
        
        commandBus.send<ClearRecommendationCommand, Unit>(command)
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 删除推荐
     */
    @DeleteMapping("/{recommendationId}")
    suspend fun deleteRecommendation(
        @PathVariable recommendationId: String
    ): ResponseEntity<Void> {
        val command = DeleteRecommendationCommand(
            recommendationId = RecommendationId.from(recommendationId)
        )
        
        commandBus.send<DeleteRecommendationCommand, Unit>(command)
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 获取推荐统计
     */
    @GetMapping("/users/{userId}/stats")
    suspend fun getRecommendationStats(
        @PathVariable userId: String
    ): ResponseEntity<RecommendationStatsDto> {
        val query = GetRecommendationStatsQuery(
            userId = UserId.from(userId)
        )
        
        val result = queryBus.send<GetRecommendationStatsQuery, RecommendationStatsDto>(query)
        return ResponseEntity.ok(result)
    }
}