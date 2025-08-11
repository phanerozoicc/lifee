package com.lifee.user.app.controllers

import com.lifee.common.cqrs.commands.CommandBus
import com.lifee.user.app.commands.LoginCommand
import com.lifee.user.app.dto.LoginResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/v1/auth")
@Validated
@Tag(name = "用户认证", description = "用户登录、令牌刷新等认证功能")
class AuthController(
    private val commandBus: CommandBus
) {
    
    private val logger = LoggerFactory.getLogger(AuthController::class.java)
    
    @Operation(summary = "用户登录", description = "用户邮箱密码登录")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "登录成功"),
            ApiResponse(responseCode = "400", description = "请求参数错误"),
            ApiResponse(responseCode = "401", description = "用户名或密码错误"),
            ApiResponse(responseCode = "423", description = "账户被锁定")
        ]
    )
    @PostMapping("/login")
    suspend fun login(
        @Valid @RequestBody request: LoginRequest,
        httpRequest: HttpServletRequest
    ): ResponseEntity<LoginResponseDto> {
        logger.info("用户登录请求: email={}", request.email)
        
        val command = LoginCommand(
            email = request.email,
            password = request.password,
            ipAddress = getClientIpAddress(httpRequest),
            userAgent = httpRequest.getHeader("User-Agent")
        )
        
        val response = commandBus.send<LoginCommand, LoginResponseDto>(command)
        
        return ResponseEntity.ok(response)
    }
    
    /**
     * 获取客户端IP地址
     */
    private fun getClientIpAddress(request: HttpServletRequest): String {
        val xForwardedFor = request.getHeader("X-Forwarded-For")
        if (!xForwardedFor.isNullOrBlank()) {
            return xForwardedFor.split(",")[0].trim()
        }
        
        val xRealIp = request.getHeader("X-Real-IP")
        if (!xRealIp.isNullOrBlank()) {
            return xRealIp
        }
        
        return request.remoteAddr ?: "unknown"
    }
}

/**
 * 登录请求
 */
data class LoginRequest(
    @field:NotBlank(message = "邮箱不能为空")
    @field:Email(message = "邮箱格式不正确")
    val email: String,
    
    @field:NotBlank(message = "密码不能为空")
    @field:Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
    val password: String
)