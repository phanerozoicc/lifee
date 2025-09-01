package com.github.phanerozoicc.user.application.query

import com.github.phanerozoicc.base.queries.AsyncQueryHandler
import com.github.phanerozoicc.base.queries.Query
import com.github.phanerozoicc.base.response.PageResponse
import com.github.phanerozoicc.user.domain.model.User
import com.github.phanerozoicc.user.domain.model.UserStatus
import com.github.phanerozoicc.user.domain.repository.UserRepository
import com.github.phanerozoicc.user.domain.repository.UserSearchCriteria
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.time.LocalDateTime


/**
 * 用户列表查询
 */
data class ListUsersQuery(
    val pageSize: Int = 20,
    val pageNumber: Int = 1,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC",
    val status: String? = null,
    val emailVerified: Boolean? = null,
    val createdAfter: LocalDateTime? = null,
    val createdBefore: LocalDateTime? = null,
    val lastLoginAfter: LocalDateTime? = null,
    val lastLoginBefore: LocalDateTime? = null
) : Query()


/**
 * 获取用户列表查询处理器
 */
@Service
class GetUserListQueryHandler(
    private val userRepository: UserRepository
) : AsyncQueryHandler<ListUsersQuery, PageResponse<UserSummaryDTO>> {

    override suspend fun handle(query: ListUsersQuery): PageResponse<UserSummaryDTO> {
        val pageable = PageRequest.of(
            query.pageNumber,
            query.pageSize,
            Sort.by(Sort.Direction.fromString(query.sortDirection), query.sortBy)
        )

        val criteria = UserSearchCriteria(
            status = query.status?.let { UserStatus.valueOf(it) },
            createdAfter = query.createdAfter,
            createdBefore = query.createdBefore,
            lastLoginAfter = query.lastLoginAfter,
            lastLoginBefore = query.lastLoginBefore
        )

        val users = userRepository.findByCriteria(criteria, pageable)
        val userSummaries = users.map { UserSummaryDTO.fromUser(it) }

        return PageResponse.from(userSummaries)
    }
}

/**
 * 用户摘要DTO
 * 用于列表显示的简化用户信息
 */
data class UserSummaryDTO(
    val userId: String,
    val email: String,
    val nickname: String,
    val displayName: String,
    val avatar: String?,
    val status: String,
    val statusDisplayName: String,
    val emailVerified: Boolean,
    val createdAt: LocalDateTime,
    val lastLoginAt: LocalDateTime?
) {
    companion object {
        /**
         * 从用户聚合根创建DTO
         */
        fun fromUser(user: User): UserSummaryDTO {
            val profile = user.getProfile()
            val status = user.getStatus()

            return UserSummaryDTO(
                userId = user.id.value,
                email = user.getEmail().value,
                nickname = profile.nickname,
                displayName = profile.getDisplayName(),
                avatar = profile.avatar,
                status = status.status.name,
                statusDisplayName = status.getDisplayName(),
                emailVerified = user.isEmailVerified(),
                createdAt = user.getCreatedAt(),
                lastLoginAt = user.getLastLoginAt()
            )
        }
    }
}
