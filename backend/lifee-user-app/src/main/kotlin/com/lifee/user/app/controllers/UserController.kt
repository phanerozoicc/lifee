package com.lifee.user.app.controllers

import com.lifee.common.cqrs.commands.CommandBus
import com.lifee.common.cqrs.queries.QueryBus
import com.lifee.user.app.commands.ActivateUserCommand
import com.lifee.user.app.commands.ActivateUserByTokenCommand
import com.lifee.user.app.commands.ChangePasswordCommand
import com.lifee.user.app.commands.ForgotPasswordCommand
import com.lifee.user.app.commands.RegisterUserCommand
import com.lifee.user.app.commands.ResetPasswordCommand
import com.lifee.user.app.commands.UpdateUserProfileCommand
import com.lifee.user.app.dto.UserDto
import com.lifee.user.app.queries.GetUserByEmailQuery
import com.lifee.user.app.queries.GetUserByIdQuery
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/users")
@Validated
@Tag(name = "用户管理", description = "用户注册、激活、档案管理等功能")
class UserController(
    private val commandBus: CommandBus,
    private val queryBus: QueryBus
) {
    
    private val logger = LoggerFactory.getLogger(UserController::class.java)
    
    @Operation(summary = "用户注册", description = "注册新用户账户")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "注册成功"),
            ApiResponse(responseCode = "400", description = "请求参数错误"),
            ApiResponse(responseCode = "409", description = "邮箱已存在")
        ]
    )
    @PostMapping("/register")
    suspend fun register(@Valid @RequestBody command: RegisterUserCommand): ResponseEntity<Map<String, String>> {
        logger.info("用户注册请求: email={}", command.email)
        
        commandBus.sendAndWait<RegisterUserCommand, Unit>(command)
        
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(mapOf("message" to "用户注册成功，请检查邮箱进行激活"))
    }
    
    @Operation(summary = "激活用户", description = "激活用户账户")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "激活成功"),
            ApiResponse(responseCode = "400", description = "请求参数错误"),
            ApiResponse(responseCode = "404", description = "用户不存在")
        ]
    )
    @PostMapping("/{userId}/activate")
    suspend fun activate(
        @Parameter(description = "用户ID", required = true)
        @PathVariable @NotBlank userId: String
    ): ResponseEntity<Map<String, String>> {
        logger.info("用户激活请求: userId={}", userId)
        
        val command = ActivateUserCommand(userId)
        commandBus.sendAndWait<ActivateUserCommand, Unit>(command)
        
        return ResponseEntity.ok(mapOf("message" to "用户激活成功"))
    }
    
    @Operation(summary = "通过令牌激活用户", description = "使用激活令牌激活用户账户")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "激活成功"),
            ApiResponse(responseCode = "400", description = "令牌无效或已过期"),
            ApiResponse(responseCode = "404", description = "用户不存在")
        ]
    )
    @PostMapping("/activate")
    suspend fun activateUserByToken(
        @Parameter(description = "激活令牌", required = true)
        @RequestParam @NotBlank token: String
    ): ResponseEntity<Map<String, String>> {
        logger.info("通过令牌激活用户请求: token={}", token)
        
        val command = ActivateUserByTokenCommand(token)
        commandBus.sendAndWait<ActivateUserByTokenCommand, Unit>(command)
        
        return ResponseEntity.ok(mapOf("message" to "用户激活成功"))
    }
    
    @Operation(summary = "根据ID查询用户", description = "根据用户ID查询用户信息")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "查询成功"),
            ApiResponse(responseCode = "404", description = "用户不存在")
        ]
    )
    @GetMapping("/{userId}")
    suspend fun getUserById(
        @Parameter(description = "用户ID", required = true)
        @PathVariable @NotBlank userId: String
    ): ResponseEntity<UserDto> {
        logger.debug("查询用户请求: userId={}", userId)
        
        val query = GetUserByIdQuery(userId)
        val user = queryBus.send<GetUserByIdQuery, UserDto?>(query)
        
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @Operation(summary = "根据邮箱查询用户", description = "根据邮箱查询用户信息")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "查询成功"),
            ApiResponse(responseCode = "404", description = "用户不存在")
        ]
    )
    @GetMapping("/by-email")
    suspend fun getUserByEmail(
        @Parameter(description = "用户邮箱", required = true)
        @RequestParam @NotBlank @Email email: String
    ): ResponseEntity<UserDto> {
        logger.debug("根据邮箱查询用户请求: email={}", email)
        
        val query = GetUserByEmailQuery(email)
        val user = queryBus.send<GetUserByEmailQuery, UserDto?>(query)
        
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }
    
    @Operation(summary = "更新用户档案", description = "更新用户个人档案信息")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "更新成功"),
            ApiResponse(responseCode = "400", description = "请求参数错误"),
            ApiResponse(responseCode = "404", description = "用户不存在")
        ]
    )
    @PutMapping("/{userId}/profile")
    suspend fun updateProfile(
        @Parameter(description = "用户ID", required = true)
        @PathVariable @NotBlank userId: String,
        @Valid @RequestBody request: UpdateUserProfileRequest
    ): ResponseEntity<Map<String, String>> {
        logger.info("更新用户档案请求: userId={}", userId)
        
        val command = UpdateUserProfileCommand(
            userId = userId,
            firstName = request.firstName,
            lastName = request.lastName,
            dateOfBirth = request.dateOfBirth,
            phoneNumber = request.phoneNumber,
            avatar = request.avatar
        )
        
        commandBus.sendAndWait<UpdateUserProfileCommand, Unit>(command)
        
        return ResponseEntity.ok(mapOf("message" to "用户档案更新成功"))
    }
    
    @Operation(summary = "更改密码", description = "更改用户密码")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "密码更改成功"),
            ApiResponse(responseCode = "400", description = "请求参数错误"),
            ApiResponse(responseCode = "401", description = "当前密码不正确"),
            ApiResponse(responseCode = "404", description = "用户不存在")
        ]
    )
    @PostMapping("/{userId}/change-password")
    suspend fun changePassword(
        @Parameter(description = "用户ID", required = true)
        @PathVariable @NotBlank userId: String,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<Map<String, String>> {
        logger.info("更改密码请求: userId={}", userId)
        
        val command = ChangePasswordCommand(
            userId = userId,
            currentPassword = request.currentPassword,
            newPassword = request.newPassword
        )
        
        commandBus.sendAndWait<ChangePasswordCommand, Unit>(command)
        
        return ResponseEntity.ok(mapOf("message" to "密码更改成功"))
    }
    
    @Operation(summary = "忘记密码", description = "发送密码重置邮件")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "重置邮件发送成功"),
            ApiResponse(responseCode = "400", description = "请求参数错误"),
            ApiResponse(responseCode = "404", description = "用户不存在")
        ]
    )
    @PostMapping("/forgot-password")
    suspend fun forgotPassword(
        @Valid @RequestBody request: ForgotPasswordRequest
    ): ResponseEntity<Map<String, String>> {
        logger.info("忘记密码请求: email={}", request.email)
        
        val command = ForgotPasswordCommand(request.email)
        commandBus.sendAndWait<ForgotPasswordCommand, Unit>(command)
        
        return ResponseEntity.ok(mapOf("message" to "密码重置邮件已发送"))
    }
    
    @Operation(summary = "重置密码", description = "使用令牌重置密码")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "密码重置成功"),
            ApiResponse(responseCode = "400", description = "令牌无效或已过期"),
            ApiResponse(responseCode = "404", description = "用户不存在")
        ]
    )
    @PostMapping("/reset-password")
    suspend fun resetPassword(
        @Valid @RequestBody request: ResetPasswordRequest
    ): ResponseEntity<Map<String, String>> {
        logger.info("重置密码请求: token={}", request.token)
        
        val command = ResetPasswordCommand(
            token = request.token,
            newPassword = request.newPassword
        )
        commandBus.sendAndWait<ResetPasswordCommand, Unit>(command)
        
        return ResponseEntity.ok(mapOf("message" to "密码重置成功"))
    }
}

/**
 * 更新用户档案请求
 */
data class UpdateUserProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val dateOfBirth: java.time.LocalDate? = null,
    val phoneNumber: String? = null,
    val avatar: String? = null
)

/**
 * 更改密码请求
 */
data class ChangePasswordRequest(
    @field:NotBlank(message = "当前密码不能为空")
    val currentPassword: String,
    
    @field:NotBlank(message = "新密码不能为空")
    val newPassword: String
)

/**
 * 忘记密码请求
 */
data class ForgotPasswordRequest(
    @field:NotBlank(message = "邮箱不能为空")
    @field:Email(message = "邮箱格式不正确")
    val email: String
)

/**
 * 重置密码请求
 */
data class ResetPasswordRequest(
    @field:NotBlank(message = "令牌不能为空")
    val token: String,
    
    @field:NotBlank(message = "新密码不能为空")
    val newPassword: String
)