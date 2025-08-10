package com.lifee.user.app.commands

import com.lifee.common.cqrs.commands.Command

/**
 * 忘记密码命令
 */
data class ForgotPasswordCommand(
    val email: String
) : Command