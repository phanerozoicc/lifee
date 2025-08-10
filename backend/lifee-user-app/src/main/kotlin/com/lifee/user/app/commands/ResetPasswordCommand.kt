package com.lifee.user.app.commands

import com.lifee.common.cqrs.commands.Command

/**
 * 重置密码命令
 */
data class ResetPasswordCommand(
    val token: String,
    val newPassword: String
) : Command