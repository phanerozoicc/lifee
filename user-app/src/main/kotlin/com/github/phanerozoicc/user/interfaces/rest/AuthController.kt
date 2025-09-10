package com.github.phanerozoicc.user.interfaces.rest

import com.github.phanerozoicc.base.command.CommandBus
import com.github.phanerozoicc.base.queries.QueryBus
import com.github.phanerozoicc.base.response.ApiResponse
import com.github.phanerozoicc.user.application.command.ChangePasswordCommand
import com.github.phanerozoicc.user.application.command.LoginResponse
import com.github.phanerozoicc.user.application.command.LoginUserCommand
import com.github.phanerozoicc.user.application.command.RegisterUserCommand
import com.github.phanerozoicc.user.application.service.UserApplicationService
import com.github.phanerozoicc.user.domain.model.UserId
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@Controller("/api/v1/auth")
class AuthController(
    private val commandBus: CommandBus,
    private val userApplicationService: UserApplicationService,
    private val queryBus: QueryBus,
) {

    companion object: KLogging()


    /**
     * 用户注册
     */
    // TODO 添加网关服务
    //  从网关获取用户IP和UserAgent
    @Operation(summary = "用户注册", description = "创建新用户账户")
    @PostMapping("/register")
    suspend fun register(@Valid @RequestBody registerRequest: RegisterUserRequest,
                         @RequestHeader("X-User-Agent") userAgent: String,
                         @RequestHeader("X-Forwarded-For") remoteIp: String
    ): ResponseEntity<ApiResponse<Unit>> {
        val registerCommand = RegisterUserCommand(
            email = registerRequest.email,
            password = registerRequest.password,
            nickname = registerRequest.nickname,
            firstName = registerRequest.firstName,
            lastName = registerRequest.lastName,
            acceptTerms = registerRequest.acceptTerms,
            marketingConsent = registerRequest.marketingConsent,
            ipAddress = remoteIp,
            userAgent = userAgent
        )
        return try {
            // 事件都用同步处理(一般)
            commandBus.sendAndWait<RegisterUserCommand, Unit>(registerCommand)
            ResponseEntity.ok(ApiResponse.success("用户注册成功，请检查邮箱进行激活"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("用户注册失败", e.message))
        }
    }



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

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    fun refreshToken(
        @RequestBody request: Map<String, String>
    ): ApiResponse<Map<String, Any>> {
        val refreshToken = request["refreshToken"]
            ?: throw IllegalArgumentException("刷新令牌不能为空")
        try {
            val authResult = userApplicationService.refreshToken(refreshToken)
            return ApiResponse.success(mapOf("token" to authResult), "令牌刷新成功")
        } catch (e: Exception) {
            return ApiResponse.error("令牌刷新失败", e.message)
        }
    }



    @PutMapping("/me/password")
    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    @PreAuthorize("isAuthenticated()")
    suspend fun changePassword(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: ChangePasswordRequest
    ):ResponseEntity<ApiResponse<Unit>> {
        val userId = userDetails.username
        logger.info("修改用户密码请求 userId：{}", userId)
        val changePasswordCommand = ChangePasswordCommand(
            userId = UserId.of(userId),
            currentPassword = request.currentPassword,
            newPassword = request.newPassword
        )
        commandBus.sendAndWait<ChangePasswordCommand, Unit>(changePasswordCommand)
        return ResponseEntity.ok(ApiResponse.success("密码修改成功"))
    }


}