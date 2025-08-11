package com.github.phanerozoicc.user.application.service

import com.github.phanerozoicc.base.command.CommandBus
import com.github.phanerozoicc.user.application.command.RegisterUserCommand
import com.github.phanerozoicc.user.interfaces.rest.RegisterUserRequest

class UserApplicationService(
    val commandBus: CommandBus,
) {


    /**
     * 注册流程
     */
    fun register(request: RegisterUserRequest, ipAddr: String, userAgent: String) {
        // 1. 封装注册command
        // 2. 使用命令总线发生command
        commandBus.send(registerCommand)
    }
}