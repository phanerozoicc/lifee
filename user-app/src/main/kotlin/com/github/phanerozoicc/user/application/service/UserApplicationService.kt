package com.github.phanerozoicc.user.application.service

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
        val registerCommand = RegisterUserCommand(
            email = request.email,
            password = request.password,
            nickname = request.nickname,
            firstName = request.firstName,
            lastName = request.lastName,
            acceptTerms = request.acceptTerms,
            marketingConsent = request.marketingConsent,
            ipAddress = ipAddr,
            userAgent = userAgent
        )
        // 2. 使用命令总线发生command
        commandBus.send(registerCommand)
    }
}