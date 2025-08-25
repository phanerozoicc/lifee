package com.github.phanerozoicc.user.application.query

import com.github.phanerozoicc.base.queries.Query
import com.github.phanerozoicc.base.queries.QueryHandler
import com.github.phanerozoicc.user.bak.domain.cqrs.QueryResult
import com.github.phanerozoicc.user.bak.domain.cqrs.UserQuery
import com.github.phanerozoicc.user.domain.model.UserId
import com.github.phanerozoicc.user.domain.repository.UserRepository
import com.github.phanerozoicc.user.interfaces.rest.UserProfileDTO
import org.springframework.stereotype.Service
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
) : QueryHandler<GetUserProfileQuery, UserProfileDTO> {

    override suspend fun handle(query: GetUserProfileQuery): QueryResult<UserProfileDTO> {
        try {
            val user = userRepository.findById(query.userId)
                ?: return QueryResult.NotFound("用户不存在")

            val profile = UserProfileDTO.fromUser(user)

            return QueryResult.Success(profile)

        } catch (e: Exception) {
            return QueryResult.Error("获取用户资料失败: ${e.message}")
        }
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
                userId = user.id.getValue(),
                email = user.getEmail().getValue(),
                nickname = profile.getNickname(),
                firstName = profile.getFirstName(),
                lastName = profile.getLastName(),
                fullName = profile.getFullName(),
                displayName = profile.getDisplayName(),
                avatar = profile.getAvatar(),
                bio = profile.getBio(),
                birthDate = profile.getBirthDate()?.toString(),
                age = profile.getAge(),
                gender = profile.getGender()?.name,
                phoneNumber = profile.getPhoneNumber(),
                address = profile.getAddress(),
                website = profile.getWebsite(),
                profileCompleteness = profile.getCompletionPercentage(),
                status = status.getStatus().name,
                statusDisplayName = status.getDisplayName(),
                emailVerified = user.isEmailVerified(),
                createdAt = user.getCreatedAt(),
                updatedAt = user.getUpdatedAt(),
                lastLoginAt = user.getLastLoginAt()
            )
        }
    }
}

