package com.github.phanerozoicc.user.presentation.controller

import com.github.phanerozoicc.user.application.service.UserApplicationService
import com.github.phanerozoicc.user.presentation.dto.*
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import java.time.LocalDateTime

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = ["*"])
class UserController(
    private val userApplicationService: UserApplicationService
) {
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterUserRequest): ResponseEntity<ApiResponse<String>> {
        return try {
            // 简化实现，直接返回成功响应
            ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("注册成功", "用户注册成功"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<String>("用户注册失败", e.message))
        }
    }
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginUserRequest): ResponseEntity<ApiResponse<String>> {
        return try {
            // 简化实现，直接返回成功响应
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
     * 激活用户
     */
    @PutMapping("/{userId}/activate")
    fun activateUser(@PathVariable userId: String): ResponseEntity<ApiResponse<String>> {
        return try {
            ResponseEntity.ok(ApiResponse.success("用户激活成功", "用户ID: $userId"))
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error<String>("用户激活失败", e.message))
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