package com.github.phanerozoicc.user.interfaces.rest

import com.github.phanerozoicc.user.domain.User
import com.github.phanerozoicc.user.domain.UserStatus
import java.time.LocalDateTime

/**
 * 用户数据传输对象
 */
data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val displayName: String,
    val avatar: String?,
    val bio: String?,
    val status: UserStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun fromDomain(user: User): UserDto {
            return UserDto(
                id = user.id.toString(),
                username = user.getUsername().value,
                email = user.getEmail().value,
                displayName = user.getProfile().displayName,
                avatar = user.getProfile().avatar,
                bio = user.getProfile().bio,
                status = user.getStatus(),
                createdAt = user.getCreatedAt(),
                updatedAt = user.getUpdatedAt()
            )
        }
        
        fun from(user: User): UserDto = fromDomain(user)
    }
}

/**
 * 用户简要信息DTO
 */
data class UserSummaryDto(
    val id: String,
    val username: String,
    val displayName: String,
    val avatar: String?
) {
    companion object {
        fun fromDomain(user: User): UserSummaryDto {
            return UserSummaryDto(
                id = user.id.toString(),
                username = user.getUsername().value,
                displayName = user.getProfile().displayName,
                avatar = user.getProfile().avatar
            )
        }
        
        fun from(user: User): UserSummaryDto = fromDomain(user)
    }
}