package com.lifee.user.app.commands

import com.lifee.shared.cqrs.Command

/**
 * 通过激活令牌激活用户命令
 */
data class ActivateUserByTokenCommand(
    val token: String
) : Command