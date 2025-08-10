package com.lifee.user.app.commands

import com.lifee.common.cqrs.commands.Command
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 更改密码命令
 */
data class ChangePasswordCommand(
    @field:NotBlank(message = "用户ID不能为空")
    val userId: String,
    
    @field:NotBlank(message = "当前密码不能为空")
    val currentPassword: String,
    
    @field:NotBlank(message = "新密码不能为空")
    @field:Size(min = 8, max = 128, message = "新密码长度必须在8-128位之间")
    val newPassword: String
) : Command