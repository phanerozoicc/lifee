package com.lifee.user.app.queries

import com.lifee.common.cqrs.queries.Query
import jakarta.validation.constraints.NotBlank

/**
 * 根据ID查询用户
 */
data class GetUserByIdQuery(
    @field:NotBlank(message = "用户ID不能为空")
    val userId: String
) : Query