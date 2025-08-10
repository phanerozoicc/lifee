package com.lifee.user.app.commands

import com.lifee.common.cqrs.commands.Command
import jakarta.validation.constraints.NotBlank

/**
 * 激活用户命令
 */
data class ActivateUserCommand(
    @field:NotBlank(message = "用户ID不能为空")
    val userId: String
) : Command