package com.github.phanerozoicc.user.interfaces.rest

import com.github.phanerozoicc.base.command.CommandBus
import com.github.phanerozoicc.base.queries.QueryBus
import com.github.phanerozoicc.base.response.ApiResponse
import com.github.phanerozoicc.base.response.PageResponse
import com.github.phanerozoicc.user.application.command.*
import com.github.phanerozoicc.user.application.query.GetUserProfileQuery
import com.github.phanerozoicc.user.application.query.ListUsersQuery
import com.github.phanerozoicc.user.application.query.UserProfileDTO
import com.github.phanerozoicc.user.application.query.UserSummaryDTO
import com.github.phanerozoicc.user.application.service.UserApplicationService
import com.github.phanerozoicc.user.domain.model.Email
import com.github.phanerozoicc.user.domain.model.UserId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = ["*"])
class UserController(
    val commandBus: CommandBus,
    val queryBus: QueryBus,
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
     * 激活用户 若注册后长时间未激活（通过定时间任务锁定） 需要重新激活
     */
    @PostMapping("/activate")
    @Operation(summary = "通过token激活用户", description = "通过token激活用户")
    suspend fun activateByToken(
        @Parameter(description = "激活令牌", required = true)
        @RequestParam @NotBlank token: String
    ): ResponseEntity<ApiResponse<Unit>> {
        val activationByTokenCommand = ActivationByTokenCommand(token)
        return try {
            commandBus.sendAndWait<ActivationByTokenCommand, Unit>(activationByTokenCommand)
            ResponseEntity.ok(ApiResponse.success("用户注册成功，请检查邮箱进行激活"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("用户注册失败", e.message))
        }
    }

    /**
     * 重新激活用户
     */
    @PostMapping("/reactivate")
    @Operation(summary = "重新激活用户", description = "重新激活用户")
    suspend fun reactivate(
        @Parameter(description = "用户邮箱", required = true)
        @RequestParam @NotBlank email: String
    ): ResponseEntity<ApiResponse<Unit>> {
        return try {
            val email = Email.of(email)
            val reactivateCommand = ReactivateUserCommand(email)
            commandBus.sendAndWait<ReactivateUserCommand, Unit>(reactivateCommand)
            ResponseEntity.ok(ApiResponse.success("用户已重新激活， 请检查邮箱"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("重新激活失败", e.message))
        }
    }


    /**
     * 获取用户资料
     */
    @GetMapping("/{userId}/profile")
    @Operation(summary = "根据ID获取用户信息", description = "获取指定用户的公开信息")
    suspend fun getUseProfile(
        @Parameter(description = "用户ID") @PathVariable userId: String
    ): ApiResponse<UserProfileDTO> {
        val getUserProfileQuery = GetUserProfileQuery(UserId.of(userId))
        val userProfileDTO = queryBus.send<GetUserProfileQuery, UserProfileDTO?>(getUserProfileQuery)
        return if (userProfileDTO!=null) {
            ApiResponse.success( userProfileDTO)
        } else {
            ApiResponse.error("未找到用户", "用户ID: $userId" )
        }
    }


    /**
     * 更新用户资料
     */
    @Operation(summary = "更新用户资料", description = "更新指定用户的个人资料")
    @ApiResponses(
        value = [
            io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "用户资料更新成功"),
            io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "用户资料更新失败")
        ]
    )
    @PutMapping("/{userId}/profile")
    fun updateUserProfile(
        @Parameter(description = "用户ID", required = true)
        @PathVariable @NotBlank userId: String,
        @Valid @RequestBody request: UpdateUserProfileRequest
    ): ResponseEntity<ApiResponse<UserProfileDTO>> {
        logger.info("更新用户档案请求 userId：{}", userId)
        val updateUserProfileCommand = UpdateUserProfileCommand(
            userId = UserId.of(userId),
            nickname = request.nickname,
            firstName = request.firstName,
            lastName = request.lastName,
            avatar = request.avatar,
            bio = request.bio,
            birthDate = request.birthDate,
            age = request.age,
            gender = request.gender,
            phoneNumber = request.phoneNumber,
            address = request.address,
            website = request.website
        )
        return try {
            runBlocking {
                commandBus.sendAndWait<UpdateUserProfileCommand, Unit>(updateUserProfileCommand)
                val getUserProfileQuery = GetUserProfileQuery(UserId.of(userId))
                val userProfileDTO = queryBus.send<GetUserProfileQuery, UserProfileDTO>(getUserProfileQuery)
                return@runBlocking ResponseEntity.ok(ApiResponse.success(userProfileDTO,
                    "用户信息修改成功"))
            }
        }catch (e : Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("用户信息修改失败", e.message))
        }

    }

    /**
     * 修改密码
     */
    @Operation(summary = "修改用户密码", description = "修改指定用户的登录密码")
    @PutMapping("/{userId}/change-password")
    suspend fun changePassword(
        @PathVariable userId: String,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<ApiResponse<Unit>> {
        logger.info("修改用户密码请求 userId：{}", userId)
        val changePasswordCommand = ChangePasswordCommand(
            userId = UserId.of(userId),
            currentPassword = request.currentPassword,
            newPassword = request.newPassword
        )
        commandBus.sendAndWait<ChangePasswordCommand, Unit>(changePasswordCommand)
        return ResponseEntity.ok(ApiResponse.success("密码修改成功"))
    }


    /**
     * 获取用户列表
     */
    @GetMapping
    suspend fun getUserList(
        @RequestParam(defaultValue = "0", required = false) page: Int,
        @RequestParam(defaultValue = "20", required = false) size: Int,
        @RequestParam(defaultValue = "createdAt", required = false) sortBy: String,
        @RequestParam(defaultValue = "desc") sortDir: String
    ): ResponseEntity<ApiResponse<PageResponse<UserSummaryDTO>>> {
        return try {
            val listUsersQuery = ListUsersQuery(
                pageNumber = page,
                pageSize = size,
                sortBy = sortBy,
                sortDirection = sortDir
            )
            val pageResponse = queryBus.send<ListUsersQuery, PageResponse<UserSummaryDTO>>(listUsersQuery)
            ResponseEntity.ok(ApiResponse.success(pageResponse,"获取用户列表成功"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("获取用户列表失败", e.message))
        }
    }

//    /**
//     * 搜索用户
//     */
//    @GetMapping("/search")
//    fun searchUsers(
//        @RequestParam keyword: String?,
//        @RequestParam status: String?,
//        @RequestParam emailVerified: Boolean?,
//        @RequestParam(defaultValue = "0") page: Int,
//        @RequestParam(defaultValue = "20") size: Int
//    ): ResponseEntity<ApiResponse<String>> {
//        return try {
//            ResponseEntity.ok(ApiResponse.success("搜索用户成功", "关键词: $keyword"))
//        } catch (e: Exception) {
//            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error<String>("搜索用户失败", e.message))
//        }
//    }

//    /**
//     * 获取用户统计
//     */
//    @GetMapping("/statistics")
//    fun getUserStatistics(): ResponseEntity<ApiResponse<String>> {
//        return try {
//            ResponseEntity.ok(ApiResponse.success("获取用户统计成功"))
//        } catch (e: Exception) {
//            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error<String>("获取用户统计失败", e.message))
//        }
//    }


//    /**
//     * 停用用户
//     */
//    @PutMapping("/{userId}/deactivate")
//    fun deactivateUser(@PathVariable userId: String): ResponseEntity<ApiResponse<String>> {
//        return try {
//            ResponseEntity.ok(ApiResponse.success("用户停用成功", "用户ID: $userId"))
//        } catch (e: Exception) {
//            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error<String>("用户停用失败", e.message))
//        }
//    }
//
//    /**
//     * 删除用户
//     */
//    @DeleteMapping("/{userId}")
//    fun deleteUser(@PathVariable userId: String): ResponseEntity<ApiResponse<String>> {
//        return try {
//            ResponseEntity.ok(ApiResponse.success("用户删除成功", "用户ID: $userId"))
//        } catch (e: Exception) {
//            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(ApiResponse.error<String>("用户删除失败", e.message))
//        }
//    }
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