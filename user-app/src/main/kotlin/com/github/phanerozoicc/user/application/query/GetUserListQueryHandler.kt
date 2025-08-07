package com.github.phanerozoicc.user.application.query

import com.github.phanerozoicc.user.bak.domain.cqrs.ListUsersQuery
import com.github.phanerozoicc.user.bak.domain.cqrs.QueryHandler
import com.github.phanerozoicc.user.bak.domain.cqrs.QueryResult
import com.github.phanerozoicc.user.bak.domain.repository.UserRepository
import com.github.phanerozoicc.user.bak.domain.repository.UserSearchCriteria
import com.github.phanerozoicc.user.domain.model.UserStatus
import com.github.phanerozoicc.user.domain.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service

/**
 * 获取用户列表查询处理器
 */
@Service
class GetUserListQueryHandler(
    private val userRepository: UserRepository
) : QueryHandler<ListUsersQuery, Page> {

    override suspend fun handle(query: ListUsersQuery): QueryResult<Page> {
        try {
            val pageable = PageRequest.of(
                query.pageNumber,
                query.pageSize,
                Sort.by(Sort.Direction.fromString(query.sortDirection), query.sortBy)
            )

            val criteria = UserSearchCriteria(
                status = query.status?.let { UserStatus.Status.valueOf(it) },
                createdAfter = query.createdAfter,
                createdBefore = query.createdBefore,
                lastLoginAfter = query.lastLoginAfter,
                lastLoginBefore = query.lastLoginBefore
            )

            val users = userRepository.findByCriteria(criteria, pageable)
            val totalCount = userRepository.countByCriteria(criteria)

            val userSummaries = users.map { UserSummaryDTO.fromUser(it) }

            val result = mapOf(
                "users" to userSummaries,
                "totalCount" to totalCount,
                "page" to query.page,
                "size" to query.size,
                "totalPages" to (totalCount + query.size - 1) / query.size
            )

            return QueryResult.Success(result)

        } catch (e: Exception) {
            return QueryResult.Error("获取用户列表失败: ${e.message}")
        }
    }
}