package com.github.phanerozoicc.user.application

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

/**
 * 注册用户命令
 */
data class RegisterUserCommand(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,
    
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
    
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,
    
    @field:NotBlank(message = "Display name is required")
    @field:Size(max = 100, message = "Display name must be at most 100 characters")
    val displayName: String,
    
    val avatar: String? = null,
    
    @field:Size(max = 500, message = "Bio must be at most 500 characters")
    val bio: String? = null
)

/**
 * 用户认证命令
 */
data class AuthenticateUserCommand(
    @field:NotBlank(message = "Username is required")
    val username: String,
    
    @field:NotBlank(message = "Password is required")
    val password: String
)

/**
 * 更新用户资料命令
 */
data class UpdateUserProfileCommand(
    @field:NotBlank(message = "User ID is required")
    val userId: String,
    
    @field:NotBlank(message = "Display name is required")
    @field:Size(max = 100, message = "Display name must be at most 100 characters")
    val displayName: String,
    
    val avatar: String? = null,
    
    @field:Size(max = 500, message = "Bio must be at most 500 characters")
    val bio: String? = null
)

/**
 * 更改密码命令
 */
data class ChangePasswordCommand(
    @field:NotBlank(message = "User ID is required")
    val userId: String,
    
    @field:NotBlank(message = "Current password is required")
    val currentPassword: String,
    
    @field:NotBlank(message = "New password is required")
    @field:Size(min = 8, message = "New password must be at least 8 characters")
    val newPassword: String
)