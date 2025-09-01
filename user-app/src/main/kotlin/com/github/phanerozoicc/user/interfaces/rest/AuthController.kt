package com.github.phanerozoicc.user.interfaces.rest

import com.github.phanerozoicc.base.command.CommandBus
import com.github.phanerozoicc.base.response.ApiResponse
import com.github.phanerozoicc.user.application.command.LoginResponse
import com.github.phanerozoicc.user.application.command.LoginUserCommand
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@Controller("/api/v1/auth")
class AuthController(
    private val commandBus: CommandBus,
    bus: CommandBus
) {

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户身份验证")
    suspend fun login(
        @Valid @RequestBody request: LoginUserRequest,
        @RequestHeader("X-User-Agent") userAgent: String,
        @RequestHeader("X-Forwarded-For") remoteIp: String
    ): ResponseEntity<ApiResponse<LoginResponse>> {
        return try {
            val loginCommand = LoginUserCommand(
                email = request.email,
                password = request.password,
                rememberMe = request.rememberMe,
                ipAddress = remoteIp,
                userAgent = userAgent
            )

            val loginResponse = commandBus.sendAndWait<LoginUserCommand, LoginResponse>(loginCommand)
            ResponseEntity.ok(ApiResponse.success(loginResponse, "用户登录成功"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<LoginResponse>("登录失败", e.message))
        }
    }


}