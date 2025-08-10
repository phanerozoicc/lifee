package com.lifee.recommendation.app.adapters.web.dto

import com.lifee.recommendation.domain.valueobjects.RecommendationType
import jakarta.validation.constraints.*

/**
 * 创建推荐请求
 */
data class CreateRecommendationRequest(
    @field:NotBlank(message = "用户ID不能为空")
    val userId: String
)

/**
 * 添加推荐项请求
 */
data class AddRecommendationItemRequest(
    @field:NotBlank(message = "内容ID不能为空")
    val contentId: String,
    
    @field:NotNull(message = "推荐分数不能为空")
    @field:DecimalMin(value = "0.0", message = "推荐分数不能小于0.0")
    @field:DecimalMax(value = "1.0", message = "推荐分数不能大于1.0")
    val score: Double,
    
    @field:NotNull(message = "推荐类型不能为空")
    val type: RecommendationType,
    
    @field:NotBlank(message = "推荐原因不能为空")
    @field:Size(max = 500, message = "推荐原因不能超过500个字符")
    val reason: String
)

/**
 * 更新分数请求
 */
data class UpdateScoreRequest(
    @field:NotNull(message = "分数不能为空")
    @field:DecimalMin(value = "0.0", message = "推荐分数不能小于0.0")
    @field:DecimalMax(value = "1.0", message = "推荐分数不能大于1.0")
    val score: Double
)

/**
 * 生成推荐请求
 */
data class GenerateRecommendationRequest(
    @field:NotBlank(message = "用户ID不能为空")
    val userId: String,
    
    @field:NotNull(message = "推荐类型不能为空")
    val type: RecommendationType,
    
    @field:Min(value = 1, message = "限制数量必须大于0")
    @field:Max(value = 50, message = "限制数量不能超过50")
    val limit: Int = 20
)