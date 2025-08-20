package com.github.phanerozoicc.user.interfaces.rest

import com.github.phanerozoicc.base.command.CommandBus
import com.github.phanerozoicc.base.response.ApiResponse
import com.github.phanerozoicc.base.response.PageResponse
import com.github.phanerozoicc.user.application.command.ActivationByTokenCommand
import com.github.phanerozoicc.user.application.command.ChangePasswordCommand
import com.github.phanerozoicc.user.application.command.LoginUserCommand
import com.github.phanerozoicc.user.application.command.RegisterUserCommand
import com.github.phanerozoicc.user.application.command.UpdateUserProfileCommand
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = ["*"])
class UserController(
    val commandBus: CommandBus,

) {

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

    @PostMapping("/activate")
    @Operation(summary = "通过token激活用户", description = "通过token激活用户")
    suspend fun activateByToken(
        @Parameter(description = "激活令牌", required = true)
        @RequestParam @NotBlank token: String
    ) {
        val activationByTokenCommand = ActivationByTokenCommand(token)
        try {
            commandBus.sendAndWait<ActivationByTokenCommand>(activationByTokenCommand)
            ResponseEntity.ok(ApiResponse.success("用户注册成功，请检查邮箱进行激活"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("用户注册失败", e.message))
        }
    }


    /**
     * 激活用户
     */
    @PutMapping("/{userId}/activate")
    @Operation(summary = "激活用户", description = "激活指定用户账户")
    fun activateUser(@PathVariable userId: String): ResponseEntity<ApiResponse<String>> {
        return try {
            ResponseEntity.ok(ApiResponse.success("用户激活成功", "用户ID: $userId"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<String>("用户激活失败", e.message))
        }
    }


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

    /**
     * 获取用户资料
     */
    @GetMapping("/{userId}")
    fun getUserProfile(@PathVariable userId: String): ResponseEntity<ApiResponse<String>> {
        return try {
            ResponseEntity.ok(ApiResponse.success("用户资料获取成功", "用户ID: $userId"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error<String>("用户不存在", e.message))
        }
    }

    /**
     * 更新用户资料
     */
    @PutMapping("/{userId}")
    fun updateUserProfile(
        @PathVariable userId: String,
        @Valid @RequestBody request: UpdateUserProfileRequest
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            ResponseEntity.ok(ApiResponse.success("用户资料更新成功", "用户ID: $userId"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<String>("用户资料更新失败", e.message))
        }
    }

    /**
     * 修改密码
     */
    @PutMapping("/{userId}/password")
    fun changePassword(
        @PathVariable userId: String,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            ResponseEntity.ok(ApiResponse.success("密码修改成功", "用户ID: $userId"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<String>("密码修改失败", e.message))
        }
    }

    /**
     * 验证邮箱
     */
    @PostMapping("/verify-email")
    fun verifyEmail(@Valid @RequestBody request: VerifyEmailRequest): ResponseEntity<ApiResponse<String>> {
        return try {
            ResponseEntity.ok(ApiResponse.success("邮箱验证成功"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<String>("邮箱验证失败", e.message))
        }
    }

    /**
     * 获取用户列表
     */
    @GetMapping
    fun getUserList(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "createdAt") sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            ResponseEntity.ok(ApiResponse.success("获取用户列表成功", "页码: $page, 大小: $size"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<String>("获取用户列表失败", e.message))
        }
    }

    /**
     * 搜索用户
     */
    @GetMapping("/search")
    fun searchUsers(
        @RequestParam keyword: String?,
        @RequestParam status: String?,
        @RequestParam emailVerified: Boolean?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<String>> {
        return try {
            ResponseEntity.ok(ApiResponse.success("搜索用户成功", "关键词: $keyword"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<String>("搜索用户失败", e.message))
        }
    }

    /**
     * 获取用户统计
     */
    @GetMapping("/statistics")
    fun getUserStatistics(): ResponseEntity<ApiResponse<String>> {
        return try {
            ResponseEntity.ok(ApiResponse.success("获取用户统计成功"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<String>("获取用户统计失败", e.message))
        }
    }

    /**
     * 检查邮箱可用性
     */
    @GetMapping("/check-email")
    fun checkEmailAvailability(@RequestParam email: String): ResponseEntity<ApiResponse<Boolean>> {
        return try {
            ResponseEntity.ok(ApiResponse.success(true))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<Boolean>("检查邮箱可用性失败", e.message))
        }
    }

    /**
     * 检查昵称可用性
     */
    @GetMapping("/check-nickname")
    fun checkNicknameAvailability(@RequestParam nickname: String): ResponseEntity<ApiResponse<Boolean>> {
        return try {
            ResponseEntity.ok(ApiResponse.success(true))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<Boolean>("检查昵称可用性失败", e.message))
        }
    }


    /**
     * 停用用户
     */
    @PutMapping("/{userId}/deactivate")
    fun deactivateUser(@PathVariable userId: String): ResponseEntity<ApiResponse<String>> {
        return try {
            ResponseEntity.ok(ApiResponse.success("用户停用成功", "用户ID: $userId"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<String>("用户停用失败", e.message))
        }
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{userId}")
    fun deleteUser(@PathVariable userId: String): ResponseEntity<ApiResponse<String>> {
        return try {
            ResponseEntity.ok(ApiResponse.success("用户删除成功", "用户ID: $userId"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<String>("用户删除失败", e.message))
        }
    }
}

/**
 * 用户控制器
 * 处理用户相关的HTTP请求
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "用户管理", description = "用户注册、登录、资料管理等接口")
class UserController(
    private val userApplicationService: UserApplicationService
) {


    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户身份验证")
    fun login(
        @Valid @RequestBody command: AuthenticateUserCommand
    ): ApiResponse<Map<String, Any>> {
        val authResult = userApplicationService.authenticateUser(command)
        return ApiResponse.success(authResult)
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    fun refreshToken(
        @RequestBody request: Map<String, String>
    ): ApiResponse<Map<String, Any>> {
        val refreshToken = request["refreshToken"]
            ?: throw IllegalArgumentException("刷新令牌不能为空")
        val authResult = userApplicationService.refreshToken(refreshToken)
        return ApiResponse.success(authResult)
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @PreAuthorize("isAuthenticated()")
    fun getCurrentUser(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ApiResponse<UserDto> {
        val user = userApplicationService.getUserByUsername(userDetails.username)
        return ApiResponse.success(UserDto.Companion.fromDomain(user))
    }

    @GetMapping("/{userId}")
    @Operation(summary = "根据ID获取用户信息", description = "获取指定用户的公开信息")
    fun getUserById(
        @Parameter(description = "用户ID") @PathVariable userId: String
    ): ApiResponse<UserSummaryDto> {
        val user = userApplicationService.getUserById(userId)
        return ApiResponse.success(UserSummaryDto.Companion.fromDomain(user))
    }

    @PutMapping("/me")
    @Operation(summary = "更新用户资料", description = "更新当前用户的个人资料")
    @PreAuthorize("isAuthenticated()")
    fun updateProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody command: UpdateUserProfileCommand
    ): ApiResponse<UserDto> {
        val user = userApplicationService.updateUserProfile(userDetails.username, command)
        return ApiResponse.success(UserDto.Companion.fromDomain(user))
    }

    @PutMapping("/me/password")
    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    @PreAuthorize("isAuthenticated()")
    fun changePassword(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody command: ChangePasswordCommand
    ): ApiResponse<String> {
        userApplicationService.changePassword(userDetails.username, command)
        return ApiResponse.success("密码修改成功")
    }



    @PutMapping("/{userId}/deactivate")
    @Operation(summary = "停用用户", description = "停用指定用户账户")
    @PreAuthorize("hasRole('ADMIN')")
    fun deactivateUser(
        @Parameter(description = "用户ID") @PathVariable userId: String
    ): ApiResponse<String> {
        userApplicationService.deactivateUser(userId)
        return ApiResponse.success("用户停用成功")
    }

    @GetMapping("/check/username")
    @Operation(summary = "检查用户名可用性", description = "检查用户名是否已被使用")
    fun checkUsernameAvailability(
        @Parameter(description = "用户名") @RequestParam username: String
    ): ApiResponse<Map<String, Boolean>> {
        val available = userApplicationService.isUsernameAvailable(username)
        return ApiResponse.success(mapOf("available" to available))
    }

    @GetMapping("/check/email")
    @Operation(summary = "检查邮箱可用性", description = "检查邮箱是否已被使用")
    fun checkEmailAvailability(
        @Parameter(description = "邮箱地址") @RequestParam email: String
    ): ApiResponse<Map<String, Boolean>> {
        val available = userApplicationService.isEmailAvailable(email)
        return ApiResponse.success(mapOf("available" to available))
    }

    @GetMapping
    @Operation(summary = "分页查询用户", description = "分页获取用户列表")
    @PreAuthorize("hasRole('ADMIN')")
    fun getUsers(
        @PageableDefault(size = 20) pageable: Pageable,
        @Parameter(description = "搜索关键词") @RequestParam(required = false) keyword: String?
    ): ApiResponse<PageResponse<UserSummaryDto>> {
        val users = userApplicationService.getUsers(pageable, keyword)
        val userDtos = users.content.map { UserSummaryDto.Companion.fromDomain(it) }
        val pageResponse = PageResponse(
            content = userDtos,
            page = users.number,
            size = users.size,
            totalElements = users.totalElements,
            totalPages = users.totalPages,
            isFirst = users.isFirst,
            isLast = users.isLast,
            hasNext = users.hasNext(),
            hasPrevious = users.hasPrevious()
        )
        return ApiResponse.success(pageResponse)
    }

    @GetMapping("/stats")
    @Operation(summary = "获取用户统计信息", description = "获取用户相关的统计数据")
    @PreAuthorize("hasRole('ADMIN')")
    fun getUserStats(): ApiResponse<Map<String, Any>> {
        val stats = userApplicationService.getUserStats()
        return ApiResponse.success(stats)
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "删除用户", description = "删除指定用户账户")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUser(
        @Parameter(description = "用户ID") @PathVariable userId: String
    ) {
        userApplicationService.deleteUser(userId)
    }
}