package com.lifee.recommendation.app.application.queries

import com.lifee.common.cqrs.queries.Query
import com.lifee.recommendation.domain.valueobjects.*
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Min

/**
 * 获取推荐查询
 */
data class GetRecommendationQuery(
    @field:NotNull(message = "推荐ID不能为空")
    val recommendationId: RecommendationId
) : Query

/**
 * 获取用户推荐查询
 */
data class GetUserRecommendationQuery(
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId
) : Query

/**
 * 获取推荐项查询
 */
data class GetRecommendationItemsQuery(
    @field:NotNull(message = "推荐ID不能为空")
    val recommendationId: RecommendationId,
    
    val type: RecommendationType? = null,
    
    @field:Min(value = 1, message = "限制数量必须大于0")
    val limit: Int = 20
) : Query

/**
 * 获取高质量推荐查询
 */
data class GetHighQualityRecommendationsQuery(
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId,
    
    @field:Min(value = 1, message = "限制数量必须大于0")
    val limit: Int = 10
) : Query

/**
 * 获取推荐历史查询
 */
data class GetRecommendationHistoryQuery(
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId,
    
    @field:Min(value = 0, message = "页码不能小于0")
    val page: Int = 0,
    
    @field:Min(value = 1, message = "页面大小必须大于0")
    val size: Int = 20
) : Query

/**
 * 获取推荐统计查询
 */
data class GetRecommendationStatsQuery(
    @field:NotNull(message = "用户ID不能为空")
    val userId: UserId
) : Query