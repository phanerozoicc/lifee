package com.github.phanerozoicc.user.interfaces.rest

import com.github.phanerozoicc.base.command.CommandBus
import com.github.phanerozoicc.base.queries.QueryBus
import com.github.phanerozoicc.base.response.ApiResponse
import com.github.phanerozoicc.user.application.command.ChangePasswordCommand
import com.github.phanerozoicc.user.application.command.LoginResponse
import com.github.phanerozoicc.user.application.command.LoginUserCommand
import com.github.phanerozoicc.user.application.command.UpdateUserProfileCommand
import com.github.phanerozoicc.user.application.query.GetUserProfileQuery
import com.github.phanerozoicc.user.application.query.UserProfileDTO
import com.github.phanerozoicc.user.application.service.UserApplicationService
import com.github.phanerozoicc.user.domain.model.UserId
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import kotlinx.coroutines.runBlocking
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

@Controller("/api/v1/auth")
class AuthController(
    private val commandBus: CommandBus,
    private val userApplicationService: UserApplicationService,
    private val queryBus: QueryBus,
) {

    companion object: KLogging()

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


    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的详细信息")
    @PreAuthorize("isAuthenticated()")
    suspend fun getCurrentUser(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ApiResponse<UserProfileDTO> {
        val getUserProfileQuery = GetUserProfileQuery(UserId.of(userDetails.username))
        val userProfileDTO = queryBus.send<GetUserProfileQuery, UserProfileDTO?>(getUserProfileQuery)
        return ApiResponse.success( userProfileDTO!!)
    }


      @PutMapping("/me")
    @Operation(summary = "更新用户资料", description = "更新当前用户的个人资料")
    @PreAuthorize("isAuthenticated()")
    suspend fun updateProfile(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: UpdateUserProfileRequest
    ): ResponseEntity<ApiResponse<UserProfileDTO>> {
        val userId = userDetails.username
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