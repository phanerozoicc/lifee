package com.lifee.user.app.queries

import com.lifee.common.cqrs.queries.Query
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * 根据邮箱查询用户
 */
data class GetUserByEmailQuery(
    @field:NotBlank(message = "邮箱不能为空")
    @field:Email(message = "邮箱格式不正确")
    val email: String
) : Query