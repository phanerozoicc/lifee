package com.lifee.user.app.commands

import com.lifee.common.cqrs.commands.Command
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 用户登录命令
 */
data class LoginCommand(
    @field:NotBlank(message = "邮箱不能为空")
    @field:Email(message = "邮箱格式不正确")
    val email: String,
    
    @field:NotBlank(message = "密码不能为空")
    @field:Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
    val password: String,
    
    val ipAddress: String? = null,
    val userAgent: String? = null
) : Command