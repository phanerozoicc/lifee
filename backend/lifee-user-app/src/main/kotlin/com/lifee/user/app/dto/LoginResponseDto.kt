package com.lifee.user.app.dto

import java.time.Instant

/**
 * 登录响应DTO
 */
data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long,
    val user: UserDto
)

/**
 * JWT令牌信息
 */
data class JwtTokenInfo(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Instant
)