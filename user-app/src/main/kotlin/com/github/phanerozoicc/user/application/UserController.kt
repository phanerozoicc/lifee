package com.github.phanerozoicc.user.application

import com.github.phanerozoicc.response.ApiResponse
import com.github.phanerozoicc.response.PageResponse
import com.github.phanerozoicc.user.application.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

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

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户账户")
    fun register(
        @Valid @RequestBody command: RegisterUserCommand
    ): ApiResponse<UserDto> {
        val user = userApplicationService.registerUser(command)
        return ApiResponse.success(UserDto.fromDomain(user))
    }

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
        return ApiResponse.success(UserDto.fromDomain(user))
    }

    @GetMapping("/{userId}")
    @Operation(summary = "根据ID获取用户信息", description = "获取指定用户的公开信息")
    fun getUserById(
        @Parameter(description = "用户ID") @PathVariable userId: String
    ): ApiResponse<UserSummaryDto> {
        val user = userApplicationService.getUserById(userId)
        return ApiResponse.success(UserSummaryDto.fromDomain(user))
    }

    @PutMapping("/me")
    @Operation(summary = "更新用户资料", description = "更新当前用户的个人资料")
    @PreAuthorize("isAuthenticated()")
    fun updateProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody command: UpdateUserProfileCommand
    ): ApiResponse<UserDto> {
        val user = userApplicationService.updateUserProfile(userDetails.username, command)
        return ApiResponse.success(UserDto.fromDomain(user))
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

    @PutMapping("/{userId}/activate")
    @Operation(summary = "激活用户", description = "激活指定用户账户")
    @PreAuthorize("hasRole('ADMIN')")
    fun activateUser(
        @Parameter(description = "用户ID") @PathVariable userId: String
    ): ApiResponse<String> {
        userApplicationService.activateUser(userId)
        return ApiResponse.success("用户激活成功")
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
        val userDtos = users.content.map { UserSummaryDto.fromDomain(it) }
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