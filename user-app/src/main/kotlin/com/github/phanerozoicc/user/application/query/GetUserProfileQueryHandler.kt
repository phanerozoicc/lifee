package com.github.phanerozoicc.user.application.query

import com.github.phanerozoicc.base.queries.Query
import com.github.phanerozoicc.base.queries.QueryHandler
import com.github.phanerozoicc.user.domain.model.User
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * 获取用户资料查询
 */
data class GetUserProfileQuery(
    val userId: UserId,
) : Query()

/**
 * 获取用户资料查询处理器
 */
@Service
class GetUserProfileQueryHandler(
    private val userRepository: UserRepository
) : QueryHandler<GetUserProfileQuery, UserProfileDTO?> {

    @Transactional(readOnly = true)
    override fun handle(query: GetUserProfileQuery): UserProfileDTO? {
        val user = userRepository.findById(query.userId)
        return user?.let { UserProfileDTO.fromUser(it) }
    }
}


/**
 * 用户资料DTO
 * 用于返回用户的基本资料信息
 */
data class UserProfileDTO(
    val userId: String,
    val email: String,
    val nickname: String,
    val firstName: String?,
    val lastName: String?,
    val fullName: String?,
    val displayName: String,
    val avatar: String?,
    val bio: String?,
    val birthDate: String?, // ISO格式日期字符串
    val age: Int?,
    val gender: String?,
    val phoneNumber: String?,
    val address: String?,
    val website: String?,
    val profileCompleteness: Int, // 资料完整度百分比
    val status: String,
    val statusDisplayName: String,
    val emailVerified: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val lastLoginAt: LocalDateTime?
) {
    companion object {
        /**
         * 从用户聚合根创建DTO
         */
        fun fromUser(user: User): UserProfileDTO {
            val profile = user.getProfile()
            val status = user.getStatus()

            return UserProfileDTO(
                userId = user.id.value,
                email = user.getEmail().value,
                nickname = profile.nickname,
                firstName = profile.firstName,
                lastName = profile.lastName,
                fullName = profile.firstName,
                displayName = profile.getDisplayName(),
                avatar = profile.avatar,
                bio = profile.bio,
                birthDate = profile.birthDate?.toString(),
                age = profile.getAge(),
                gender = profile.gender?.name,
                phoneNumber = profile.phoneNumber,
                address = profile.address,
                website = profile.website,
                profileCompleteness = profile.getCompletionPercentage(),
                status = status.status.name,
                statusDisplayName = status.getDisplayName(),
                emailVerified = user.isEmailVerified(),
                createdAt = user.getCreatedAt(),
                updatedAt = user.getUpdatedAt(),
                lastLoginAt = user.getLastLoginAt()
            )
        }
    }
}

