package com.github.phanerozoicc.user.interfaces.rest

import com.github.phanerozoicc.base.response.ApiResponse
import com.github.phanerozoicc.user.application.command.LoginUserCommand
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

class AuthController {

    /**
     * 用户登录
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginUserRequest): ResponseEntity<ApiResponse<String>> {
        return try {
            val loginCommand = LoginUserCommand(
                email = request.email,
                password = request.password,
                rememberMe = request.rememberMe,
            )
            ResponseEntity.ok(ApiResponse.success("登录成功", "用户登录成功"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<String>("登录失败", e.message))
        }
    }
}