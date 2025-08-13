package com.lifee.recommendation.app.application.commands

import com.lifee.common.cqrs.commands.Command
import com.lifee.recommendation.domain.valueobjects.RecommendationId
import com.lifee.recommendation.domain.valueobjects.ContentId
import com.lifee.recommendation.domain.valueobjects.RecommendationType
import com.lifee.user.domain.UserId
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin

/**
 * 创建推荐命令
 */
data class CreateRecommendationCommand(
    @field:NotNull(message = "推荐ID不能为空")
    val recommendationId: RecommendationId,
    
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId
) : Command

/**
 * 添加推荐项命令
 */
data class AddRecommendationItemCommand(
    @field:NotNull(message = "推荐ID不能为空")
    val recommendationId: RecommendationId,
    
    @field:NotNull(message = "内容ID不能为空")
    val contentId: ContentId,
    
    @field:NotNull(message = "推荐分数不能为空")
    @field:DecimalMin(value = "0.0", message = "推荐分数不能小于0.0")
    @field:DecimalMax(value = "1.0", message = "推荐分数不能大于1.0")
    val score: Double,
    
    @field:NotNull(message = "推荐类型不能为空")
    val type: RecommendationType,
    
    @field:NotBlank(message = "推荐原因不能为空")
    val reason: String
) : Command

/**
 * 移除推荐项命令
 */
data class RemoveRecommendationItemCommand(
    @field:NotNull(message = "推荐ID不能为空")
    val recommendationId: RecommendationId,
    
    @field:NotNull(message = "内容ID不能为空")
    val contentId: ContentId
) : Command

/**
 * 更新推荐分数命令
 */
data class UpdateRecommendationScoreCommand(
    @field:NotNull(message = "推荐ID不能为空")
    val recommendationId: RecommendationId,
    
    @field:NotNull(message = "内容ID不能为空")
    val contentId: ContentId,
    
    @field:NotNull(message = "新分数不能为空")
    @field:DecimalMin(value = "0.0", message = "推荐分数不能小于0.0")
    @field:DecimalMax(value = "1.0", message = "推荐分数不能大于1.0")
    val newScore: Double
) : Command

/**
 * 清空推荐命令
 */
data class ClearRecommendationCommand(
    @field:NotNull(message = "推荐ID不能为空")
    val recommendationId: RecommendationId
) : Command

/**
 * 删除推荐命令
 */
data class DeleteRecommendationCommand(
    @field:NotNull(message = "推荐ID不能为空")
    val recommendationId: RecommendationId
) : Command

/**
 * 生成推荐命令
 */
data class GenerateRecommendationCommand(
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId,
    
    @field:NotNull(message = "推荐类型不能为空")
    val type: RecommendationType,
    
    val limit: Int = 20
) : Command