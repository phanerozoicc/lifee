package com.lifee.user.app.dto

import com.lifee.user.domain.User
import com.lifee.user.domain.UserStatus
import java.time.Instant
import java.time.LocalDate

/**
 * 用户数据传输对象
 */
data class UserDto(
    val id: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val fullName: String,
    val dateOfBirth: LocalDate?,
    val phoneNumber: String?,
    val avatar: String?,
    val status: UserStatus,
    val emailVerified: Boolean,
    val lastLoginAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        /**
         * 从User领域对象转换为DTO
         */
        fun fromDomain(user: User): UserDto {
            val profile = user.getProfile()
            return UserDto(
                id = user.getId().toString(),
                email = user.getEmail().toString(),
                firstName = profile.firstName,
                lastName = profile.lastName,
                fullName = profile.getFullName(),
                dateOfBirth = profile.dateOfBirth,
                phoneNumber = profile.phoneNumber,
                avatar = profile.avatar,
                status = user.getStatus(),
                emailVerified = user.isEmailVerified(),
                lastLoginAt = user.getLastLoginAt(),
                createdAt = user.getCreatedAt(),
                updatedAt = user.getUpdatedAt()
            )
        }
    }
}